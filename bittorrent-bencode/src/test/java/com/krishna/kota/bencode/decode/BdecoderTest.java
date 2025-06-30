package com.krishna.kota.bencode.decode;

import com.krishna.kota.bencode.type.BencodeElement;
import com.krishna.kota.bencode.type.BencodeInteger;
import com.krishna.kota.bencode.type.BencodeList;
import com.krishna.kota.bencode.type.BencodeMap;
import com.krishna.kota.bencode.type.BencodeString;
import com.krishna.kota.bencode.type.impl.BencInteger;
import com.krishna.kota.bencode.type.impl.BencString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bdecoder Tests")
class BdecoderTest {

    private Bdecoder bdecoder;

    @BeforeEach
    void setUp() {
        bdecoder = new Bdecoder();
    }

    private BencodeElement decodeString(String input) throws IOException {
        return bdecoder.decode(input.getBytes(StandardCharsets.UTF_8));
    }

    @Nested
    @DisplayName("String Decoding")
    class StringDecoding {
        @Test
        @DisplayName("should decode a simple string")
        void testDecodeSimpleString() throws IOException {
            BencodeString result = (BencodeString) decodeString("4:spam");
            assertArrayEquals("spam".getBytes(StandardCharsets.UTF_8), result.getValue());
        }

        @Test
        @DisplayName("should decode an empty string")
        void testDecodeEmptyString() throws IOException {
            BencodeString result = (BencodeString) decodeString("0:");
            assertEquals(0, result.getValue().length);
        }

        @Test
        @DisplayName("should throw IOException for string with negative length")
        void testDecodeStringNegativeLength() {
            assertThrows(IOException.class, () -> decodeString("-1:a"));
        }
    }

    @Nested
    @DisplayName("Integer Decoding")
    class IntegerDecoding {
        @Test
        @DisplayName("should decode a positive integer")
        void testDecodePositiveInteger() throws IOException {
            BencodeInteger result = (BencodeInteger) decodeString("i42e");
            assertEquals(BigInteger.valueOf(42), result.bigIntegerValue());
        }

        @Test
        @DisplayName("should decode a negative integer")
        void testDecodeNegativeInteger() throws IOException {
            BencodeInteger result = (BencodeInteger) decodeString("i-42e");
            assertEquals(BigInteger.valueOf(-42), result.bigIntegerValue());
        }

        @Test
        @DisplayName("should decode zero")
        void testDecodeZero() throws IOException {
            BencodeInteger result = (BencodeInteger) decodeString("i0e");
            assertEquals(BigInteger.ZERO, result.bigIntegerValue());
        }

        @Test
        @DisplayName("should decode a large integer (BigInteger)")
        void testDecodeLargeInteger() throws IOException {
            String largeNum = "9223372036854775808"; // Long.MAX_VALUE + 1
            BencodeInteger result = (BencodeInteger) decodeString("i" + largeNum + "e");
            assertEquals(new BigInteger(largeNum), result.bigIntegerValue());
        }

        @Test
        @DisplayName("should throw IOException for integer with leading zero")
        void testDecodeIntegerWithLeadingZero() {
            assertThrows(IOException.class, () -> decodeString("i03e"));
        }

        @Test
        @DisplayName("should throw IOException for negative zero")
        void testDecodeNegativeZero() {
            assertThrows(IOException.class, () -> decodeString("i-0e"));
        }

        @Test
        @DisplayName("should throw IOException for empty integer")
        void testDecodeEmptyInteger() {
            assertThrows(IOException.class, () -> decodeString("ie"));
        }
    }

    @Nested
    @DisplayName("List Decoding")
    class ListDecoding {
        @Test
        @DisplayName("should decode a list of strings")
        void testDecodeListOfStrings() throws IOException {
            BencodeList result = (BencodeList) decodeString("l4:spam4:eggse");
            assertEquals(2, result.size());
            assertEquals("spam", ((BencodeString) result.get(0)).asString(StandardCharsets.UTF_8));
            assertEquals("eggs", ((BencodeString) result.get(1)).asString(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("should decode a list with mixed types")
        void testDecodeMixedList() throws IOException {
            BencodeList result = (BencodeList) decodeString("l4:spami42ee");
            assertEquals(2, result.size());
            assertInstanceOf(BencodeString.class, result.get(0));
            assertInstanceOf(BencodeInteger.class, result.get(1));
            assertEquals(new BencInteger(BigInteger.valueOf(42)), result.get(1));
        }

        @Test
        @DisplayName("should decode an empty list")
        void testDecodeEmptyList() throws IOException {
            BencodeList result = (BencodeList) decodeString("le");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should decode a nested list")
        void testDecodeNestedList() throws IOException {
            BencodeList result = (BencodeList) decodeString("l4:iteml1:a1:bee");
            assertEquals(2, result.size());
            assertInstanceOf(BencodeString.class, result.get(0));
            assertInstanceOf(BencodeList.class, result.get(1));

            BencodeList nestedList = (BencodeList) result.get(1);
            assertEquals(2, nestedList.size());
            assertEquals("a", ((BencodeString) nestedList.get(0)).asString(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("should throw IOException for unterminated list")
        void testUnterminatedList() {
            assertThrows(IOException.class, () -> decodeString("l4:spam"));
        }
    }

    @Nested
    @DisplayName("Map (Dictionary) Decoding")
    class MapDecoding {
        @Test
        @DisplayName("should decode a simple map")
        void testDecodeSimpleMap() throws IOException {
            Map<BencodeString, BencodeElement> map = (BencodeMap) decodeString("d3:cow3:moo4:spam4:eggse");
            assertEquals(2, map.size());

            BencodeString key1 = new BencString("cow");
            BencodeString val1 = new BencString("moo");
            assertEquals(val1, map.get(key1));

            BencodeString key2 = new BencString("spam".getBytes());
            BencodeString val2 = new BencString("eggs".getBytes());
            assertEquals(val2, map.get(key2));
        }

        @Test
        @DisplayName("should decode a map with mixed value types")
        void testDecodeMapWithMixedValues() throws IOException {
            Map<BencodeString, BencodeElement> map = (BencodeMap) decodeString("d9:publisher3:bob3:agei25ee");
            assertEquals(2, map.size());
            assertInstanceOf(BencodeString.class, map.get(new BencString("publisher")));
            assertInstanceOf(BencodeInteger.class, map.get(new BencString("age")));
        }

        @Test
        @DisplayName("should decode an empty map")
        void testDecodeEmptyMap() throws IOException {
            BencodeMap result = (BencodeMap) decodeString("de");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should decode a complex nested map")
        void testDecodeComplexNestedMap() throws IOException {
            String bencoded = "d8:announce26:http://tracker.example.com4:infod5:filesl9:part1.dat9:part2.date6:lengthi1024eee";
            BencodeMap result = (BencodeMap) decodeString(bencoded);
            assertEquals(2, result.size());
            System.out.println(result);
            assertInstanceOf(BencodeMap.class, result.get(new BencString("info")));

            BencodeMap infoMap = (BencodeMap) result.get(new BencString("info".getBytes()));
            assertEquals(2, infoMap.size());
            assertInstanceOf(BencodeList.class, infoMap.get(new BencString("files".getBytes())));
            assertInstanceOf(BencodeInteger.class, infoMap.get(new BencString("length".getBytes())));
        }

        @Test
        @DisplayName("should throw IOException for map with non-string key")
        void testMapWithNonStringKey() {
            // A map where a key is an integer, which is invalid.
            assertThrows(UnsupportedEncodingException.class, () -> decodeString("di123e4:valuee"));
        }

        @Test
        @DisplayName("should throw IOException for unterminated map")
        void testUnterminatedMap() {
            assertThrows(IOException.class, () -> decodeString("d4:key4:value"));
        }
    }

    @Nested
    @DisplayName("General Error Handling")
    class GeneralErrorHandling {
        @Test
        @DisplayName("should throw IOException for empty input")
        void testEmptyInput() {
            assertThrows(IOException.class, () -> decodeString(""));
        }

        @Test
        @DisplayName("should throw IOException for unexpected end of stream")
        void testUnexpectedEndOfStream() {
            // String claims length 10 but only provides 5 bytes
            assertThrows(IOException.class, () -> bdecoder.decode(new ByteArrayInputStream("10:abcde".getBytes())));
        }

        @Test
        @DisplayName("should throw UnsupportedEncodingException for invalid marker")
        void testInvalidMarker() {
            assertThrows(UnsupportedEncodingException.class, () -> decodeString("x4:spam"));
        }
    }
}
