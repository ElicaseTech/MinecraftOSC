package tech.elicase.minecraftosc.core.platform;

/**
 * 平台层聊天广播接口
 */
@FunctionalInterface
public interface ChatBroadcaster {

    /**
     * 向 Minecraft 广播一条消息
     */
    void broadcast(String message);
}
