package com.ares.concurrency.connectionpool;

public record PoolConfigProperties(int minSize, int maxSize, long maxIdleTime,
    long validationInterval, long maxLifetime) {

}
