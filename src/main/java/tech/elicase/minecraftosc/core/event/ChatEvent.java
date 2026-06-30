package tech.elicase.minecraftosc.core.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 内部聊天事件
 */
public record ChatEvent(
        UUID id,
        String sourceAddress,
        String oscAddress,
        String content,
        int attempt,
        Instant receivedAt
) {

    public ChatEvent {
        id = Objects.requireNonNull(id, "id");
        sourceAddress = Objects.requireNonNull(sourceAddress, "sourceAddress");
        oscAddress = Objects.requireNonNull(oscAddress, "oscAddress");
        content = Objects.requireNonNull(content, "content");
        receivedAt = Objects.requireNonNull(receivedAt, "receivedAt");
    }

    /**
     * 创建首次入队的聊天事件
     */
    public static ChatEvent create(String sourceAddress, String oscAddress, String content) {
        return new ChatEvent(UUID.randomUUID(), sourceAddress, oscAddress, content, 0, Instant.now());
    }

    /**
     * 返回增加重试次数后的副本
     */
    public ChatEvent nextAttempt() {
        return new ChatEvent(id, sourceAddress, oscAddress, content, attempt + 1, receivedAt);
    }

    /**
     * 返回替换内容后的副本
     */
    public ChatEvent withContent(String newContent) {
        return new ChatEvent(id, sourceAddress, oscAddress, newContent, attempt, receivedAt);
    }
}
