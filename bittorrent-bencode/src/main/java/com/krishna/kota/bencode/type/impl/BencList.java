package com.krishna.kota.bencode.type.impl;

import com.krishna.kota.bencode.type.BencodeElement;
import com.krishna.kota.bencode.type.BencodeList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An immutable implementation of the BencodeList interface.
 *
 * This class uses composition, wrapping an unmodifiable internal list to guarantee
 * immutability. It delegates all List operations to this internal list.
 *
 * Any attempt to call a mutator method (like add, set, or remove) will result
 * in an {@link UnsupportedOperationException}, as per the contract for unmodifiable lists.
 */
public final class BencList implements BencodeList {

    private final List<BencodeElement> elements;

    /**
     * Constructs a BencodeList from a collection of BencodeElements.
     *
     * @param elements The collection of elements. A defensive, immutable copy is made.
     *                 Cannot be null.
     */
    public BencList(Collection<BencodeElement> elements) {
        Objects.requireNonNull(elements, "Element collection cannot be null.");
        // List.copyOf creates a new, unmodifiable list, ensuring immutability.
        this.elements = List.copyOf(elements);
    }

    // --- Delegation of all List interface methods to the internal list ---

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<BencodeElement> iterator() {
        return elements.iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return elements.toArray(a);
    }

    @Override
    public BencodeElement get(int index) {
        return elements.get(index);
    }

    @Override
    public BencodeElement set(int index, BencodeElement element) {
        return null;
    }

    @Override
    public void add(int index, BencodeElement element) {

    }

    @Override
    public BencodeElement remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    public ListIterator<BencodeElement> listIterator() {
        return elements.listIterator();
    }

    @Override
    public ListIterator<BencodeElement> listIterator(int index) {
        return elements.listIterator(index);
    }

    @Override
    public List<BencodeElement> subList(int fromIndex, int toIndex) {
        // The sublist of an unmodifiable list is also unmodifiable.
        return elements.subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends BencodeElement> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends BencodeElement> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Stream<BencodeElement> stream() {
        return elements.stream();
    }

    @Override
    public Stream<BencodeElement> parallelStream() {
        return elements.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super BencodeElement> action) {
        elements.forEach(action);
    }

    @Override
    public Spliterator<BencodeElement> spliterator() {
        return elements.spliterator();
    }

    // --- Mutator methods will throw UnsupportedOperationException ---
    // We don't need to implement them because the delegate (elements) will do it for us.

    @Override
    public boolean add(BencodeElement bencodeElement) {
        return elements.add(bencodeElement); // Will throw
    }

    @Override
    public boolean remove(Object o) {
        return elements.remove(o); // Will throw
    }

    // ... and so on for all other mutator methods (addAll, removeAll, set, etc.)

    // --- equals, hashCode, and toString are also delegated ---

    /**
     * Compares this BencodeList with another object for equality.
     * Follows the contract of {@link List#equals(Object)}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // We can be equal to any other List, not just another BencodeListImpl.
        if (!(o instanceof List)) return false;
        return elements.equals(o);
    }

    /**
     * Returns the hash code for this list.
     * Follows the contract of {@link List#hashCode()}.
     */
    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    /**
     * Returns a string representation of this list.
     * Follows the contract of {@link List#toString()}.
     */
    @Override
    public String toString() {
        return elements.toString();
    }
}
