package com.pkmprojects.shoppiq.aiservice.tools;

import com.pkmprojects.shoppiq.aiservice.config.ChatMemoryConfig;
import com.pkmprojects.shoppiq.aiservice.entity.ChatConversation;
import com.pkmprojects.shoppiq.aiservice.entity.ChatMessage;
import com.pkmprojects.shoppiq.aiservice.enums.ChatMessageRole;
import com.pkmprojects.shoppiq.aiservice.enums.ConversationStatus;
import com.pkmprojects.shoppiq.aiservice.exception.AiAssistantException;
import com.pkmprojects.shoppiq.aiservice.repository.ChatConversationRepository;
import com.pkmprojects.shoppiq.aiservice.repository.ChatMessageRepository;
import com.pkmprojects.shoppiq.entity.Item;
import com.pkmprojects.shoppiq.entity.Order;
import com.pkmprojects.shoppiq.entity.ItemReview;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AiConversationNotFoundException;
import com.pkmprojects.shoppiq.repository.ItemRepository;
import com.pkmprojects.shoppiq.repository.OrderRepository;
import com.pkmprojects.shoppiq.repository.ItemReviewRepository;
import com.pkmprojects.shoppiq.service.CartService;
import com.pkmprojects.shoppiq.dto.response.CartResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * LangChain4j tool methods that give the AI assistant access to Shoppiq's
 * product catalog, orders, cart, and review data.
 *
 * <p>
 * Each {@code @Tool}-annotated method is auto-discovered by LangChain4j and
 * made available to the AI model for function calling. The model decides when
 * to invoke each tool based on the tool description and the user's request.
 *
 * <h2>Tools</h2>
 * <ul>
 *   <li>{@link #semanticProductSearch} — vector/semantic product search with optional category/price filters</li>
 *   <li>{@link #getProductDetail} — detailed product information by slug or name</li>
 *   <li>{@link #getOrderStatus} — recent order history for the authenticated user</li>
 *   <li>{@link #getCartContents} — current shopping cart contents</li>
 *   <li>{@link #getUserReviews} — product reviews written by the user</li>
 *   <li>{@link #resolveCurrentConversation} — resolve (close) the current conversation</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Tool methods are stateless — they delegate to existing repository/service beans</li>
 *   <li>The {@code @ToolMemoryId} parameter provides the chat ID, which is used to
 *       look up the owning user via {@link ChatConversationRepository}</li>
 *   <li>Guest users cannot use order, cart, or review tools (no authenticated user)</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(name = "shoppiq.ai.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ShoppiqTools {

    private static final Logger log = LoggerFactory.getLogger(ShoppiqTools.class);

    private final ItemRepository itemRepository;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ChatMemoryConfig chatMemoryConfig;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ItemReviewRepository reviewRepository;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @PostConstruct
    void logInit() {
        log.debug("[AI-INIT] ShoppiqTools initialised — itemRepo={}, orderRepo={}, cartService={}, reviewRepo={}",
                itemRepository != null ? "OK" : "NULL",
                orderRepository != null ? "OK" : "NULL",
                cartService != null ? "OK" : "NULL",
                reviewRepository != null ? "OK" : "NULL");
    }

    /**
     * Retrieves detailed information for a specific product by slug or name.
     *
     * <p>
     * First attempts an exact slug lookup, then falls back to a full-text
     * search if no slug matches. Returns SKU, price, discount, stock,
     * category, description, and URL.
     *
     * @param identifier the product slug (e.g., "wireless-headphones") or name
     * @return formatted product detail or a "not found" message
     */
    @Tool("Get detailed information about a specific product by name or slug. Use this when the user asks about a specific product's details, specs, or availability.")
    public String getProductDetail(
            @P("Product name or slug") String identifier) {

        Item item = itemRepository.findBySlug(identifier).orElse(null);

        if (item == null) {
            List<Item> candidates = itemRepository.findByNameContainingIgnoreCase(identifier, PageRequest.of(0, 1));
            if (candidates.isEmpty()) {
                return "No product found with identifier '" + identifier + "'.";
            }
            item = candidates.getFirst();
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
     * <p>
     * Each order includes its ID, status, grand total, and placement timestamp.
     * Results are limited to the most recent orders.
     *
     * @param orderNumber optional specific order number to filter by
     * @param limit       maximum number of orders to return (default 5)
     * @param chatId      the conversation ID, used to resolve the authenticated user
     * @return formatted order list or a "no orders" message
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
                return orderRepository.findById(orderId)
                        .filter(order -> order.getUser().getId().equals(user.getId()))
                        .map(order -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Order #").append(order.getId()).append("\n");
                            sb.append("Status: ").append(order.getStatus()).append("\n");
                            sb.append("Total: $").append(order.getGrandTotal()).append("\n");
                            sb.append("Placed: ").append(order.getPlacedAt()).append("\n");
                            return sb.toString();
                        })
                        .orElse("No order found with number '" + orderNumber + "'.");
            } catch (NumberFormatException e) {
                return "Invalid order number '" + orderNumber + "'. Please provide a numeric order ID.";
            }
        }

        List<Order> orders = orderRepository.findAllByUserOrderByPlacedAtDesc(user);

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
     * <p>
     * Includes item names, quantities, unit prices, and the cart subtotal.
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
     * <p>
     * Each review includes the product name, rating (out of 5), and the
     * review text.
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
        List<ItemReview> reviews = reviewRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

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
     * <p>
     * Marks the conversation as RESOLVED, records the resolution timestamp,
     * and appends a SYSTEM message to the conversation history.
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
        conv.setResolvedAt(java.time.Instant.now());
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
            String price = priceVal != null ? String.format("%.2f", priceVal) : "n/a";
            sb.append("- **").append(name).append("** — $").append(price);
            if (!slug.isBlank()) sb.append(" (/item/").append(slug).append(")");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Builds a vector-store filter for semantic product search.
     *
     * <p>
     * Combines optional category and max-price constraints into a single
     * {@link Filter} suitable for {@link EmbeddingSearchRequest}. Returns
     * {@code null} when no filters are specified (unrestricted search).
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
     * <p>
     * Looks up the conversation by its public chat ID, then retrieves the
     * associated user. Throws if the conversation is a guest session (no user).
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
