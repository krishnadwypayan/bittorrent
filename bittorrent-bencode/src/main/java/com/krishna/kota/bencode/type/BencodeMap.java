package com.krishna.kota.bencode.type;

import java.util.Map;

public interface BencodeMap extends BencodeElement, Map<BencodeString, BencodeElement> {
}
