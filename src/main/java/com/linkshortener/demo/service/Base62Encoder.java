package com.linkshortener.demo.service;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    public String encode (long value) {
        if (value == 0 ) {
            return String.valueOf(ALPHABET.charAt(0));

        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = (int)(value % BASE);
            sb.append(ALPHABET.charAt(remainder));
            value /= BASE;
        }

        return sb.reverse().toString();
    }

    public long decode (String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++){
            num = num * BASE + ALPHABET.indexOf(str.charAt(i));
        }

        return num;
    }
    
}
