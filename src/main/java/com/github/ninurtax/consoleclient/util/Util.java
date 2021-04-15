package com.github.ninurtax.consoleclient.util;

public class Util {
    public static boolean isOnDelay(long lastTimeTriggered, long delay) {
        return System.currentTimeMillis() - lastTimeTriggered < delay*1000;
    }
}
