package com.pkmprojects.shoppiq.aiservice.tools;

import com.pkmprojects.shoppiq.aiservice.config.ChatMemoryConfig;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.exception.AiAssistantException;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.aiservice.service.ChatOrderService;
import com.pkmprojects.shoppiq.aiservice.service.ChatProductService;
import com.pkmprojects.shoppiq.aiservice.service.ChatReviewService;
import com.pkmprojects.shoppiq.dto.cart.CartResponse;
import com.pkmprojects.shoppiq.entity.item.Item;
import com.pkmprojects.shoppiq.entity.order.Order;
import com.pkmprojects.shoppiq.entity.review.ItemReview;
import com.pkmprojects.shoppiq.entity.user.User;
import com.pkmprojects.shoppiq.exception.general.aiservice.AiConversationNotFoundException;
import com.pkmprojects.shoppiq.service.cart.CartService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsLessThanOrEqualTo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * LangChain4j tool methods that give the AI assistant access to Shoppiq's data.
 *
 * <p>This component provides a collection of tool methods annotated with
 * {@code @Tool} that the AI model can invoke during conversations to
 * retrieve real data from the Shoppiq database. Tools include product
 * detail lookup, order status checking, cart contents retrieval, user
 * review access, semantic product search via vector embeddings, and
 * conversation resolution.</p>
 *
 * <p>Each tool method includes a descriptive annotation that the LLM uses
 * to decide when to invoke it. Tools are only available to authenticated
 * users; guest conversations rely solely on RAG-based product retrieval
 * without tool access. The {@code @ToolMemoryId} annotation provides
 * the conversation's chat ID for user resolution and memory isolation.</p>
 *
 * @author prabhatkrmishra
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ShoppiqTools {

    private static final Logger log = LoggerFactory.getLogger(ShoppiqTools.class);

    private final ChatProductService chatProductService;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMemoryConfig chatMemoryConfig;
    private final ChatOrderService chatOrderService;
    private final CartService cartService;
    private final ChatReviewService chatReviewService;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final Clock clock;

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] ShoppiqTools initialised — chatProductService={}, chatOrderService={}, cartService={}, chatReviewService={}",
                chatProductService != null ? "OK" : "NULL",
                chatOrderService != null ? "OK" : "NULL",
                cartService != null ? "OK" : "NULL",
                chatReviewService != null ? "OK" : "NULL");
    }

    /**
     * Retrieves detailed information for a specific product by slug or name.
     *
     * <p>This tool first attempts an exact slug lookup via
     * {@link ChatProductService#findBySlug(String)}. If no slug matches,
     * it falls back to a case-insensitive name search via
     * {@link ChatProductService#findByNameContaining(String, int)}.
     * The returned string includes product name, SKU, price, discount
     * percentage, stock quantity, category, description, and URL link.</p>
     *
     * <p>Used by the AI model when a user asks about a specific product's
     * details, specifications, or availability.</p>
     *
     * @param identifier the product slug (e.g., "wireless-headphones") or name
     * @return formatted product detail string, or a "not found" message
     */
    @Tool("Get detailed information about a specific product by name or slug. Use this when the user asks about a specific product's details, specs, or availability.")
    public String getProductDetail(
            @P("Product name or slug") String identifier) {

        Item item = chatProductService.findBySlug(identifier).orElse(null);

        if (item == null) {
            List<Item> candidates = chatProductService.findByNameContaining(identifier, 1);
            if (candidates.isEmpty()) {
                return "No product found with identifier '" + identifier + "'.";
            }
            item = candidates.getFirst();
        }

        if (item.getItemDetails() == null) {
            return "Product '" + item.getName() + "' found but detailed information is currently unavailable.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("**").append(item.getName()).append("**\n\n");
        sb.append("SKU: ").append(item.getItemDetails().getSku()).append("\n");
        sb.append("Price: $").append(item.getItemDetails().getPrice());
        if (item.getItemDetails().getDiscountPercentage() != null
                && item.getItemDetails().getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" (").append(item.getItemDetails().getDiscountPercentage()).append("% off)");
        }
        sb.append("\n");
        sb.append("Stock: ").append(item.getItemDetails().getStockQuantity()).append(" units\n");
        if (item.getItemDetails().getCategory() != null) {
            sb.append("Category: ").append(item.getItemDetails().getCategory().getName()).append("\n");
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            sb.append("\nDescription: ").append(item.getDescription()).append("\n");
        }
        sb.append("\nURL: /item/").append(item.getSlug());

        return sb.toString();
    }

    /**
     * Returns the authenticated user's recent order history.
     *
     * <p>This tool supports two modes: single-order lookup (when an order
     * number is provided) and recent-order listing (when omitted). Single-order
     * mode validates ownership before returning details. Recent-order mode
     * returns up to the specified limit (default 5) orders with ID, status,
     * grand total, and placement timestamp.</p>
     *
     * <p>Used by the AI model when a user asks about their orders, delivery
     * status, or purchase history.</p>
     *
     * @param orderNumber optional specific order number to filter by
     * @param limit       maximum number of orders to return (default 5)
     * @param chatId      the conversation ID, used to resolve the authenticated user
     * @return formatted order list, single order details, or a "no orders" message
     */
    @Tool("Get the status and details of the user's recent orders. Use this when the user asks about their orders, delivery status, or purchase history.")
    public String getOrderStatus(
            @P(value = "Specific order number (e.g., '57'). Omit to get recent orders.", required = false) String orderNumber,
            @P(value = "Number of recent orders to return (default 5)", required = false) Integer limit,
            @ToolMemoryId String chatId) {

        User user = resolveUser(chatId);

        if (orderNumber != null && !orderNumber.isBlank() && !"None".equals(orderNumber)) {
            try {
                Long orderId = Long.parseLong(orderNumber.trim());
                return chatOrderService.findById(orderId)
                        .filter(order -> order.getUser().getId().equals(user.getId()))
                        .map(order -> {
                            String sb = "Order #" + order.getId() + "\n" +
                                    "Status: " + order.getStatus() + "\n" +
                                    "Total: $" + order.getGrandTotal() + "\n" +
                                    "Placed: " + order.getPlacedAt() + "\n";
                            return sb;
                        })
                        .orElse("No order found with number '" + orderNumber + "'.");
            } catch (NumberFormatException _) {
                return "Invalid order number '" + orderNumber + "'. Please provide a numeric order ID.";
            }
        }

        List<Order> orders = chatOrderService.findByUserNewestFirst(user);

        if (orders.isEmpty()) {
            return "You have no orders yet.";
        }

        int maxResults = Math.min(limit != null ? limit : 5, orders.size());
        StringBuilder sb = new StringBuilder();
        sb.append("Your recent orders:\n\n");

        for (int i = 0; i < maxResults; i++) {
            Order order = orders.get(i);
            sb.append("- Order #").append(order.getId()).append("\n");
            sb.append("  Status: ").append(order.getStatus()).append("\n");
            sb.append("  Total: $").append(order.getGrandTotal()).append("\n");
            sb.append("  Placed: ").append(order.getPlacedAt()).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * Returns the authenticated user's current shopping cart contents.
     *
     * <p>This tool retrieves the user's cart via {@link CartService} and
     * formats the contents as a readable summary including item names,
     * quantities, unit prices, and the cart subtotal. Returns an "empty
     * cart" message if the cart contains no items.</p>
     *
     * <p>Used by the AI model when a user asks what's in their cart, the
     * cart total, or wants to discuss their cart items.</p>
     *
     * @param chatId the conversation ID, used to resolve the authenticated user
     * @return formatted cart summary or an "empty cart" message
     */
    @Tool("Get the user's current shopping cart contents. Use this when the user asks what's in their cart, cart total, or wants to discuss their cart items.")
    public String getCartContents(@ToolMemoryId String chatId) {
        User user = resolveUser(chatId);
        CartResponse cart = cartService.get(user);

        if (cart.totalItems() == 0) {
            return "Your cart is empty.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Your cart (").append(cart.totalItems()).append(" items, subtotal: $").append(cart.subtotal()).append("):\n\n");

        for (var item : cart.items()) {
            sb.append("- ").append(item.itemName()).append(" x").append(item.quantity());
            sb.append(" — $").append(item.unitPrice()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns product reviews written by the authenticated user.
     *
     * <p>This tool retrieves the user's review history via
     * {@link ChatReviewService} and formats each review with the product
     * name, rating (out of 5), and review text. Results are limited to
     * the specified count (default 5) and ordered by most recent first.</p>
     *
     * <p>Used by the AI model when a user asks about reviews they've
     * written or wants to see their review history.</p>
     *
     * @param limit  maximum number of reviews to return (default 5)
     * @param chatId the conversation ID, used to resolve the authenticated user
     * @return formatted review list or a "no reviews" message
     */
    @Tool("Get the user's product reviews. Use this when the user asks about reviews they've written.")
    public String getUserReviews(
            @P(value = "Number of recent reviews to return (default 5)", required = false) Integer limit,
            @ToolMemoryId String chatId) {

        User user = resolveUser(chatId);
        List<ItemReview> reviews = chatReviewService.findByUserNewestFirst(user.getId());

        if (reviews.isEmpty()) {
            return "You haven't written any reviews yet.";
        }

        int maxResults = Math.min(limit != null ? limit : 5, reviews.size());
        StringBuilder sb = new StringBuilder();
        sb.append("Your recent reviews:\n\n");

        for (int i = 0; i < maxResults; i++) {
            ItemReview review = reviews.get(i);
            sb.append("- **").append(review.getItem().getName()).append("**\n");
            sb.append("  Rating: ").append(review.getRating()).append("/5\n");
            if (review.getReview() != null && !review.getReview().isBlank()) {
                sb.append("  Review: ").append(review.getReview()).append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Resolves (closes) the current conversation.
     *
     * <p>This tool validates conversation ownership, marks the conversation
     * as RESOLVED, records the resolution timestamp, appends a SYSTEM
     * message, and clears the chat memory window. Returns a confirmation
     * message or indicates that the conversation is already resolved.</p>
     *
     * <p>Used by the AI model when the user indicates they are done
     * (e.g., says "thanks", "bye", "that's all", or explicitly asks
     * to close the chat).</p>
     *
     * @param chatId the conversation ID, used to resolve the conversation
     * @return confirmation message
     */
    @Tool("Resolve (close) the current conversation. Use this when the user indicates they are done (e.g., says 'thanks', 'bye', 'that's all', or explicitly asks to close the chat).")
    public String resolveCurrentConversation(@ToolMemoryId String chatId) {
        User user = resolveUser(chatId);
        ChatConversation conv = conversationRepository.findByChatId(chatId)
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));

        if (conv.getUser() == null || !conv.getUser().getId().equals(user.getId())) {
            throw AiAssistantException.apiError("You do not have access to this conversation.");
        }

        if (conv.getStatus() == ConversationStatus.RESOLVED) {
            return "This conversation is already resolved.";
        }

        conv.setStatus(ConversationStatus.RESOLVED);
        conv.setResolvedAt(Instant.now(clock));
        conversationRepository.save(conv);

        ChatMessage msg = ChatMessage.builder()
                .conversation(conv)
                .role(ChatMessageRole.SYSTEM)
                .content("Conversation resolved.")
                .build();
        messageRepository.save(msg);

        chatMemoryConfig.clearMemory(chatId);

        return "Conversation resolved. Thank you for chatting with Shoppiq!";
    }

    /**
     * Performs semantic product search using vector embeddings.
     *
     * <p>This tool embeds the natural-language query using the local
     * BGE model and searches the Qdrant vector store for the most
     * semantically similar product text segments. Results are filtered
     * by optional category slug and maximum price constraints, and are
     * ranked by cosine similarity score (minimum 0.6). The returned
     * string includes product names, prices, and clickable links.</p>
     *
     * <p>Used by the AI model for vague or natural-language queries like
     * "comfortable running shoes", "gift for a photographer", or "laptop
     * for college".</p>
     *
     * @param query    natural-language description of what the user wants
     * @param category optional category slug to restrict results (e.g., "electronics")
     * @param maxPrice optional maximum price in USD
     * @param limit    number of results (default 5, max 10)
     * @return formatted product list or a "no results" message
     */
    @Tool("Semantic product search using vector embeddings. Use for vague or natural-language queries like 'comfortable running shoes', 'gift for a photographer', or 'laptop for college'. Returns the most relevant products with price and link.")
    public String semanticProductSearch(
            @P("Natural-language description of what the user wants") String query,
            @P(value = "Category slug to restrict results (e.g., 'electronics')", required = false) String category,
            @P(value = "Maximum price in USD", required = false) Double maxPrice,
            @P(value = "Number of results (default 5, max 10)", required = false) Integer limit) {

        int maxResults = Math.min(limit != null ? limit : 5, 10);

        Filter filter = buildSemanticFilter(category, maxPrice);

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(0.6)
                .filter(filter)
                .build();

        var matches = embeddingStore.search(request).matches();
        if (matches.isEmpty()) {
            return "No products found matching '" + query + "'.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(matches.size()).append(" relevant product(s):\n\n");
        for (var match : matches) {
            var meta = match.embedded().metadata();
            String name = meta.getString("name") != null ? meta.getString("name") : "Product";
            String slug = meta.getString("slug") != null ? meta.getString("slug") : "";
            Double priceVal = meta.getDouble("price");
            String price = priceVal != null ? "%.2f".formatted(priceVal) : "n/a";
            sb.append("- **").append(name).append("** — $").append(price);
            if (!slug.isBlank()) sb.append(" (/item/").append(slug).append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a vector-store filter for semantic product search.
     *
     * <p>Combines optional category and max-price constraints into a single
     * {@link Filter} suitable for {@link EmbeddingSearchRequest}. Uses
     * {@link Filter#and} when both constraints are present. Returns
     * {@code null} when no filters are specified, enabling unrestricted
     * search across the entire product catalog.</p>
     *
     * @param category optional category slug to restrict results
     * @param maxPrice optional maximum price in USD
     * @return the combined filter, or {@code null} if no filters are needed
     */
    private Filter buildSemanticFilter(String category, Double maxPrice) {
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasMaxPrice = maxPrice != null;

        if (hasCategory && hasMaxPrice) {
            return Filter.and(
                    new IsEqualTo("category", category),
                    new IsLessThanOrEqualTo("price", maxPrice)
            );
        }
        if (hasCategory) {
            return new IsEqualTo("category", category);
        }
        if (hasMaxPrice) {
            return new IsLessThanOrEqualTo("price", maxPrice);
        }
        return null;
    }

    /**
     * Resolves the authenticated user from the conversation's chat ID.
     *
     * <p>Looks up the conversation by its public chat ID and retrieves the
     * associated user. Throws {@link AiAssistantException} if the conversation
     * is a guest session (no user associated) and throws
     * {@link AiConversationNotFoundException} if the conversation does not
     * exist.</p>
     *
     * @param chatId the conversation's public identifier
     * @return the authenticated user
     * @throws AiAssistantException            if the conversation has no associated user
     * @throws AiConversationNotFoundException if the conversation does not exist
     */
    private User resolveUser(String chatId) {
        return conversationRepository.findByChatId(chatId)
                .map(conv -> {
                    if (conv.getUser() == null) {
                        throw AiAssistantException.apiError("This tool requires an authenticated user.");
                    }
                    return conv.getUser();
                })
                .orElseThrow(() -> AiConversationNotFoundException.chatId(chatId));
    }
}
