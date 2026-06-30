package tech.elicase.minecraftosc.platform.forge;

import net.minecraft.client.multiplayer.ClientPacketListener;
import tech.elicase.minecraftosc.core.platform.ChatBroadcaster;

import java.util.Objects;

/**
 * Forge 客户端聊天发送实现
 */
public final class ForgeChatBroadcaster implements ChatBroadcaster {

    private final ClientPacketListener connection;

    public ForgeChatBroadcaster(ClientPacketListener connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public void broadcast(String message) {
        connection.sendChat(message);
    }
}
