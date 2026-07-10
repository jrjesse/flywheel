package com.antigravity.sales.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int apiLimit;
    private final int webhookLimit;

    public RateLimitFilter(
            @Value("${app.security.rate-limit.requests-per-minute}") int apiLimit,
            @Value("${app.security.rate-limit.webhook-requests-per-minute}") int webhookLimit) {
        this.apiLimit = apiLimit;
        this.webhookLimit = webhookLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean webhook = path.startsWith("/api/webhooks/");
        int limit = webhook ? webhookLimit : apiLimit;
        String key = resolveKey(request, webhook);

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket(limit));
        if (!bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request, boolean webhook) {
        String ip = request.getRemoteAddr();
        if (!webhook) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                return "user:" + auth.substring(7, Math.min(auth.length(), 40));
            }
        }
        return "ip:" + ip + ":" + (webhook ? "webhook" : "api");
    }

    private Bucket newBucket(int limitPerMinute) {
        Bandwidth limit = Bandwidth.classic(limitPerMinute, Refill.greedy(limitPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
