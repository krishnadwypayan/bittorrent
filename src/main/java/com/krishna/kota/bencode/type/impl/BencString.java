package com.krishna.kota.bencode.type.impl;

import com.krishna.kota.bencode.type.BencodeString;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a string in the Bencode format.
 * Bencode strings are byte sequences prefixed by their length and a colon.
 * This class is immutable.
 *
 * @param value the byte array value. A defensive copy is made.
 * @throws NullPointerException if the provided value is null.
 */
public record BencString(byte[] value) implements BencodeString {

    /**
     * Represents a string in the Bencode format.
     * Bencode strings are byte sequences prefixed by their length and a colon.
     * This class is immutable.
     */
    public BencString {
        Objects.requireNonNull(value, "value cannot be null");
        // make a defensive copy of the value to prevent external modifications
        value = Arrays.copyOf(value, value.length);
    }

    /**
     * Constructs a new BencString from a standard Java String.
     * The string is encoded into bytes using the UTF-8 charset.
     *
     * @param strValue the string value to be represented by this BencString.
     */
    public BencString(String strValue) {
        this(strValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns a defensive copy of the byte array value.
     * <p>
     * A new array is created and returned to prevent the internal
     * state of the object from being modified externally.
     *
     * @return a new byte array containing a copy of the value
     */
    @Override
    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    /**
     * Creates a new String by decoding the underlying bytes using the specified charset.
     *
     * @param charset the character set to use for decoding
     * @return the resulting String from the decoding process
     */
    @Override
    public String asString(Charset charset) {
        return new String(value, charset);
    }

    @Override
    public String toString() {
        return asString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BencString that = (BencString) o;
        return Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
