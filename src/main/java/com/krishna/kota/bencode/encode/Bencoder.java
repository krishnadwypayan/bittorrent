package com.krishna.kota.bencode.encode;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implements the Bencode encoding scheme for various Java objects.
 * This class provides methods to convert Java Strings, Integers, Lists, and Maps
 * into their Bencode byte representation, commonly used in BitTorrent files.
 */
@Singleton
public class Bencoder implements IBencoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bencoder.class);

    /**
     * Encodes the given object into a byte array.
     * This method internally uses a ByteArrayOutputStream to capture the encoded output.
     *
     * @param data the object to be encoded.
     * @return a byte array containing the encoded data.
     * @throws IOException if an I/O error occurs during the encoding process.
     */
    @Override
    public byte[] encode(Object data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encode(data, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Encodes the given data object and writes the result to the specified output stream.
     * This method supports encoding for String, Integer, List, and Map types. It
     * delegates the encoding process to type-specific helper methods.
     *
     * @param data         The data object to be encoded. Supported types are String,
     *                     Integer, List, and Map.
     * @param outputStream The stream to which the encoded data will be written.
     * @throws IOException if an I/O error occurs during writing to the stream,
     *                     or if the data type of the object is not supported for encoding.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void encode(Object data, OutputStream outputStream) throws IOException {
        switch (data) {
            case byte[] bytes -> encodeBytes(bytes, outputStream);
            case String s -> encodeString(s, outputStream);
            case Integer integer -> encodeInteger(integer, outputStream);
            case List<?> objects -> encodeList(objects, outputStream);
            case Map<?, ?> map -> encodeMap((Map<String, Object>) map, outputStream);
            case null, default -> throw new IOException("Data type not supported for encoding");
        }
    }

    /**
     * This is the canonical implementation for Bencoded strings.
     * The format is <length>:<byte_data>.
     *
     * @param data         the byte array to be encoded
     * @param outputStream the stream to which the encoded data will be written
     * @throws IOException if an I/O error occurs
     */
    private void encodeBytes(byte[] data, OutputStream outputStream) throws IOException {
        outputStream.write(String.valueOf(data.length).getBytes(StandardCharsets.US_ASCII));
        outputStream.write(':');
        outputStream.write(data);
    }

    /**
     * Encodes a given string into a length-prefixed format and writes it to an output stream.
     * The format consists of the string's length, followed by a colon, and then the string data itself.
     * Any exceptions during the write operation are caught and logged internally.
     *
     * @param data         the string data to be encoded
     * @param outputStream the stream to which the encoded data will be written
     */
    private void encodeString(String data, OutputStream outputStream) throws IOException {
        try {
            outputStream.write(String.format("%d:", data.length()).getBytes(StandardCharsets.UTF_8));
            outputStream.write(data.getBytes());
        } catch (Exception e) {
            LOGGER.error("Error occurred while encoding", e);
            throw new IOException("Error occurred while encoding");
        }
    }

    /**
     * Encodes a long integer and writes it to an output stream.
     * The integer is formatted as a string "i<value>e", converted to bytes,
     * and then written to the provided stream. Exceptions during the write
     * operation are caught and logged internally.
     *
     * @param value        the long integer value to encode
     * @param outputStream the stream to write the encoded integer to
     */
    private void encodeInteger(long value, OutputStream outputStream) throws IOException {
        try {
            outputStream.write(String.format("i%de", value).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.error("Error occurred while encoding", e);
            throw new IOException("Error occurred while encoding");
        }
    }

    /**
     * Encodes a list of objects into Bencode format and writes it to an output stream.
     * <p>
     * The encoding starts with the character 'l', followed by the Bencoded
     * representation of each element in the list, and ends with the
     * character 'e'. Any exceptions during the process are caught and logged.
     *
     * @param list         The list of objects to be encoded.
     * @param outputStream The stream to which the encoded list will be written.
     */
    private void encodeList(List<?> list, OutputStream outputStream) throws IOException {
        try {
            outputStream.write('l');
            for (Object element: list) {
                encode(element, outputStream);
            }
            outputStream.write('e');
        } catch (Exception e) {
            LOGGER.error("Error occurred while encoding", e);
            throw new IOException("Error occurred while encoding");
        }
    }

    /**
     * Encodes a map into the Bencode dictionary format and writes it to an output stream.
     * The map is first sorted by its keys as required by the Bencode specification.
     * The encoded dictionary starts with 'd' and ends with 'e'.
     *
     * @param map          The map containing string keys and object values to be encoded.
     * @param outputStream The stream to which the encoded map will be written.
     */
    private void encodeMap(Map<String, Object> map, OutputStream outputStream) throws IOException {
        try {
            outputStream.write('d');

            // Bencode requires entries be sorted by key
            Map<String, Object> treeMap = new TreeMap<>(map);
            for (Map.Entry<String, Object> entry: treeMap.entrySet()) {
                encodeString(entry.getKey(), outputStream);
                encode(entry.getValue(), outputStream);
            }
            outputStream.write('e');
        } catch (Exception e) {
            LOGGER.error("Error occurred while encoding", e);
            throw new IOException("Error occurred while encoding");
        }
    }
}
