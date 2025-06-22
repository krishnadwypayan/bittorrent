package com.krishna.kota.bencode.type;

import java.nio.charset.Charset;

/**
 * Represents a string element in the Bencode format.
 * Bencode strings are essentially byte arrays. This interface provides methods
 * to retrieve the raw byte data or to interpret it as a String using a
 * specific character set.
 */
public interface BencodeString extends BencodeElement {

    /**
     * Gets the value as a byte array.
     *
     * @return the value as a byte array
     */
    byte[] getValue();

    /**
     * Convenience method.
     * Decodes the content into a String using the specified character set.
     *
     * @param charset the character set to use for decoding the content
     * @return the decoded content as a String
     */
    String asString(Charset charset);

}
