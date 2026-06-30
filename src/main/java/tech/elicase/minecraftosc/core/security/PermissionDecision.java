package tech.elicase.minecraftosc.core.security;

import tech.elicase.minecraftosc.core.event.ChatEvent;

import java.util.Optional;

/**
 * 权限检查结果
 */
public record PermissionDecision(Optional<ChatEvent> acceptedEvent, String reason) {

    public static PermissionDecision allow(ChatEvent event) {
        return new PermissionDecision(Optional.of(event), "");
    }

    public static PermissionDecision reject(String reason) {
        return new PermissionDecision(Optional.empty(), reason);
    }

    public boolean allowed() {
        return acceptedEvent.isPresent();
    }
}
