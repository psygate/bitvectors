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

import java.util.Arrays;

import static com.github.psygate.bitutils.bitvectors.ArrayUtils.packToLong;

/**
 * Created by psygate on 04.09.2017.
 * <p>
 * Implementation of a BitVector. This implementation is modifying in place and will only return a new vector where explicitly required. All set & write methods return this vector.
 */
public class BitVector {
    private long[] bits;
    private int size;

    public static BitVector of(boolean[] values) {
        return new BitVector(packToLong(values), values.length, false);
    }

    public static BitVector of(byte[] values) {
        return new BitVector(packToLong(values), values.length * Byte.SIZE, false);
    }

    public static BitVector of(short[] values) {
        return new BitVector(packToLong(values), values.length * Short.SIZE, false);
    }

    public static BitVector of(int[] values) {
        return new BitVector(packToLong(values), values.length * Integer.SIZE, false);
    }

    public static BitVector of(long[] values) {
        return new BitVector(values, values.length * Long.SIZE, true);
    }

    public static BitVector of(boolean value) {
        return new BitVector(new long[]{value ? 1L : 0L}, 1, false);
    }

    public static BitVector of(byte value) {
        return new BitVector(new long[]{value & 0xFFL}, Byte.SIZE, false);
    }

    public static BitVector of(short value) {
        return new BitVector(new long[]{value & 0xFFFFL}, Short.SIZE, false);
    }

    public static BitVector of(int value) {
        return new BitVector(new long[]{value & 0xFFFF_FFFFL}, Integer.SIZE, false);
    }

    public static BitVector of(long value) {
        return new BitVector(new long[]{value}, Long.SIZE, false);
    }

    public static BitVector of(int value, int size) {
        if (size > Integer.SIZE || size < 0) {
            throw new IllegalArgumentException("Cannot create BitVector from integer with size " + size + " (max. " + Integer.SIZE + ")");
        }
        return new BitVector(new long[]{value & 0xFFFF_FFFFL}, size, false);
    }

    public static BitVector of(long value, int size) {
        if (size > Long.SIZE || size < 0) {
            throw new IllegalArgumentException("Cannot create BitVector from long with size " + size + " (max. " + Long.SIZE + ")");
        }
        return new BitVector(new long[]{value}, size, false);
    }

    public static BitVector ofBinaryString(String values) {
        if (!values.matches("[01]*")) {
            throw new IllegalArgumentException("Cannot turn \"" + values + "\" into a BitVector.");
        }

        return new BitVector(packToLong(values), values.length(), false);
    }

    private BitVector(long[] values, int size, boolean copy) {
        if (copy) {
            this.bits = Arrays.copyOf(values, values.length);
        } else {
            this.bits = values;
        }
        this.size = size;
    }

    public BitVector(int size) {
        this.bits = new long[size / Long.SIZE];
        this.size = size;
    }


    public BitVector() {
        this(new long[0], 0, false);
    }

    public BitVector(long[] values, int size) {
        this(values, size, true);
    }


    /**
     * @param bit Bit to write.
     * @return Vector with the written bit.
     */
    public BitVector writeBit(boolean bit) {
        if (isEmpty()) {
            bits = new long[]{bit ? 1L : 0L};
            size = 1;
            return this;
        }

        if (bits.length * Long.SIZE <= size()) {
            long[] oldbits = bits;
            bits = new long[oldbits.length + 1];
            System.arraycopy(oldbits, 0, bits, 0, oldbits.length);
        }

        int longIdx = size / Long.SIZE;
        int bitOffset = size % Long.SIZE;
        long value = bits[longIdx];
        long bitSet = 1L << bitOffset;

        if (bit) {
            value |= bitSet;
        } else {
            value &= ~bitSet;
        }

        bits[longIdx] = value;

        size++;
        return this;
    }


    /**
     * @param index Index in the BitVector where to set the bit.
     * @param bit   The bit to set.
     * @return Vector with the bit at $index set to $bit.
     */
    protected BitVector setBitUnchecked(int index, boolean bit) {
        int longIdx = index / Long.SIZE;
        int bitOffset = index % Long.SIZE;
        long value = bits[longIdx];
        long bitSet = 1L << bitOffset;

        if (bit) {
            value |= bitSet;
        } else {
            value &= ~bitSet;
        }

        bits[longIdx] = value;

        return this;
    }

    /**
     * @return Size of the vector in bits.
     */
    public int size() {
        return size;
    }

    /**
     * Returns a bit without performing any checks.
     *
     * @param position Position where to get the bit from.
     * @return Boolean representing the bit.
     */
    protected boolean getUnchecked(int position) {
        int longIdx = position / Long.SIZE;
        int bitIdx = position % Long.SIZE;

        return ((bits[longIdx] >> bitIdx) & 1L) == 1L;
    }

    /**
     * Creates a subvector of this bitvector without explicit argument checking.
     *
     * @param position Position from which to create the subvector.
     * @param length   Length of the subvector.
     * @return BitVector containing the bits from position to position + length.
     */
    protected BitVector subVectorUnchecked(int position, int length) {
        BitVector vec = new BitVector();
        for (int i = 0; i < length; i++) {
            vec.writeBit(get(position + i));
        }

        return vec;
    }

    /**
     * Returns an empty bitvector.
     *
     * @return Empty bitvector.
     */
    public BitVector empty() {
        return new BitVector(0);
    }

    /**
     * Returns an independent copy of this vector.
     *
     * @return Copy of this vector.
     */
    public BitVector copy() {
        return new BitVector(bits, size(), true);
    }

    public static BitVector emptyVector() {
        return new BitVector();
    }

    /**
     * @param index   Index at which to start searching for find.
     * @param find    Substring to find within this BitVector.
     * @param replace Replacement string to insert instead of $find within this BitVector.
     * @return BitVector with the first occurance after $index of $find replaced with $replace.
     */
    public BitVector replaceFirst(int index, BitVector find, BitVector replace) {
        if (isEmpty()) {
            return this;
        }

        int found = indexOf(index, find);

        if (found == -1) {
            return this;
        } else {
            BitVector head = subVectorUnchecked(0, found);
            BitVector tail = subVectorUnchecked(found + find.size(), size() - (found + find.size()));

            BitVector vec = (BitVector) head.write(replace);

            vec = (BitVector) vec.write(tail);

            return vec;
        }
    }

    /**
     * @param other Returns a bitvector that is the bitwise or of this and the other bitvector.
     * @return Bitwise Or product of this and other vector.
     */
    protected BitVector uncheckedOr(BitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) | getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    /**
     * @param other Returns a bitvector that is the bitwise and of this and the other bitvector.
     * @return Bitwise And product of this and other vector.
     */
    protected BitVector uncheckedAnd(BitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) & getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    /**
     * @param other Returns a bitvector that is the bitwise xor of this and the other bitvector.
     * @return Bitwise Xor product of this and other vector.
     */
    protected BitVector uncheckedXor(BitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) ^ getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    /**
     * @return Returns the bitwise not of this vector.
     */
    public BitVector not() {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < bits.length; i++) {
            buffer[i] = ~bits[i];
        }

        return new BitVector(buffer, size(), false);
    }

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
    public BitVector subVector(int position) {
        return subVector(position, size() - position);
    }

    /**
     * Creates a subvector of this vector. Modifying the subvector will not change the parent vector.
     *
     * @param position Position from which to create the subvector.
     * @param length   Length of the subvector.
     * @return BitVector containing the bits from position to position + length.
     */
    public BitVector subVector(int position, int length) {
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
    public BitVector range(int from, int to) {
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
    protected BitVector rangeUnchecked(int from, int to) {
        return subVectorUnchecked(from, to - from);
    }

    /**
     * Same as matches(0, other). See {@link #matches(int, BitVector)}
     */
    public boolean matches(BitVector other) {
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
    public boolean matches(int startIndex, BitVector other) {
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
     * Same as indexOf(0, other). See {@link #indexOf(int, BitVector)}
     */
    public int indexOf(BitVector other) {
        return indexOf(0, other);
    }

    /**
     * Returns the first index after startIndex at which this vector matches the other. Searching for an empty vector will always return 0.
     *
     * @param startIndex
     * @param other
     * @return
     */
    public int indexOf(int startIndex, BitVector other) {
        if (other.isEmpty()) {// || this.empty()) {
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


    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BitVector)) {
            return false;
        } else {
            BitVector other = (BitVector) obj;
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


    public String toString() {
        return "BitVector{" + bitString() + "}";
    }

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

    /**
     * @param bit Bit to write. Only the least significant bit is considered.
     * @return Vector with the written bit.
     */
    public BitVector writeBit(int bit) {
        return writeBit((bit & 0x1) == 1);
    }


    /**
     * @param bit Bit to write. Only the least significant bit is considered.
     * @return Vector with the written bit.
     */
    public BitVector writeBit(long bit) {
        return writeBit((bit & 0x1) == 1);
    }

    /**
     * @param value Byte to write to the vector.
     * @return Vector with the written byte.
     */
    public BitVector writeByte(byte value) {
        return writeByte(value & 0xFF);
    }

    /**
     * @param value Byte to write to the vector. Only the least significant 8 bits are considered.
     * @return Vector with the written byte.
     */
    public BitVector writeByte(int value) {
        BitVector v = this;

        for (int i = 0; i < Byte.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Short value to write to the vector.
     * @return Vector with the written short value.
     */
    public BitVector writeShort(short value) {
        return writeShort(value & 0xFFFF);
    }

    /**
     * @param value Short value to write. Only the least significant 16 bits are considered.
     * @return Vector with the written short value.
     */
    public BitVector writeShort(int value) {
        BitVector v = this;
        for (int i = 0; i < Short.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Int value to write.
     * @return Vector with the written int value.
     */
    public BitVector writeInt(int value) {
        BitVector v = this;
        for (int i = 0; i < Integer.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Long value to write.
     * @return Vector with the written long value.
     */
    public BitVector writeLong(long value) {
        BitVector v = this;
        for (int i = 0; i < Long.SIZE; i++) {
            v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value  Value to write from least significant to most significant bit.
     * @param amount Amount of bits to write.
     * @return Vector with the written amount of bits of value.
     */
    public BitVector writeBits(int value, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bit count too small. " + amount);
        } else if (amount > Integer.SIZE) {
            throw new IllegalArgumentException("Bit count exceeds maximum size. " + amount + "/" + Integer.SIZE);
        }

        BitVector v = this;
        for (int i = 0; i < amount; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value  Value to write from least significant to most significant bit.
     * @param amount Amount of bits to write.
     * @return Vector with the written amount of bits of value.
     */
    public BitVector writeBits(long value, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bit count too small. " + amount);
        } else if (amount > Long.SIZE) {
            throw new IllegalArgumentException("Bit count exceeds maximum size. " + amount + "/" + Long.SIZE);
        }

        BitVector v = this;

        for (int i = 0; i < amount; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * Writes the specified bytes to the stream.
     *
     * @param data Array containing the data to be written.
     * @return Vector with the written bytes.
     */
    public BitVector writeBytes(byte[] data) {
        BitVector v = this;
        for (int i = 0; i < data.length; i++) {
            v = v.writeByte(data[i]);
        }

        return v;
    }

    /**
     * Writes the specified bytes to the stream.
     *
     * @param data   Data containing the bytes to write.
     * @param offset Offset from which to start reading the bytes.
     * @param length Length of the bytes to write.
     * @return Vector with the written bytes.
     */
    public BitVector writeBytes(byte[] data, int offset, int length) {
        BitVector v = this;
        for (int i = 0; i < length; i++) {
            v = v.writeByte(data[offset + i]);
        }

        return v;
    }

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param bit   The bit to set.
     * @return Vector with the bit at $index set to $bit.
     */
    public BitVector setBit(int index, boolean bit) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds. (Size: " + size() + ")");
        }

        return setBitUnchecked(index, bit);
    }

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param value The bit to set (only the least significant bit is considered).
     * @return Vector with the bit at $index set to $bit.
     */
    public BitVector setBit(int index, int value) {
        return setBit(index, (value & 0x1) == 1);
    }

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param value The bit to set (only the least significant bit is considered).
     * @return Vector with the bit at $index set to $bit.
     */
    public BitVector setBit(int index, long value) {
        return setBit(index, (value & 0x1L) == 1L);
    }

    /**
     * @param index Index in the BitVector where to set the byte.
     * @param value The byte to set.
     * @return Vector with the byte at $index set to $value.
     */
    public BitVector setByte(int index, byte value) {
        return setBits(index, value, Byte.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the byte.
     * @param value The byte to set (only the least significant 8 bits are considered).
     * @return Vector with the byte at $index set to $value.
     */
    public BitVector setByte(int index, int value) {
        return setBits(index, value, Byte.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the short.
     * @param value The short to set.
     * @return Vector with the short at $index set to $value.
     */
    public BitVector setShort(int index, short value) {
        return setBits(index, value, Short.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the short.
     * @param value The short to set (only the least significant 16 bits are considered).
     * @return Vector with the short at $index set to $value.
     */
    public BitVector setShort(int index, int value) {
        return setBits(index, value, Short.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the int.
     * @param value The short to set.
     * @return Vector with the int at $index set to $value.
     */
    public BitVector setInt(int index, int value) {
        return setBits(index, value, Integer.SIZE);
    }


    /**
     * @param index Index in the BitVector where to set the long.
     * @param value The short to set.
     * @return Vector with the long at $index set to $value.
     */
    public BitVector setLong(int index, long value) {
        return setBits(index, value, Long.SIZE);
    }

    /**
     * @param index  Index in the BitVector where to set the bits.
     * @param value  Value of which to write the bits from least significant to most significant bit.
     * @param amount Amount of bits to write.
     * @return
     */
    public BitVector setBits(int index, int value, int amount) {
        BitVector v = this;
        for (int i = 0; i < amount; i++) {
            v = v.setBit(index + i, value >>> i);
        }

        return v;
    }

    /**
     * @param index  Index in the BitVector where to set the bits.
     * @param value  Value of which to write the bits from least significant to most significant bit.
     * @param amount Amount of bits to write.
     * @return
     */
    public BitVector setBits(int index, long value, int amount) {
        BitVector v = this;
        for (int i = 0; i < amount; i++) {
            v = v.setBit(index + i, value >>> i);
        }

        return v;
    }

    /**
     * Sets the specified bytes in the stream.
     *
     * @param data Array containing the data to be written.
     * @return Vector with the written bytes.
     */
    public BitVector setBytes(int index, byte[] data) {
        BitVector v = this;
        for (int i = 0; i < data.length; i++) {
            v = v.setByte(index, data[i]);
        }

        return v;
    }

    /**
     * Sets the specified bytes in the stream.
     *
     * @param data   Data containing the bytes to write.
     * @param offset Offset from which to start reading the bytes.
     * @param length Length of the bytes to write.
     * @return Vector with the written bytes.
     */
    public BitVector setBytes(int index, byte[] data, int offset, int length) {
        BitVector v = this;
        for (int i = 0; i < length; i++) {
            v = v.setByte(index, data[offset + i]);
        }

        return v;
    }

    /**
     * @param other BitVector to write.
     * @return BitVector with the other BitVector written at the end.
     */
    public BitVector write(BitVector other) {
        return write(other, 0, other.size());
    }

    /**
     * @param other  BitVector to write.
     * @param offset Offset from which to read the bits in other.
     * @param amount Amount of bits to write.
     * @return
     */
    public BitVector write(BitVector other, int offset, int amount) {
        BitVector v = this;

        for (int i = 0; i < amount; i++) {
            v = v.writeBit(other.get(offset + i));
        }
        return v;
    }

    /**
     * @param values Writes the boolean values as bits to the bitvector.
     * @return BitVector with the bits written at the end.
     */
    public BitVector write(boolean[] values) {
        return write(values, values.length);
    }

    /**
     * @param values Writes the boolean values as bits to the bitvector.
     * @param amount Amount of bits from values to write to the vector.
     * @return
     */
    public BitVector write(boolean[] values, int amount) {
        BitVector v = this;
        for (int i = 0; i < amount; i++) {
            v = v.writeBit(values[i]);
        }

        return v;
    }

    /**
     * @param index Index from which to start setting the bits of other.
     * @param other BitVector to read the bits from.
     * @return BitVector with the bits of other set at $index.
     */
    public BitVector set(int index, BitVector other) {
        return set(index, other, 0, other.size());
    }

    /**
     * @param index  Index from which to start setting the bits of other.
     * @param other  BitVector to read the bits from.
     * @param offset offset from which to start reading in other.
     * @param amount Amount of bits to read from other.
     * @return BitVector with the bits of other set at $index.
     */
    public BitVector set(int index, BitVector other, int offset, int amount) {
        BitVector v = this;

        for (int i = 0; i < amount; i++) {
            v = v.setBit(index + i, other.get(offset + i));
        }
        return v;
    }

    /**
     * @param find    Substring to find within this BitVector.
     * @param replace Replacement string to insert instead of $find within this BitVector.
     * @return BitVector with the first occurance of $find replaced with $replace.
     */
    public BitVector replaceFirst(BitVector find, BitVector replace) {
        return replaceFirst(0, find, replace);
    }


    /**
     * @param other Returns a bitvector that is the logical or of this and the other bitvector.
     * @return Logical Or product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public BitVector or(BitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedOr(other);
    }

    /**
     * @param other Returns a bitvector that is the logical and of this and the other bitvector.
     * @return Logical And product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public BitVector and(BitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedAnd(other);
    }

    /**
     * @param other Returns a bitvector that is the logical xor of this and the other bitvector.
     * @return Logical Xor product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public BitVector xor(BitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedXor(other);
    }
}
