package ru.job4j.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BaseConversion {

    private static final String ALLOWED_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final Random RANDOM = new Random();

    public String encode(long input) {
        StringBuilder sb = new StringBuilder("_____");

        int digitPos = RANDOM.nextInt(5);
        sb.setCharAt(digitPos, DIGITS.charAt(RANDOM.nextInt(10)));

        for (int i = 0; i < 5; i++) {
            if (i != digitPos) {
                sb.setCharAt(i, ALLOWED_STRING.charAt(RANDOM.nextInt(ALLOWED_STRING.length())));
            }
        }

        return sb.toString();
    }
}
