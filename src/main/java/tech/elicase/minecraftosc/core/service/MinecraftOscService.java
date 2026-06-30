package tech.elicase.minecraftosc.core.service;

import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;
import tech.elicase.minecraftosc.core.config.MinecraftOscSettings;
import tech.elicase.minecraftosc.core.event.ChatEvent;
import tech.elicase.minecraftosc.core.osc.OscMessage;
import tech.elicase.minecraftosc.core.osc.OscParseException;
import tech.elicase.minecraftosc.core.osc.OscParser;
import tech.elicase.minecraftosc.core.queue.MessageQueue;
import tech.elicase.minecraftosc.core.receiver.OscReceiver;
import tech.elicase.minecraftosc.core.retry.RetryManager;
import tech.elicase.minecraftosc.core.security.PermissionDecision;
import tech.elicase.minecraftosc.core.security.PermissionManager;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

/**
 * 管理 OSC 接收、解析和消息入队
 */
public final class MinecraftOscService {

    private final ConfigManager configManager;
    private final MinecraftOscSettings settings;
    private final Logger logger;
    private final OscParser parser = new OscParser();
    private final OscMessageHandler messageHandler;
    private final PermissionManager permissionManager;
    private final MessageQueue messageQueue;
    private final RetryManager retryManager;
    private final OscReceiver receiver;

    public MinecraftOscService(ConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.settings = configManager.current();
        this.logger = logger;
        this.messageHandler = new OscMessageHandler(settings.acceptedOscAddresses());
        this.permissionManager = new PermissionManager(settings);
        this.messageQueue = new MessageQueue(settings.maxQueueSize());
        this.retryManager = new RetryManager(settings.maxRetries(), settings.retryDelayTicks());
        this.receiver = new OscReceiver(settings.listenPort(), this::handleDatagram, logger);
    }

    /**
     * 启动 OSC 服务
     */
    public void start() throws SocketException {
        receiver.start();
        logger.info("MinecraftOSC 已开始监听 UDP 端口 {}", settings.listenPort());
    }

    /**
     * 停止服务并清理状态
     */
    public void stop() {
        receiver.stop();
        messageQueue.clear();
        retryManager.clear();
    }

    /**
     * 读取当前配置快照
     */
    public MinecraftOscSettings settings() {
        return settings;
    }

    /**
     * 释放到期的重试消息
     */
    public void releaseReadyRetries(long currentTick) {
        List<ChatEvent> readyRetries = retryManager.drainReady(currentTick, settings.maxDispatchPerTick());
        for (ChatEvent retryEvent : readyRetries) {
            if (!messageQueue.offer(retryEvent)) {
                logger.warn("重试队列回流失败，主队列已满: id={}", retryEvent.id());
            }
        }
    }

    /**
     * 取出一条待派发消息
     */
    public ChatEvent pollChatEvent() {
        return messageQueue.poll();
    }

    /**
     * 记录并安排一次重试
     */
    public boolean scheduleRetry(ChatEvent event, long currentTick, Throwable throwable) {
        boolean scheduled = retryManager.schedule(event, currentTick);
        if (scheduled) {
            logger.warn("聊天注入失败，已安排重试: id={}, attempt={}", event.id(), event.attempt() + 1, throwable);
        } else {
            logger.error("聊天注入失败且达到最大重试次数: id={}", event.id(), throwable);
        }
        return scheduled;
    }

    private void handleDatagram(byte[] data, InetSocketAddress remoteAddress) {
        try {
            List<OscMessage> messages = parser.parseMessages(data);
            for (ChatEvent rawEvent : messageHandler.toChatEvents(messages, remoteAddress)) {
                PermissionDecision decision = permissionManager.evaluate(rawEvent);
                if (!decision.allowed()) {
                    logger.warn("OSC 消息被拒绝: source={}, reason={}", rawEvent.sourceAddress(), decision.reason());
                    continue;
                }

                ChatEvent acceptedEvent = decision.acceptedEvent().orElseThrow();
                if (!messageQueue.offer(acceptedEvent)) {
                    logger.warn("OSC 消息队列已满，丢弃消息: id={}, source={}", acceptedEvent.id(), acceptedEvent.sourceAddress());
                    continue;
                }
                logger.info("OSC 消息已入队: id={}, source={}, address={}", acceptedEvent.id(), acceptedEvent.sourceAddress(), acceptedEvent.oscAddress());
            }
        } catch (OscParseException exception) {
            logger.warn("OSC 消息解析失败: source={}", remoteAddress, exception);
        }
    }
}
