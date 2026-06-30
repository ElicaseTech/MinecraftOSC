package tech.elicase.minecraftosc.platform.forge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import tech.elicase.minecraftosc.core.platform.ChatBroadcaster;

/**
 * Forge 聊天广播实现
 */
public final class ForgeChatBroadcaster implements ChatBroadcaster {

    private final MinecraftServer server;

    public ForgeChatBroadcaster(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void broadcast(String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
