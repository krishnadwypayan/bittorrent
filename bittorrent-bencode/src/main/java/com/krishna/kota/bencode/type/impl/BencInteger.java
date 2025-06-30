package com.krishna.kota.bencode.type.impl;

import com.krishna.kota.bencode.type.BencodeInteger;

import java.math.BigInteger;
import java.util.Objects;

public record BencInteger(BigInteger value) implements BencodeInteger {

    public BencInteger(long longValue) {
        this(BigInteger.valueOf(longValue));
    }

    @Override
    public BigInteger bigIntegerValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BencInteger that = (BencInteger) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
