/*
 *
 * MIT License
 *
 * Copyright (c) 2017 psygate (https://github.com/psygate)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.psygate.bitutils.bitvectors;

/**
 * Created by psygate on 01.09.2017.
 * <p>
 * AbstractReadableBitVector represents a purely readable bitvector.
 * <p>
 * get$TYPE(position) methods are a short hand for getBits(position, TYPE.SIZE)
 */
public abstract class AbstractReadableBitVector<T extends AbstractReadableBitVector<T>> {
    /**
     * @return Size of the vector in bits.
     */
    public abstract int size();

    /**
     * @return True if the bitvector doesn't contain anything, else false.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @param position Position of the bit to return.
     * @return Boolean representing the value of the bit at that position. (true = 1, false = 0)
     */
    public boolean get(int position) {
        if (position < 0 || position >= size()) {
            throw new IndexOutOfBoundsException("Position " + position + " out of bounds. (Size: " + size() + ")");
        }

        return getUnchecked(position);
    }

    protected abstract boolean getUnchecked(int position);

    /**
     * @param position Position of the bit to return.
     * @return Integer representing the value of the bit at that position.
     */
    public int getBit(int position) {
        return get(position) ? 1 : 0;
    }

    /**
     * Attempts to read a byte at the provided position.
     *
     * @param position Position to read the byte from.
     * @return Byte in the bitvector from position to position + Byte.SIZE
     * Shorthand for {@link #getBits(int, int)}
     */
    public byte getByte(int position) {
        return (byte) getBits(position, Byte.SIZE);
    }

    /**
     * Attempts to read a short at the provided position.
     *
     * @param position Position to read the short from.
     * @return Short in the bitvector from position to position + Short.SIZE
     * Shorthand for {@link #getBits(int, int)}
     */
    public short getShort(int position) {
        return (short) getBits(position, Short.SIZE);
    }

    /**
     * Attempts to read a byte at the provided position.
     *
     * @param position Position to read the integer from.
     * @return Integer in the bitvector from position to position + Integer.SIZE
     * Shorthand for {@link #getBits(int, int)}
     */
    public int getInt(int position) {
        return getBits(position, Integer.SIZE);
    }

    /**
     * Attempts to read a long at the provided position.
     *
     * @param position Position to read the long from.
     * @return Long in the bitvector from position to position + Byte.SIZE
     * Shorthand for {@link #getBitsLong(int, int)}
     */
    public long getLong(int position) {
        return getBitsLong(position, Long.SIZE);
    }

    /**
     * Gets the specified amount of bits from the vector, and packs it into the int.
     *
     * @param position Position to read the bits from.
     * @param size     Amount of bits to read.
     * @return Int containing the specified amount of bits.
     */
    public int getBits(int position, int size) {
        checkPosition(position);
        checkSize(size, Integer.SIZE);
        checkAvailable(position, size);
        int value = 0;

        for (int i = 0; i < size; i++) {
            value |= getBit(position + i) << i;
        }

        return value;
    }

    /**
     * Gets the specified amount of bits from the vector, and packs it into the long.
     *
     * @param position Position to read the bits from.
     * @param amount   Amount of bits to read.
     * @return Long containing the specified amount of bits.
     */
    public long getBitsLong(int position, int amount) {
        checkPosition(position);
        checkSize(amount, Long.SIZE);
        checkAvailable(position, amount);

        long value = 0;
        for (int i = 0; i < amount; i++) {
            value |= ((long) getBit(position + i)) << i;
        }

        return value;
    }

    /**
     * Returns an array of bytes from the specified position with the specified length.
     *
     * @param position Position to read from in bits.
     * @param length   Amount of bytes to read.
     * @return Array containing the bytes read.
     */
    public byte[] getBytes(int position, int length) {
        checkPosition(position);
        checkSize(length * Byte.SIZE, Integer.MAX_VALUE);
        checkAvailable(position, length * Byte.SIZE);

        byte[] byteArray = new byte[length];

        for (int i = 0; i < length; i++) {
            byteArray[i] = getByte(position + i * Byte.SIZE);
        }

        return byteArray;
    }

    /**
     * Converts the bitvector to a boolean array.
     *
     * @return Boolean array containing the bits in this vector as booleans. The bit order is the same as this vectors bit order.
     */
    public boolean[] toBooleanArray() {
        boolean[] boolArray = new boolean[size()];

        for (int i = 0; i < size(); i++) {
            boolArray[i] = get(i);
        }

        return boolArray;
    }

    // String like operations

    /**
     * Same as subvector(position, {@link #size()} - position). See {@link #subVector(int, int)}
     */
    public T subVector(int position) {
        return subVector(position, size() - position);
    }

    /**
     * Creates a subvector of this vector. Modifying the subvector will not change the parent vector.
     *
     * @param position Position from which to create the subvector.
     * @param length   Length of the subvector.
     * @return BitVector containing the bits from position to position + length.
     */
    public T subVector(int position, int length) {
        if (position < 0 || position > size()) {
            throw new IndexOutOfBoundsException("Position exceeds in BitVector size. (Position: " + position + ", Length: " + length + ", Size: " + size() + ")");
        } else if (length + position > size() || length < 0) {
            throw new IndexOutOfBoundsException("Length exceeds in BitVector size. (Position: " + position + ", Length: " + length + ", Size: " + size() + ")");
        }

        return subVectorUnchecked(position, length);
    }

    /**
     * Returns a subvector of the range $from to $to.
     *
     * @param from Position to start from.
     * @param to   Position to stop at.
     * @return BitVector representing the range of $from to $to.
     * @throws IndexOutOfBoundsException If the requested positions are outside the bitvector.
     */
    public T range(int from, int to) {
        checkPosition(from);
        checkPosition(to);
        if (from == to) {
            return empty();
        } else if (from > to) {
            throw new IllegalArgumentException("From position is larger than to position. (" + from + " > " + to + ")");
        }
        return rangeUnchecked(from, to);
    }

    /**
     * Returns a subvector of the range $from to $to.
     *
     * @param from Position to start from.
     * @param to   Position to stop at.
     * @return BitVector representing the range of $from to $to.
     */
    protected T rangeUnchecked(int from, int to) {
        return subVectorUnchecked(from, to - from);
    }

    /**
     * Creates a subvector of this bitvector without explicit argument checking.
     *
     * @param position Position from which to create the subvector.
     * @param length   Length of the subvector.
     * @return BitVector containing the bits from position to position + length.
     */
    protected abstract T subVectorUnchecked(int position, int length);

    /**
     * Same as matches(0, other). See {@link #matches(int, AbstractReadableBitVector)}
     */
    public boolean matches(AbstractReadableBitVector other) {
        return matches(0, other);
    }

    /**
     * Returns true if the bits in this vector match the bits in the other vector from startIndex to startIndex + other.size()
     * . If the other.size() is smaller than {@link #size()} and all bits match, this will still return true.
     *
     * @param other      BitVector to check against.
     * @param startIndex Index from which to start matching against other.
     * @return True if all bits at position startIndex to startIndex + other.size() match all bits in the other vector.
     */
    public boolean matches(int startIndex, AbstractReadableBitVector other) {
        if (startIndex + other.size() > size()) {
            return false;
        }
        for (int i = 0; i < other.size(); i++) {
            if (get(i + startIndex) != other.get(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Same as indexOf(0, other). See {@link #indexOf(int, AbstractReadableBitVector)}
     */
    public int indexOf(AbstractReadableBitVector other) {
        return indexOf(0, other);
    }

    /**
     * Returns the first index after startIndex at which this vector matches the other. Searching for an empty vector will always return 0.
     *
     * @param startIndex
     * @param other
     * @return
     */
    public int indexOf(int startIndex, AbstractReadableBitVector other) {
        if (other.isEmpty()) {// || this.isEmpty()) {
            return 0;
//            return -1;
        }

        if (startIndex + other.size() <= size()) {
            for (int i = startIndex; i < size(); i++) {
                if (matches(i, other)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Returns a byte array containing all bits in the bitvector. Modifying the array does not modify the vector.
     *
     * @return byte[] containing all bits in the bitvector.
     */
    public byte[] toByteArray() {
        int remaining = size() % Byte.SIZE;
        int arraySize;

        if (remaining != 0) {
            arraySize = size() / Byte.SIZE + 1;
        } else {
            arraySize = size() / Byte.SIZE;
        }

        byte[] out = new byte[arraySize];

        for (int i = 0; i < (size() / Byte.SIZE); i++) {
            out[i] = getByte(i * Byte.SIZE);
        }

        if (remaining != 0) {
            out[out.length - 1] = (byte) getBits(size() - remaining, remaining);
        }

        return out;
    }

    // Check methods

    /**
     * Checks if more or equally many bits to amount are available from the bitvector from the given position.
     *
     * @param position Position to check from.
     * @param amount   Amount of bits that have to be available after position.
     * @throws IndexOutOfBoundsException If the requested amount at the given position exceeds the available bits in the vector.
     */
    protected void checkAvailable(int position, int amount) {
        if (position + amount > size()) {
            throw new IndexOutOfBoundsException("Cannot read byte, position + " + amount + " out of range. (Position: " + position + ", Size: " + size() + ")");
        }
    }

    /**
     * Checks if the given size is a valid size with respect to maximumBits.
     *
     * @param size    Size to check.
     * @param maximum Maximum permitted size.
     * @throws IndexOutOfBoundsException If the requested size is smaller than 0 or larger than maximum.
     */
    protected void checkSize(int size, int maximum) {
        if (size < 0) {
            throw new IndexOutOfBoundsException("Size cannot be less than 0. (Size: " + size + ")");
        } else if (size > maximum) {
            throw new IndexOutOfBoundsException("Size cannot be larger than containing type. (Size: " + size + ", Maximum Bits: " + maximum + ")");
        }
    }

    /**
     * Checks if a given position is within the bitvector.
     *
     * @param position Position to check.
     * @throws IndexOutOfBoundsException If the requested position is outside the bitvector.
     */
    protected void checkPosition(int position) {
        if (position < 0) {
            throw new IndexOutOfBoundsException("Position cannot be less than 0. (Position: " + position + ")");
        } else if (position >= size()) {
            throw new IndexOutOfBoundsException("Position cannot be outside fo vector range. (Position: " + position + ", Size: " + size() + ")");
        }
    }

    /**
     * Returns a string containing the bits in this vector as "1" and "0".
     *
     * @return String representing this
     */
    protected String bitString() {
        String bits = "";

        for (int i = 0; i < size(); i++) {
            bits += get(i) ? "1" : "0";
            if (i < size() - 1) {
                bits += ", ";
            }
        }

        return bits;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AbstractReadableBitVector)) {
            return false;
        } else {
            AbstractReadableBitVector other = (AbstractReadableBitVector) obj;
            if (other.size() != size()) {
                return false;
            } else {
                for (int i = 0; i < size(); i++) {
                    if (get(i) != other.get(i)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }


    @Override
    public String toString() {
        return "BitVector{" + bitString() + "}";
    }

    /**
     * Returns if this vector is empty or not.
     *
     * @return True if this vector contains no bits and is empty.
     */
    public abstract T empty();

    /**
     * Returns an independent copy of this vector.
     *
     * @return Copy of this vector.
     */
    public abstract T copy();


    /**
     * @return Shorthand for {@link #nextSetBit(int)} with 0 as argument.
     */
    public int nextSetBit() {
        return nextSetBit(0);
    }

    /**
     * @param index Index from which to start searching the first set bit.
     * @return Index of the first set bit in the bitvector, if none are set, -1 is returned.
     */
    public int nextSetBit(int index) {
        for (int i = index; i < size(); i++) {
            if (get(i)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * @return Shorthand for {@link #nextUnsetBit(int)} with 0 as argument.
     */
    public int nextUnsetBit() {
        return nextSetBit(0);
    }

    /**
     * @param index Index from which to start searching the first unset bit.
     * @return Index of the first unset bit in the bitvector, if none are set, -1 is returned.
     */
    public int nextUnsetBit(int index) {
        for (int i = index; i < size(); i++) {
            if (!get(i)) {
                return i;
            }
        }

        return -1;
    }
}
