package tech.elicase.minecraftosc.core.service;

import tech.elicase.minecraftosc.core.event.ChatEvent;
import tech.elicase.minecraftosc.core.osc.OscMessage;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 将 OSC Message 转换为内部 ChatEvent
 */
public final class OscMessageHandler {

    private final Set<String> acceptedAddresses;

    public OscMessageHandler(Set<String> acceptedAddresses) {
        this.acceptedAddresses = Set.copyOf(Objects.requireNonNull(acceptedAddresses, "acceptedAddresses"));
    }

    /**
     * 将消息列表转换为聊天事件
     */
    public List<ChatEvent> toChatEvents(List<OscMessage> messages, InetSocketAddress remoteAddress) {
        List<ChatEvent> events = new ArrayList<>();
        for (OscMessage message : messages) {
            if (!acceptedAddresses.contains(message.address())) {
                continue;
            }

            String content = extractContent(message.arguments());
            if (content.isBlank()) {
                continue;
            }

            events.add(ChatEvent.create(remoteAddress.getAddress().getHostAddress(), message.address(), content));
        }
        return events;
    }

    private String extractContent(List<Object> arguments) {
        for (Object argument : arguments) {
            if (argument instanceof String string && !string.isBlank()) {
                return string;
            }
        }

        StringBuilder fallback = new StringBuilder();
        for (Object argument : arguments) {
            if (argument == null || argument instanceof byte[]) {
                continue;
            }
            if (!fallback.isEmpty()) {
                fallback.append(' ');
            }
            fallback.append(argument);
        }
        return fallback.toString();
    }
}
