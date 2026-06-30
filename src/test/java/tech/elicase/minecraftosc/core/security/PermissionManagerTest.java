package tech.elicase.minecraftosc.core.security;

import org.junit.jupiter.api.Test;
import tech.elicase.minecraftosc.core.config.MinecraftOscSettings;
import tech.elicase.minecraftosc.core.event.ChatEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionManagerTest {

    @Test
    void rejectsMessagesFromUnknownSenders() {
        PermissionManager permissionManager = new PermissionManager(settings());

        PermissionDecision decision = permissionManager.evaluate(ChatEvent.create("192.168.0.2", "/vrchat/chat", "Hello"));

        assertFalse(decision.allowed());
        assertEquals("来源地址未加入白名单", decision.reason());
    }

    @Test
    void sanitizesAndEscapesCommandLikeContent() {
        PermissionManager permissionManager = new PermissionManager(settings());

        PermissionDecision decision = permissionManager.evaluate(ChatEvent.create("127.0.0.1", "/vrchat/chat", "/say hi\n\t\u00A7a"));

        assertTrue(decision.allowed());
        assertEquals("'/say hi", decision.acceptedEvent().orElseThrow().content());
    }

    @Test
    void enforcesRateLimitWindow() {
        MinecraftOscSettings settings = new MinecraftOscSettings(
                9000,
                Set.of("/vrchat/chat"),
                false,
                Set.of("127.0.0.1"),
                "[VRChat] ",
                256,
                10,
                2,
                20,
                2,
                60,
                8
        );
        PermissionManager permissionManager = new PermissionManager(settings);

        assertTrue(permissionManager.evaluate(ChatEvent.create("127.0.0.1", "/vrchat/chat", "one")).allowed());
        assertTrue(permissionManager.evaluate(ChatEvent.create("127.0.0.1", "/vrchat/chat", "two")).allowed());
        assertFalse(permissionManager.evaluate(ChatEvent.create("127.0.0.1", "/vrchat/chat", "three")).allowed());
    }

    private static MinecraftOscSettings settings() {
        return new MinecraftOscSettings(
                9000,
                Set.of("/vrchat/chat", "/chatbox/input"),
                false,
                Set.of("127.0.0.1"),
                "[VRChat] ",
                256,
                10,
                2,
                20,
                8,
                5,
                8
        );
    }
}
