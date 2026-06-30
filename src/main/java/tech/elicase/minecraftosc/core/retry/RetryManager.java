package tech.elicase.minecraftosc.core.retry;

import tech.elicase.minecraftosc.core.event.ChatEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * 管理失败消息的延迟重试
 */
public final class RetryManager {

    private final PriorityQueue<ScheduledRetry> retryQueue = new PriorityQueue<>(Comparator.comparingLong(ScheduledRetry::readyTick));
    private final int maxRetries;
    private final int retryDelayTicks;

    public RetryManager(int maxRetries, int retryDelayTicks) {
        this.maxRetries = maxRetries;
        this.retryDelayTicks = retryDelayTicks;
    }

    /**
     * 调度一次重试
     */
    public synchronized boolean schedule(ChatEvent event, long currentTick) {
        if (event.attempt() >= maxRetries) {
            return false;
        }
        retryQueue.add(new ScheduledRetry(currentTick + retryDelayTicks, event.nextAttempt()));
        return true;
    }

    /**
     * 取出当前 tick 可以执行的重试消息
     */
    public synchronized List<ChatEvent> drainReady(long currentTick, int maxCount) {
        List<ChatEvent> readyEvents = new ArrayList<>();
        while (!retryQueue.isEmpty() && readyEvents.size() < maxCount) {
            ScheduledRetry retry = retryQueue.peek();
            if (retry.readyTick() > currentTick) {
                break;
            }
            readyEvents.add(retryQueue.poll().event());
        }
        return readyEvents;
    }

    /**
     * 清空所有重试任务
     */
    public synchronized void clear() {
        retryQueue.clear();
    }

    private record ScheduledRetry(long readyTick, ChatEvent event) {
    }
}
