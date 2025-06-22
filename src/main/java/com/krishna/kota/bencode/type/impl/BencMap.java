package com.krishna.kota.bencode.type.impl;

import com.krishna.kota.bencode.type.BencodeElement;
import com.krishna.kota.bencode.type.BencodeMap;
import com.krishna.kota.bencode.type.BencodeString;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BencMap implements BencodeMap {

    private final Map<BencodeString, BencodeElement> map;

    public BencMap(Map<BencodeString, BencodeElement> map) {
        this.map = Map.copyOf(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public BencodeElement get(Object key) {
        return map.get(key);
    }

    @Override
    public BencodeElement put(BencodeString key, BencodeElement value) {
        return map.put(key, value);
    }

    @Override
    public BencodeElement remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends BencodeString, ? extends BencodeElement> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<BencodeString> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<BencodeElement> values() {
        return map.values();
    }

    @Override
    public Set<Entry<BencodeString, BencodeElement>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // A BencMap can be equal to ANY other Map, not just another BencMap.
        // This is the contract of java.util.Map.
        if (!(o instanceof Map)) return false;
        // Delegate the comparison to the internal map, which has the correct logic.
        return this.map.equals(o);
    }

    @Override
    public int hashCode() {
        // This is correct, as it just delegates to the internal map's hashCode.
        return map.hashCode();
    }
}
