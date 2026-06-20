package org.dromara.common.web.filter;

import java.time.Duration;

/**
 * Backing store for open-api throttling and replay protection.
 */
public interface OpenApiProtectionStore {

    boolean tryAcquire(String key, int count, int time, int idleTimeout);

    boolean putNonceIfAbsent(String key, Duration duration);

}
