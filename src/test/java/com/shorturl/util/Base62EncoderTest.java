package com.shorturl.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Base62Encoder
 */
class Base62EncoderTest {

    @Test
    void testEncode_Zero() {
        String result = Base62Encoder.encode(0);
        assertEquals("0", result);
    }

    @Test
    void testEncode_SmallNumbers() {
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("9", Base62Encoder.encode(9));
        assertEquals("a", Base62Encoder.encode(10));
        assertEquals("z", Base62Encoder.encode(35));
        assertEquals("A", Base62Encoder.encode(36));
        assertEquals("Z", Base62Encoder.encode(61));
    }

    @Test
    void testEncode_LargeNumbers() {
        assertEquals("10", Base62Encoder.encode(62));
        assertEquals("19", Base62Encoder.encode(71));
        assertEquals("1a", Base62Encoder.encode(72));
    }

    @Test
    void testDecode_Zero() {
        long result = Base62Encoder.decode("0");
        assertEquals(0, result);
    }

    @Test
    void testDecode_SmallNumbers() {
        assertEquals(1, Base62Encoder.decode("1"));
        assertEquals(9, Base62Encoder.decode("9"));
        assertEquals(10, Base62Encoder.decode("a"));
        assertEquals(35, Base62Encoder.decode("z"));
        assertEquals(36, Base62Encoder.decode("A"));
        assertEquals(61, Base62Encoder.decode("Z"));
    }

    @Test
    void testDecode_LargeNumbers() {
        assertEquals(62, Base62Encoder.decode("10"));
        assertEquals(71, Base62Encoder.decode("19"));
        assertEquals(72, Base62Encoder.decode("1a"));
    }

    @Test
    void testEncodeDecode_Roundtrip() {
        long[] testNumbers = {0, 1, 62, 123, 456, 789, 1000, 10000, 100000, 1000000, Long.MAX_VALUE / 2};
        
        for (long number : testNumbers) {
            String encoded = Base62Encoder.encode(number);
            long decoded = Base62Encoder.decode(encoded);
            assertEquals(number, decoded, "Failed for number: " + number);
        }
    }

    @Test
    void testDecode_InvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            Base62Encoder.decode("abc@def");
        });
    }

    @Test
    void testIsValidBase62_ValidStrings() {
        assertTrue(Base62Encoder.isValidBase62("0"));
        assertTrue(Base62Encoder.isValidBase62("123"));
        assertTrue(Base62Encoder.isValidBase62("abc"));
        assertTrue(Base62Encoder.isValidBase62("ABC"));
        assertTrue(Base62Encoder.isValidBase62("123abcABC"));
    }

    @Test
    void testIsValidBase62_InvalidStrings() {
        assertFalse(Base62Encoder.isValidBase62(null));
        assertFalse(Base62Encoder.isValidBase62(""));
        assertFalse(Base62Encoder.isValidBase62("abc@def"));
        assertFalse(Base62Encoder.isValidBase62("123-456"));
        assertFalse(Base62Encoder.isValidBase62("hello world"));
        assertFalse(Base62Encoder.isValidBase62("test!"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 7, 10, 15})
    void testGenerateRandom_Length(int length) {
        String random = Base62Encoder.generateRandom(length);
        assertEquals(length, random.length());
        assertTrue(Base62Encoder.isValidBase62(random));
    }

    @Test
    void testGenerateRandom_Uniqueness() {
        String random1 = Base62Encoder.generateRandom(10);
        String random2 = Base62Encoder.generateRandom(10);
        
        // While not guaranteed, it's extremely unlikely they'll be the same
        assertNotEquals(random1, random2);
    }

    @Test
    void testEncode_ConsistentResults() {
        long testNumber = 123456789L;
        String encoded1 = Base62Encoder.encode(testNumber);
        String encoded2 = Base62Encoder.encode(testNumber);
        
        assertEquals(encoded1, encoded2);
    }

    @Test
    void testDecode_ConsistentResults() {
        String testString = "abc123XYZ";
        long decoded1 = Base62Encoder.decode(testString);
        long decoded2 = Base62Encoder.decode(testString);
        
        assertEquals(decoded1, decoded2);
    }
}