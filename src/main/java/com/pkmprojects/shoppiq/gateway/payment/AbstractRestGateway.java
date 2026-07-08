package com.pkmprojects.shoppiq.gateway.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkmprojects.shoppiq.exception.PaymentGatewayException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

abstract class AbstractRestGateway implements PaymentGatewayStrategy {

    protected final RestClient restClient;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;
    protected final String apiKey;
    protected final String apiSecret;

    protected AbstractRestGateway(RestClient.Builder restClientBuilder,
                                  ObjectMapper objectMapper,
                                  String baseUrl,
                                  String apiKey,
                                  String apiSecret) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    protected abstract String gatewayName();

    protected String exchange(HttpMethod method, String path, Object body, Consumer<HttpHeaders> auth) {
        try {
            RestClient.RequestBodySpec spec = restClient.method(method)
                    .uri(expand(path))
                    .headers(auth);
            if (body != null) {
                spec = spec.body(body);
            }
            return applyErrorHandling(spec.retrieve()).body(String.class);
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    protected String exchangeForm(HttpMethod method, String path, String formBody, Consumer<HttpHeaders> auth) {
        try {
            return applyErrorHandling(restClient.method(method)
                    .uri(expand(path))
                    .headers(auth)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()).body(String.class);
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    private RestClient.ResponseSpec applyErrorHandling(RestClient.ResponseSpec spec) {
        return spec.onStatus(status -> status.isError(), (request, response) -> {
            byte[] bytes = response.getBody().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8);
            throw PaymentGatewayException.ofResponse(
                    gatewayName(), response.getStatusCode().value(), body);
        });
    }

    protected JsonNode parse(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            throw PaymentGatewayException.of(gatewayName(), ex);
        }
    }

    protected String expand(String path) {
        return baseUrl.replaceAll("/+$", "") + path;
    }

    protected long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    protected Consumer<HttpHeaders> basicAuth(String user, String secret) {
        return headers -> headers.setBasicAuth(user, secret);
    }

    protected Consumer<HttpHeaders> bearer(String token) {
        return headers -> headers.setBearerAuth(token);
    }
}
