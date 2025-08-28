package com.shorturl.util;

/**
 * Base62 encoder utility for generating short URLs
 * 
 * Base62 uses characters: 0-9, a-z, A-Z (62 characters total)
 * This provides a good balance between URL length and readability
 */
public class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();

    /**
     * Encode a long number to Base62 string
     * 
     * @param number the number to encode
     * @return Base62 encoded string
     */
    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_CHARS.charAt(0));
        }

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            result.append(BASE62_CHARS.charAt((int) (number % BASE)));
            number /= BASE;
        }

        return result.reverse().toString();
    }

    /**
     * Decode a Base62 string to long number
     * 
     * @param encoded the Base62 encoded string
     * @return decoded long number
     */
    public static long decode(String encoded) {
        long result = 0;
        long power = 1;

        for (int i = encoded.length() - 1; i >= 0; i--) {
            char c = encoded.charAt(i);
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + c);
            }
            result += index * power;
            power *= BASE;
        }

        return result;
    }

    /**
     * Check if a string is valid Base62
     * 
     * @param str the string to check
     * @return true if valid Base62
     */
    public static boolean isValidBase62(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (BASE62_CHARS.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Generate a random Base62 string of specified length
     * 
     * @param length the desired length
     * @return random Base62 string
     */
    public static String generateRandom(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * BASE);
            result.append(BASE62_CHARS.charAt(randomIndex));
        }
        return result.toString();
    }
}