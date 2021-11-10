package ru.afek.auth.utils;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

import java.net.InetAddress;

import com.google.common.cache.Cache;

/**
 * @author Afek
 */

public class BlackListIp {

    private static Cache<InetAddress, Integer> connections = CacheBuilder.newBuilder().concurrencyLevel(Runtime.getRuntime().availableProcessors()).initialCapacity(100).expireAfterWrite(10L, TimeUnit.MINUTES).build();

    public static void IncreaseOrAdd(final InetAddress address) {
        final Integer numOfCon = BlackListIp.connections.getIfPresent(address);
        if (numOfCon != null && numOfCon >= 3)
            return;
        BlackListIp.connections.put(address, (numOfCon == null) ? 1 : (numOfCon + 1));
    }

    public static boolean isManyChecks(final InetAddress address) {
        final Integer numOfCon = BlackListIp.connections.getIfPresent(address);
        return numOfCon != null && numOfCon >= 3;
    }

    public static void clear() {
        BlackListIp.connections.invalidateAll();
    }

    public static void cleanUP() {
        BlackListIp.connections.cleanUp();
    }
}
