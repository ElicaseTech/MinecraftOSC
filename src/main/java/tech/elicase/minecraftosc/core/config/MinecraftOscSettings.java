package tech.elicase.minecraftosc.core.config;

import java.util.Objects;
import java.util.Set;

/**
 * 核心层运行配置
 */
public record MinecraftOscSettings(
        int listenPort,
        Set<String> acceptedOscAddresses,
        boolean allowAnySender,
        Set<String> allowedSenders,
        String chatPrefix,
        int maxMessageLength,
        int maxQueueSize,
        int maxRetries,
        int retryDelayTicks,
        int rateLimitMessages,
        int rateLimitWindowSeconds,
        int maxDispatchPerTick
) {

    public MinecraftOscSettings {
        acceptedOscAddresses = Set.copyOf(Objects.requireNonNull(acceptedOscAddresses, "acceptedOscAddresses"));
        allowedSenders = Set.copyOf(Objects.requireNonNull(allowedSenders, "allowedSenders"));
        chatPrefix = Objects.requireNonNull(chatPrefix, "chatPrefix");
    }
}
