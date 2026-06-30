package tech.elicase.minecraftosc.core.queue;

import tech.elicase.minecraftosc.core.event.ChatEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全消息队列
 */
public final class MessageQueue {

    private final ConcurrentLinkedQueue<ChatEvent> queue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger size = new AtomicInteger();
    private final int capacity;

    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 入队消息
     */
    public boolean offer(ChatEvent event) {
        Objects.requireNonNull(event, "event");
        while (true) {
            int currentSize = size.get();
            if (currentSize >= capacity) {
                return false;
            }
            if (size.compareAndSet(currentSize, currentSize + 1)) {
                queue.offer(event);
                return true;
            }
        }
    }

    /**
     * 出队消息
     */
    public ChatEvent poll() {
        ChatEvent event = queue.poll();
        if (event != null) {
            size.decrementAndGet();
        }
        return event;
    }

    /**
     * 当前队列大小
     */
    public int size() {
        return size.get();
    }

    /**
     * 清空队列
     */
    public void clear() {
        queue.clear();
        size.set(0);
    }
}
