package com.krishna.kota.bencode.encode;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Provides an interface for encoding Java objects into the Bencode format.
 * Supported types are Map, List, Integer, String.
 * Bencode is a data serialization format used primarily by the BitTorrent
 * file sharing system.
 */
public interface IBencoder {

    /**
     * Encode the given data into a bencoded byte array.
     *
     * @param data The object to encode.
     * @return A byte array containing the Bencoded representation.
     * @throws java.io.IOException If the given data is not supported.
     */
    byte[] encode(Object data) throws IOException;

    /**
     * Encode the given data into a bencoded byte array and write it to the given output stream.
     *
     * @param data The object to encode.
     * @param outputStream The output stream to write the Bencoded data to.
     * @throws java.io.IOException If the given data is not supported.
     */
    void encode(Object data, OutputStream outputStream) throws IOException;

}
