package com.krishna.kota.bencode.decode;

import com.krishna.kota.bencode.type.*;
import com.krishna.kota.bencode.type.impl.BencInteger;
import com.krishna.kota.bencode.type.impl.BencList;
import com.krishna.kota.bencode.type.impl.BencMap;
import com.krishna.kota.bencode.type.impl.BencString;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides functionality to decode Bencode-formatted data streams and byte arrays.
 * This class implements the IBdecoder interface and is responsible for parsing
 * Bencode strings, integers, lists, and dictionaries into their corresponding
 * object representations.
 */
public class Bdecoder implements IBdecoder {

    private static final int STRING_DELIMITER = ':';
    private static final int INTEGER_MARKER = 'i';
    private static final int LIST_MARKER = 'l';
    private static final int DICT_MARKER = 'd';
    private static final int END_MARKER = 'e';

    /**
     * Decodes a Bencode-formatted data stream from the provided InputStream
     * into its corresponding BencodeElement representation.
     *
     * @param inputStream The input stream containing the Bencode data to be decoded.
     * @return The decoded BencodeElement object.
     * @throws IOException If an I/O error occurs while reading from the stream.
     */
    @Override
    public BencodeElement decode(InputStream inputStream) throws IOException {
        // Wrap in a BufferedInputStream to ensure mark/reset is supported and for performance.
        InputStream is = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
        return parseElement(is);
    }

    /**
     * Decodes a Bencoded byte array into a BencodeElement.
     * <p>
     * This is a convenience method that wraps the byte array in a
     * ByteArrayInputStream and then proceeds with the decoding.
     *
     * @param encodedBytes The byte array containing the Bencoded data to decode.
     * @return The decoded BencodeElement object.
     * @throws IOException if an I/O error occurs during the decoding process.
     */
    @Override
    public BencodeElement decode(byte[] encodedBytes) throws IOException {
        return decode(new ByteArrayInputStream(encodedBytes));
    }

    /**
     * Parses a single Bencode element from an input stream.
     * This method determines the element type by peeking at the first byte
     * and delegates to the corresponding specialized parser.
     *
     * @param inputStream The input stream to read the Bencode data from. Must support mark and reset.
     * @return The parsed Bencode element.
     * @throws IOException if an I/O error occurs, the stream ends unexpectedly, or an unsupported Bencode marker is found.
     */
    private BencodeElement parseElement(InputStream inputStream) throws IOException {
        inputStream.mark(1);
        int marker = inputStream.read();
        inputStream.reset();

        if (marker >= '0' && marker <= '9') {
            return parseString(inputStream);
        }

        switch (marker) {
            case INTEGER_MARKER:
                return parseInteger(inputStream);
            case LIST_MARKER:
                return parseList(inputStream);
            case DICT_MARKER:
                return parseMap(inputStream);
            case -1:
                throw new IOException("Unexpected end of stream.");
            default:
                throw new UnsupportedEncodingException("Unsupported Bencode marker");
        }
    }

    /**
     * Parses a Bencode formatted string from an input stream.
     * This method reads the string's length, followed by a delimiter, and then reads the
     * specified number of bytes to construct the string.
     *
     * @param inputStream the stream to read the Bencode string from.
     * @return the parsed BencodeString object.
     * @throws IOException if an I/O error occurs during reading or if the string format is invalid.
     */
    private BencodeString parseString(InputStream inputStream) throws IOException {
        int length = readNumberUntil(inputStream, STRING_DELIMITER);
        if (length < 0) {
            throw new IOException("Invalid string length");
        }
        byte[] string = inputStream.readNBytes(length);
        if (string.length != length) {
            throw new IOException("Unexpected end of stream while reading string");
        }
        return new BencString(string);
    }

    /**
     * Parses a Bencoded integer from the given input stream.
     * This method expects the stream to start with the integer marker 'i',
     * followed by the number, and end with the end marker 'e'. It performs
     * validation to reject invalid formats such as "-0" or numbers with
     * leading zeros.
     *
     * @param inputStream the input stream to read the Bencoded integer from
     * @return the parsed BencodeInteger object
     * @throws IOException if an I/O error occurs during reading, or if the
     *                     data in the stream does not represent a valid
     *                     Bencoded integer
     */
    private BencodeInteger parseInteger(InputStream inputStream) throws IOException {
        consumeAndVerify(inputStream, INTEGER_MARKER);
        String numberString = readStringUntil(inputStream, END_MARKER);
        if (numberString.equals("-0")) {
            throw new IOException("Invalid integer literal");
        }
        if (numberString.length() > 1 && numberString.charAt(0) == '0') {
            throw new IOException("Invalid integer literal");
        }

        try {
            BigInteger value = new BigInteger(numberString);
            return new BencInteger(value);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format for number integer: " + numberString, e);
        }
    }

    /**
     * Parses a Bencode-encoded list from an input stream.
     * The method reads from the stream until it finds the list end marker.
     * Each item within the list is parsed as a Bencode element.
     *
     * @param inputStream The input stream to read the Bencode list from.
     * @return A BencodeList containing the parsed elements.
     * @throws IOException if an I/O error occurs or if the stream format is invalid.
     */
    private BencodeList parseList(InputStream inputStream) throws IOException {
        consumeAndVerify(inputStream, LIST_MARKER);

        List<BencodeElement> elements = new ArrayList<>();
        while (peek(inputStream) != END_MARKER) {
            elements.add(parseElement(inputStream));
        }

        consumeAndVerify(inputStream, END_MARKER);
        return new BencList(elements);
    }

    /**
     * Parses a Bencode-encoded dictionary from an input stream.
     * A Bencode dictionary starts with 'd', contains a series of alternating
     * keys and values, and ends with 'e'. The keys must be Bencode strings.
     *
     * @param inputStream The stream from which to read the Bencode data.
     * @return A BencodeMap object representing the parsed dictionary.
     * @throws IOException If an I/O error occurs, the stream ends unexpectedly,
     *                     or the data format is invalid (e.g., a key is not a string).
     */
    private BencodeMap parseMap(InputStream inputStream) throws IOException {
        consumeAndVerify(inputStream, DICT_MARKER);

        Map<BencodeString, BencodeElement> map = new HashMap<>();
        while (peek(inputStream) != END_MARKER) {
            BencodeElement key = parseElement(inputStream);
            if (!(key instanceof BencodeString)) {
                throw new UnsupportedEncodingException("key must be an instanceof BencodeString");
            }
            BencodeElement value = parseElement(inputStream);
            map.put((BencodeString) key, value);
        }

        consumeAndVerify(inputStream, END_MARKER);
        return new BencMap(map);
    }

    /* --------------------------------- */
    /* Helper methods below */

    /**
     * Reads a string from an input stream until a specified delimiter is found,
     * and then parses it into an integer.
     *
     * @param inputStream The input stream to read data from.
     * @param delimiter   The character that marks the end of the number string.
     * @return The integer parsed from the stream.
     * @throws IOException If an I/O error occurs while reading from the stream,
     *                     or if the read string cannot be parsed into a valid integer.
     */
    private int readNumberUntil(InputStream inputStream, int delimiter) throws IOException {
        String lenStr = readStringUntil(inputStream, delimiter);
        try {
            return Integer.parseInt(lenStr);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid number format");
        }
    }

    /**
     * Reads bytes from an input stream until a specified delimiter is encountered.
     * The method consumes the delimiter from the stream but does not include it
     * in the returned string. The collected bytes are converted to a string using
     * the US-ASCII charset.
     *
     * @param inputStream The input stream to read data from.
     * @param delimiter   The byte value that signals the end of reading.
     * @return A string constructed from the bytes read before the delimiter.
     * @throws IOException If an I/O error occurs during the read operation.
     */
    private String readStringUntil(InputStream inputStream, int delimiter) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int currentByte;
        while ((currentByte = inputStream.read()) != delimiter) {
            byteArrayOutputStream.write(currentByte);
        }
        return byteArrayOutputStream.toString(StandardCharsets.US_ASCII);
    }

    /**
     * Peeks at the next byte of an input stream without consuming it.
     * This method reads the next byte but then resets the stream to its
     * original position before the read, effectively allowing a lookahead.
     * The underlying stream must support the mark and reset operations.
     *
     * @param inputStream the input stream to peek into
     * @return the next byte of data as an integer, or -1 if the end of the stream is reached
     * @throws IOException if an I/O error occurs or the stream does not support mark/reset
     */
    private int peek(InputStream inputStream) throws IOException {
        inputStream.mark(1);
        int current = inputStream.read();
        inputStream.reset();
        return current;
    }

    /**
     * Reads a single byte from the given input stream and verifies it against
     * an expected value.
     *
     * @param inputStream the input stream to read the byte from
     * @param actual      the expected byte value to verify against
     * @throws IOException if an I/O error occurs during the read, or if the byte
     *                     read from the stream does not match the expected value
     */
    private void consumeAndVerify(InputStream inputStream, int actual) throws IOException {
        int current = inputStream.read();
        if (current != actual) {
            throw new IOException("Expected '" + (char)actual + "', but got '" + (char)current + "'");
        }
    }

}
