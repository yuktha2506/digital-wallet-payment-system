package com.example.wallet.util;

import java.security.SecureRandom;

public final class IdGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private IdGenerator() {
    }

    public static String walletNumber() {
        return "WALLET" + random(10);
    }

    public static String transactionId() {
        return "TXN" + System.currentTimeMillis() + random(6);
    }

    private static String random(int length) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return value.toString();
    }
}
