package tech.elicase.minecraftosc.core.receiver;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * UDP OSC 接收器
 */
public final class OscReceiver {

    private static final int MAX_PACKET_SIZE = 65_507;

    private final int port;
    private final OscDatagramHandler datagramHandler;
    private final Logger logger;

    private volatile boolean running;
    private DatagramSocket socket;
    private Thread workerThread;

    public OscReceiver(int port, OscDatagramHandler datagramHandler, Logger logger) {
        this.port = port;
        this.datagramHandler = datagramHandler;
        this.logger = logger;
    }

    /**
     * 启动 UDP 监听线程
     */
    public synchronized void start() throws SocketException {
        if (running) {
            return;
        }

        socket = new DatagramSocket(port);
        socket.setReuseAddress(true);
        socket.setSoTimeout(1_000);

        running = true;
        workerThread = new Thread(this::listenLoop, "MinecraftOSC-Receiver");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    /**
     * 停止监听
     */
    public synchronized void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (workerThread != null) {
            try {
                workerThread.join(1_500);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void listenLoop() {
        byte[] buffer = new byte[MAX_PACKET_SIZE];
        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                byte[] payload = Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getOffset() + packet.getLength());
                datagramHandler.handleDatagram(payload, new InetSocketAddress(packet.getAddress(), packet.getPort()));
            } catch (SocketTimeoutException ignored) {
                // 轮询退出信号
            } catch (SocketException socketException) {
                if (running) {
                    logger.error("OSC 接收 Socket 异常", socketException);
                }
            } catch (IOException ioException) {
                logger.error("OSC 接收 I/O 异常", ioException);
            } catch (RuntimeException runtimeException) {
                logger.error("OSC 数据报处理失败", runtimeException);
            }
        }
    }
}
