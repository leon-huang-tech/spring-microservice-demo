package com.demo.gateway;

import java.util.List;

import javax.crypto.SecretKey;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * FOR Spring WebFlux,BUT Spring MVC
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    /**
     * Signing Key(not the signature itself). openssl rand -base64 32
     * Signature(HMAC-SHA256(header.payload, secretKey)) = Header + Payload + secretKey
     * Header By {"alg": "HS256", "typ": "JWT"}
     * Payload By .subject(email).issuedAt(...).expiration(...)
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Defines a list of public URI paths that bypass JWT authentication
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/users/login",
            "/api/users/register",
            "/api/users/health",
            "/api/orders/health",
            "/api/ai/health",
            "/user-service/v3/api-docs",
            "/order-service/v3/api-docs",
            "/ai-service/v3/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        // Gets the URI path of the current incoming HTTP request
        String path = exchange.getRequest().getPath().toString();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            // Checks if the request path starts with any allowed public path; if so, lets it pass directly.
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // If the header is missing or does not start with "Bearer ", returns 401 Unauthorized and terminates the request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extracts the raw JWT token string by removing the "Bearer " prefix (7 characters).
        String token = authHeader.substring(7);

        try {
            // Parses and validates the JWT token using the signing key, then gets the payload claims.
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Mutates the request to inject a custom HTTP header X-User-Email containing the user's email extracted from the token.
            exchange.getRequest().mutate()
                    .header("X-User-Email", claims.getSubject())
                    .build();

            // Forwards the modified request to the next filter in the gateway chain.
            return chain.filter(exchange);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Sets the filter execution priority;
     * <br>
     * -1 ensures this filter runs before standard default gateway filters.
     */
    @Override
    public int getOrder() {
        return -1;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}