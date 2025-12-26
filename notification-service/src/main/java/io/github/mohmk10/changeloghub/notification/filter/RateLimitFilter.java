package io.github.mohmk10.changeloghub.notification.filter;

import io.github.mohmk10.changeloghub.notification.model.Notification;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFilter implements NotificationFilter {

    private final int maxNotificationsPerWindow;
    private final Duration windowDuration;
    private final boolean perChannel;
    private final boolean perApi;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxNotificationsPerWindow, Duration windowDuration) {
        this(maxNotificationsPerWindow, windowDuration, false, false);
    }

    public RateLimitFilter(int maxNotificationsPerWindow, Duration windowDuration,
                          boolean perChannel, boolean perApi) {
        this.maxNotificationsPerWindow = maxNotificationsPerWindow;
        this.windowDuration = windowDuration;
        this.perChannel = perChannel;
        this.perApi = perApi;
    }

    @Override
    public boolean shouldSend(Notification notification) {
        String bucketKey = getBucketKey(notification);
        RateLimitBucket bucket = buckets.computeIfAbsent(bucketKey,
            k -> new RateLimitBucket(maxNotificationsPerWindow, windowDuration));

        return bucket.tryAcquire();
    }

    private String getBucketKey(Notification notification) {
        StringBuilder key = new StringBuilder("global");

        if (perChannel) {
            
            String channel = notification.getTargetChannels().stream()
                .findFirst()
                .map(ChannelType::name)
                .orElse("all");
            key.append(":").append(channel);
        }

        if (perApi && notification.getEvent() != null) {
            String apiName = notification.getEvent().getApiName();
            if (apiName != null) {
                key.append(":").append(apiName);
            }
        }

        return key.toString();
    }

    @Override
    public String getFilterReason(Notification notification) {
        String bucketKey = getBucketKey(notification);
        RateLimitBucket bucket = buckets.get(bucketKey);
        if (bucket != null) {
            return String.format("Rate limit exceeded: %d/%d in %s (bucket: %s)",
                bucket.getCurrentCount(), maxNotificationsPerWindow,
                windowDuration, bucketKey);
        }
        return "Rate limit exceeded";
    }

    @Override
    public int getPriority() {
        return 5; 
    }

    public void reset() {
        buckets.clear();
    }

    public void reset(String bucketKey) {
        buckets.remove(bucketKey);
    }

    public int getCurrentCount(String bucketKey) {
        RateLimitBucket bucket = buckets.get(bucketKey);
        return bucket != null ? bucket.getCurrentCount() : 0;
    }

    public int getRemainingCapacity() {
        RateLimitBucket bucket = buckets.get("global");
        return bucket != null
            ? Math.max(0, maxNotificationsPerWindow - bucket.getCurrentCount())
            : maxNotificationsPerWindow;
    }

    public int getMaxNotificationsPerWindow() {
        return maxNotificationsPerWindow;
    }

    public Duration getWindowDuration() {
        return windowDuration;
    }

    public static RateLimitFilter perMinute(int max) {
        return new RateLimitFilter(max, Duration.ofMinutes(1));
    }

    public static RateLimitFilter perHour(int max) {
        return new RateLimitFilter(max, Duration.ofHours(1));
    }

    public static RateLimitFilter perChannelPerMinute(int max) {
        return new RateLimitFilter(max, Duration.ofMinutes(1), true, false);
    }

    private static class RateLimitBucket {
        private final int maxCount;
        private final long windowMillis;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart;

        RateLimitBucket(int maxCount, Duration windowDuration) {
            this.maxCount = maxCount;
            this.windowMillis = windowDuration.toMillis();
            this.windowStart = Instant.now().toEpochMilli();
        }

        synchronized boolean tryAcquire() {
            long now = Instant.now().toEpochMilli();

            if (now - windowStart >= windowMillis) {
                windowStart = now;
                count.set(0);
            }

            if (count.get() < maxCount) {
                count.incrementAndGet();
                return true;
            }

            return false;
        }

        int getCurrentCount() {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart >= windowMillis) {
                return 0; 
            }
            return count.get();
        }
    }
}
