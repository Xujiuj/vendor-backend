package org.dromara.common.web.filter;

import org.dromara.common.redis.utils.RedisUtils;
import org.redisson.api.RateType;

import java.time.Duration;

/**
 * Redis-backed open-api protection store for clustered deployments.
 */
public class RedisOpenApiProtectionStore implements OpenApiProtectionStore {

    @Override
    public boolean tryAcquire(String key, int count, int time, int idleTimeout) {
        return RedisUtils.rateLimiter(key, RateType.OVERALL, count, time, idleTimeout) >= 0;
    }

    @Override
    public boolean putNonceIfAbsent(String key, Duration duration) {
        return RedisUtils.setObjectIfAbsent(key, "", duration);
    }

}
