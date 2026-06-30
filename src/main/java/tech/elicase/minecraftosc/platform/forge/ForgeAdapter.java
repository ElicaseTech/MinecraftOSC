package tech.elicase.minecraftosc.platform.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;
import tech.elicase.minecraftosc.core.event.ChatEvent;
import tech.elicase.minecraftosc.core.service.MinecraftOscService;

import java.net.SocketException;

/**
 * Forge 生命周期适配器
 */
public final class ForgeAdapter implements ReloadableAdapter {

    private final ConfigManager configManager;
    private final Logger logger;

    private MinecraftOscService oscService;
    private long clientTick;

    public ForgeAdapter(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * 配置变更时重启服务
     */
    public void reload() {
        if (oscService == null) {
            return;
        }

        logger.info("MinecraftOSC 配置已变更，正在重启 OSC 服务");
        stopService();
        startService();
    }

    @SubscribeEvent
    public void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        startService();
    }

    @SubscribeEvent
    public void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        stopService();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || oscService == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (minecraft.player == null || connection == null) {
            return;
        }

        clientTick++;
        oscService.releaseReadyRetries(clientTick);

        ForgeChatBroadcaster broadcaster = new ForgeChatBroadcaster(connection);
        int dispatched = 0;
        while (dispatched < oscService.settings().maxDispatchPerTick()) {
            ChatEvent chatEvent = oscService.pollChatEvent();
            if (chatEvent == null) {
                break;
            }

            try {
                broadcaster.broadcast(oscService.settings().chatPrefix() + chatEvent.content());
                logger.info("聊天消息已通过本地玩家发送: id={}, source={}", chatEvent.id(), chatEvent.sourceAddress());
            } catch (RuntimeException exception) {
                oscService.scheduleRetry(chatEvent, clientTick, exception);
            }
            dispatched++;
        }
    }

    private void startService() {
        stopService();
        clientTick = 0L;
        oscService = new MinecraftOscService(configManager, logger);
        try {
            oscService.start();
        } catch (SocketException socketException) {
            logger.error("MinecraftOSC 启动失败，无法监听 UDP 端口 {}", configManager.current().listenPort(), socketException);
            oscService = null;
        }
    }

    private void stopService() {
        if (oscService == null) {
            return;
        }
        oscService.stop();
        oscService = null;
    }
}
