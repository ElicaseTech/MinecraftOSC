package tech.elicase.minecraftosc.platform.forge;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;
import tech.elicase.minecraftosc.core.event.ChatEvent;
import tech.elicase.minecraftosc.core.service.MinecraftOscService;

import java.net.SocketException;

/**
 * Forge 生命周期适配器
 */
public final class ForgeAdapter {

    private final ConfigManager configManager;
    private final Logger logger;

    private MinecraftOscService oscService;
    private MinecraftServer server;
    private long serverTick;

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
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        startService();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        stopService();
        server = null;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || oscService == null || server == null) {
            return;
        }

        serverTick++;
        oscService.releaseReadyRetries(serverTick);

        ForgeChatBroadcaster broadcaster = new ForgeChatBroadcaster(server);
        int dispatched = 0;
        while (dispatched < oscService.settings().maxDispatchPerTick()) {
            ChatEvent chatEvent = oscService.pollChatEvent();
            if (chatEvent == null) {
                break;
            }

            try {
                broadcaster.broadcast(oscService.settings().chatPrefix() + chatEvent.content());
                logger.info("聊天消息已注入 Minecraft: id={}, source={}", chatEvent.id(), chatEvent.sourceAddress());
            } catch (RuntimeException exception) {
                oscService.scheduleRetry(chatEvent, serverTick, exception);
            }
            dispatched++;
        }
    }

    private void startService() {
        stopService();
        serverTick = 0L;
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
