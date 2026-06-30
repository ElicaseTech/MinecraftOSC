package tech.elicase.minecraftosc.core.service;

import org.junit.jupiter.api.Test;
import tech.elicase.minecraftosc.core.event.ChatEvent;
import tech.elicase.minecraftosc.core.osc.OscMessage;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OscMessageHandlerTest {

    @Test
    void extractsFirstStringArgumentFromChatboxInput() {
        OscMessageHandler handler = new OscMessageHandler(Set.of("/chatbox/input"));

        List<ChatEvent> events = handler.toChatEvents(
                List.of(new OscMessage("/chatbox/input", List.of("Hello from VRChat", true, false))),
                new InetSocketAddress("127.0.0.1", 9000)
        );

        assertEquals(1, events.size());
        assertEquals("Hello from VRChat", events.get(0).content());
    }

    @Test
    void ignoresMessagesOutsideAcceptedAddresses() {
        OscMessageHandler handler = new OscMessageHandler(Set.of("/vrchat/chat"));

        List<ChatEvent> events = handler.toChatEvents(
                List.of(new OscMessage("/avatar/parameters/Test", List.of("Ignored"))),
                new InetSocketAddress("127.0.0.1", 9000)
        );

        assertTrue(events.isEmpty());
    }
}
