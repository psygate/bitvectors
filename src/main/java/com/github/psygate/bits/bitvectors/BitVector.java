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

package com.github.psygate.bits.bitvectors;

import java.util.Arrays;

import static com.github.psygate.bits.bitvectors.ArrayUtils.packToLong;

/**
 * Created by psygate on 04.09.2017.
 * <p>
 * Implementation of a BitVector. This implementation is modifying in place and will only return a new vector where explicitly required. All set & write methods return this vector.
 */
public class BitVector extends AbstractWriteableBitVector<BitVector> {
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

    @Override
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

    @Override
    protected BitVector setBitUnchecked(int index, boolean bit) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds. (Size: " + size() + ")");
        }

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

    @Override
    public int size() {
        return size;
    }

    @Override
    protected boolean getUnchecked(int position) {
        int longIdx = position / Long.SIZE;
        int bitIdx = position % Long.SIZE;

        return ((bits[longIdx] >> bitIdx) & 1L) == 1L;
    }

    @Override
    protected BitVector subVectorUnchecked(int position, int length) {
        BitVector vec = new BitVector();
        for (int i = 0; i < length; i++) {
            vec.writeBit(get(position + i));
        }

        return vec;
    }

    @Override
    public BitVector empty() {
        return EMPTY();
    }

    public BitVector copy() {
        return new BitVector(bits, size(), true);
    }

    public static BitVector EMPTY() {
        return new BitVector();
    }

    public BitVector replaceFirst(int index, AbstractReadableBitVector find, AbstractReadableBitVector replace) {
        if (isEmpty()) {
            return this;
        }

        int found = indexOf(index, find);

        if (found == -1) {
            return this;
        } else {
            BitVector head = subVectorUnchecked(0, found);
            BitVector tail = subVectorUnchecked(found + find.size(), size() - (found + find.size()));

            BitVector vec = head.write(replace);

            vec = vec.write(tail);

            return vec;
        }
    }

    @Override
    protected BitVector uncheckedOr(AbstractReadableBitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) | getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    @Override
    protected BitVector uncheckedAnd(AbstractReadableBitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) & getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    @Override
    protected BitVector uncheckedXor(AbstractReadableBitVector other) {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < size(); i++) {
            int longIndex = i / Long.SIZE;
            int bitIndex = i % Long.SIZE;

            buffer[longIndex] |= (other.getBit(i) ^ getBit(i)) << bitIndex;
        }

        return new BitVector(buffer, size(), false);
    }

    @Override
    public BitVector not() {
        long[] buffer = new long[bits.length];

        for (int i = 0; i < bits.length; i++) {
            buffer[i] = ~bits[i];
        }

        return new BitVector(buffer, size(), false);
    }
}
