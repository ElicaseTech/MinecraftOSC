package tech.elicase.minecraftosc.core.receiver;

import java.net.InetSocketAddress;

/**
 * 处理收到的 OSC UDP 数据报
 */
@FunctionalInterface
public interface OscDatagramHandler {

    /**
     * 处理单个数据报
     */
    void handleDatagram(byte[] data, InetSocketAddress remoteAddress);
}
