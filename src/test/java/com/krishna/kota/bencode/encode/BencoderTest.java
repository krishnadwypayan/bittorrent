package com.krishna.kota.bencode.encode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Bencoder Tests")
class BencoderTest {

    private Bencoder bencoder;

    @BeforeEach
    void setUp() {
        bencoder = new Bencoder();
    }

    /**
     * Helper method to assert the bencoded output.
     * It compares the byte array result with the expected string's UTF-8 bytes.
     *
     * @param input    The object to encode.
     * @param expected The expected Bencoded string.
     * @throws IOException If the encoder fails.
     */
    private void assertBencoded(Object input, String expected) throws IOException {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = bencoder.encode(input);

        assertArrayEquals(expectedBytes, actualBytes,
                () -> String.format("Bencoding failed for input '%s'. Expected: '%s', Actual: '%s'",
                        input,
                        new String(expectedBytes, StandardCharsets.UTF_8),
                        new String(actualBytes, StandardCharsets.UTF_8)
                )
        );
    }

    @Nested
    @DisplayName("Byte Array Encoding")
    class ByteArrayEncoding {
        @Test
        @DisplayName("should encode a simple byte array")
        void testEncodeByteArray() throws IOException {
            // A sample 20-byte SHA-1 hash
            byte[] hash = new byte[]{
                    (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90,
                    (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x12, (byte) 0x34,
                    (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd,
                    (byte) 0xef, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78
            };

            // Expected output: "20:" followed by the 20 raw bytes
            ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
            expectedStream.write("20:".getBytes(StandardCharsets.US_ASCII));
            expectedStream.write(hash);
            byte[] expectedBytes = expectedStream.toByteArray();

            byte[] actualBytes = bencoder.encode(hash);

            assertArrayEquals(expectedBytes, actualBytes);
        }

        @Test
        @DisplayName("should encode an empty byte array")
        void testEncodeEmptyByteArray() throws IOException {
            assertBencoded(new byte[0], "0:");
        }
    }


    @Nested
    @DisplayName("String Encoding")
    class StringEncoding {
        @Test
        @DisplayName("should encode a simple string")
        void testEncodeSimpleString() throws IOException {
            assertBencoded("spam", "4:spam");
        }

        @Test
        @DisplayName("should encode an empty string")
        void testEncodeEmptyString() throws IOException {
            assertBencoded("", "0:");
        }

        @Test
        @DisplayName("should encode a string containing numbers and symbols")
        void testEncodeComplexString() throws IOException {
            assertBencoded("hello-world:123", "15:hello-world:123");
        }
    }

    @Nested
    @DisplayName("Integer Encoding")
    class IntegerEncoding {
        @Test
        @DisplayName("should encode a positive integer")
        void testEncodePositiveInteger() throws IOException {
            assertBencoded(42, "i42e");
        }

        @Test
        @DisplayName("should encode a negative integer")
        void testEncodeNegativeInteger() throws IOException {
            assertBencoded(-42, "i-42e");
        }

        @Test
        @DisplayName("should encode zero")
        void testEncodeZero() throws IOException {
            assertBencoded(0, "i0e");
        }

        @Test
        @DisplayName("should encode a large integer value")
        void testEncodeLargeInteger() throws IOException {
            assertBencoded(Integer.MAX_VALUE, "i2147483647e");
        }
    }

    @Nested
    @DisplayName("List Encoding")
    class ListEncoding {
        @Test
        @DisplayName("should encode a list of strings")
        void testEncodeListOfStrings() throws IOException {
            assertBencoded(List.of("spam", "eggs"), "l4:spam4:eggse");
        }

        @Test
        @DisplayName("should encode a list of integers")
        void testEncodeListOfIntegers() throws IOException {
            assertBencoded(List.of(10, -20, 30), "li10ei-20ei30ee");
        }

        @Test
        @DisplayName("should encode a list with mixed types")
        void testEncodeMixedList() throws IOException {
            assertBencoded(List.of("spam", 42), "l4:spami42ee");
        }

        @Test
        @DisplayName("should encode an empty list")
        void testEncodeEmptyList() throws IOException {
            assertBencoded(Collections.emptyList(), "le");
        }

        @Test
        @DisplayName("should encode a nested list")
        void testEncodeNestedList() throws IOException {
            List<Object> nestedList = List.of("a", List.of("b", "c"));
            assertBencoded(nestedList, "l1:al1:b1:cee");
        }
    }

    @Nested
    @DisplayName("Map (Dictionary) Encoding")
    class MapEncoding {
        @Test
        @DisplayName("should encode a map and sort keys lexicographically")
        void testEncodeMapWithSortedKeys() throws IOException {
            // Use LinkedHashMap to show that the encoder sorts the keys, not the input map.
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("spam", "eggs");
            map.put("cow", "moo"); // "cow" should come before "spam"
            assertBencoded(map, "d3:cow3:moo4:spam4:eggse");
        }

        @Test
        @DisplayName("should encode a map with mixed value types")
        void testEncodeMapWithMixedValues() throws IOException {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("publisher", "bob");
            map.put("age", 25); // "age" should come before "publisher"
            assertBencoded(map, "d3:agei25e9:publisher3:bobe");
        }

        @Test
        @DisplayName("should encode an empty map")
        void testEncodeEmptyMap() throws IOException {
            assertBencoded(Collections.emptyMap(), "de");
        }

        @Test
        @DisplayName("should encode a complex nested structure")
        void testEncodeComplexNestedMap() throws IOException {
            Map<String, Object> infoMap = new LinkedHashMap<>();
            infoMap.put("length", 1024);
            infoMap.put("files", List.of("part1.dat", "part2.dat"));

            Map<String, Object> torrentMap = new LinkedHashMap<>();
            torrentMap.put("announce", "http://tracker.example.com");
            torrentMap.put("info", infoMap);

            // Expected sorted order: "announce", "info"
            // Expected sorted order in info: "files", "length"
            String expected = "d8:announce26:http://tracker.example.com4:infod5:filesl9:part1.dat9:part2.date6:lengthi1024eee";
            assertBencoded(torrentMap, expected);
        }
    }

    @Nested
    @DisplayName("Error and Edge Case Handling")
    class ErrorHandling {
        @Test
        @DisplayName("should throw IOException for unsupported data types")
        void testEncodeUnsupportedType() {
            // A Double is not a supported type in the Bencoder implementation.
            Double unsupportedData = 123.45;
            IOException exception = assertThrows(IOException.class, () -> bencoder.encode(unsupportedData));
            assert(exception.getMessage().contains("Data type not supported"));
        }

        @Test
        @DisplayName("should throw IOException for null input")
        void testEncodeNull() {
            // Null is not an instance of any supported type.
            IOException exception = assertThrows(IOException.class, () -> bencoder.encode(null));
            assert(exception.getMessage().contains("Data type not supported"));
        }

        @Test
        @DisplayName("should throw ClassCastException for map with non-string keys")
        void testMapWithNonStringKeys() {
            // This test exposes a potential bug in Bencoder.java where it unsafely casts
            // Map<?,?> to Map<String, Object>, which will cause a ClassCastException at runtime.
            Map<Integer, String> mapWithIntKeys = Map.of(1, "one");
            assertThrows(IOException.class, () -> bencoder.encode(mapWithIntKeys));
        }
    }
}
