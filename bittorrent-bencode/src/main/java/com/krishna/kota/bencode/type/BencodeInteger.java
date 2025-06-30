package com.krishna.kota.bencode.type;

import java.math.BigInteger;

/**
 * Represents an integer element in the Bencode format.
 * Bencode integers are encoded as 'i' followed by the number in base 10
 * and a closing 'e'. For example, the integer 42 is encoded as "i42e".
 */
public interface BencodeInteger extends BencodeElement {

    /**
     * Returns the value of this object as a long, which may involve rounding or truncation.
     *
     * @return the numeric value represented by this object after conversion to type long
     */
    BigInteger bigIntegerValue();

    default long longValue() {
        return bigIntegerValue().longValue();
    }

}
