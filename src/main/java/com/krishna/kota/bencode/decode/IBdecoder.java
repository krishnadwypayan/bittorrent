package com.krishna.kota.bencode.decode;

import com.krishna.kota.bencode.type.BencodeElement;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines the contract for a Bencode decoder.
 * Implementations of this interface are responsible for parsing Bencode-formatted
 * data from various sources into a structured representation.
 */
public interface IBdecoder {

    /**
     * Decodes a Bencode-formatted data stream into a BencodeElement.
     * This method reads from the input stream and parses the data,
     * constructing the corresponding BencodeElement object.
     *
     * @param inputStream The input stream containing the Bencode data to decode.
     * @return The decoded BencodeElement object.
     * @throws java.io.IOException if an I/O error occurs while reading from the stream
     *                             or if the data is not in a valid Bencode format.
     */
    BencodeElement decode(InputStream inputStream) throws IOException;

    /**
     * Decodes a Bencode-encoded byte array into a BencodeElement.
     *
     * @param encodedBytes the byte array containing the Bencode-encoded data to be decoded.
     * @return the resulting BencodeElement object.
     * @throws IOException if the byte array is null or represents malformed Bencode data.
     */
    BencodeElement decode(byte[] encodedBytes) throws IOException;

}
