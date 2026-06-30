package tech.elicase.minecraftosc.core.security;

import tech.elicase.minecraftosc.core.config.MinecraftOscSettings;
import tech.elicase.minecraftosc.core.event.ChatEvent;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 权限、过滤与速率限制
 */
public final class PermissionManager {

    private final MinecraftOscSettings settings;
    private final Map<String, Deque<Long>> rateLimiter = new ConcurrentHashMap<>();

    public PermissionManager(MinecraftOscSettings settings) {
        this.settings = settings;
    }

    /**
     * 过滤并校验消息
     */
    public PermissionDecision evaluate(ChatEvent event) {
        if (!settings.allowAnySender() && !settings.allowedSenders().contains(event.sourceAddress())) {
            return PermissionDecision.reject("来源地址未加入白名单");
        }

        if (!acquireRateLimit(event.sourceAddress(), Instant.now().toEpochMilli())) {
            return PermissionDecision.reject("消息发送过于频繁");
        }

        String sanitizedContent = sanitize(event.content());
        if (sanitizedContent.isBlank()) {
            return PermissionDecision.reject("消息内容为空");
        }

        return PermissionDecision.allow(event.withContent(sanitizedContent));
    }

    private boolean acquireRateLimit(String sourceAddress, long currentTimeMillis) {
        long windowStart = currentTimeMillis - (settings.rateLimitWindowSeconds() * 1_000L);
        Deque<Long> timestamps = rateLimiter.computeIfAbsent(sourceAddress, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= settings.rateLimitMessages()) {
                return false;
            }
            timestamps.addLast(currentTimeMillis);
            return true;
        }
    }

    private String sanitize(String rawContent) {
        String compactedWhitespace = rawContent
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim();

        StringBuilder sanitized = new StringBuilder(compactedWhitespace.length());
        boolean previousWhitespace = false;
        for (int index = 0; index < compactedWhitespace.length(); index++) {
            char current = compactedWhitespace.charAt(index);
            if (current == '\u00A7') {
                if (index + 1 < compactedWhitespace.length()) {
                    index++;
                }
                continue;
            }
            if (Character.isISOControl(current)) {
                continue;
            }
            if (Character.isWhitespace(current)) {
                if (previousWhitespace) {
                    continue;
                }
                previousWhitespace = true;
                sanitized.append(' ');
                continue;
            }
            previousWhitespace = false;
            sanitized.append(current);
        }

        String normalized = sanitized.toString().trim();
        if (normalized.startsWith("/")) {
            normalized = "'" + normalized;
        }
        if (normalized.length() > settings.maxMessageLength()) {
            normalized = normalized.substring(0, settings.maxMessageLength());
        }
        return normalized;
    }
}
