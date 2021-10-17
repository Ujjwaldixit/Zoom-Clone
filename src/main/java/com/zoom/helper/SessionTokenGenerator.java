package com.zoom.helper;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionTokenGenerator {

    private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPERCASE = CHAR_LOWERCASE.toUpperCase();
    private static final String DIGIT = "0123456789";
    private static final String OTHER_PUNCTUATION = "!@#&()–[{}]:;',?/*";
    private static final String OTHER_SYMBOL = "~$^+=<>";
    private static final String OTHER_SPECIAL = OTHER_PUNCTUATION + OTHER_SYMBOL;
    private static final String PASSWORD_ALLOW =
            CHAR_LOWERCASE + CHAR_UPPERCASE + DIGIT + OTHER_SPECIAL;
    private static final int PASSWORD_LENGTH = 100;
    private static final SecureRandom random = new SecureRandom();

    public  String generateToken() {

        StringBuilder result = new StringBuilder(PASSWORD_LENGTH);

        String strLowerCase = generateRandomString(CHAR_LOWERCASE, 10);
        System.out.format("%-20s: %s%n", "Chars (Lowercase)", strLowerCase);
        result.append(strLowerCase);

        String strUppercaseCase = generateRandomString(CHAR_UPPERCASE, 10);
        System.out.format("%-20s: %s%n", "Chars (Uppercase)", strUppercaseCase);
        result.append(strUppercaseCase);

        String strDigit = generateRandomString(DIGIT, 10);
        System.out.format("%-20s: %s%n", "Digits", strDigit);
        result.append(strDigit);

        String strSpecialChar = generateRandomString(OTHER_SPECIAL, 10);
        System.out.format("%-20s: %s%n", "Special chars", strSpecialChar);
        result.append(strSpecialChar);

        String strOther = generateRandomString(PASSWORD_ALLOW, PASSWORD_LENGTH);
        System.out.format("%-20s: %s%n", "Others", strOther);
        result.append(strOther);

        String password = result.toString();
        // combine all
        System.out.format("%-20s: %s%n", "Password", password);
        // shuffle again
        System.out.format("%-20s: %s%n", "Final Password", shuffleString(password));
        System.out.format("%-20s: %s%n%n", "Password Length", password.length());

        return password;
    }

    private  String generateRandomString(String input, int size) {

        if (input == null || input.length() <= 0)
            throw new IllegalArgumentException("Invalid input.");
        if (size < 1) throw new IllegalArgumentException("Invalid size.");

        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            // produce a random order
            int index = random.nextInt(input.length());
            result.append(input.charAt(index));
        }
        return result.toString();
    }

    public String shuffleString(String input) {
        List<String> result = Arrays.asList(input.split(""));
        Collections.shuffle(result);
        return result.stream().collect(Collectors.joining());
    }
}

