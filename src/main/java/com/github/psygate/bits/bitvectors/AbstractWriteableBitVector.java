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

/**
 * Created by psygate on 01.09.2017.
 * <p>
 * AbstractWriteableBitVector represents a readable and writeable bitvector.
 * <p>
 * set$TYPE(position) methods are a short hand for getBits(position, TYPE.SIZE)
 * write$TYPE(position) methods are a short hand for getBits(position, TYPE.SIZE)
 * <p>
 * Write methods append to the vector, extending it.
 * Set methods set already existing bits, extending when necessary.
 */
public abstract class AbstractWriteableBitVector<T extends AbstractWriteableBitVector<T>> extends AbstractReadableBitVector<T> {
    /**
     * @param bit Bit to write.
     * @return Vector with the written bit.
     */
    public abstract T writeBit(boolean bit);

    /**
     * @param bit Bit to write. Only the least significant bit is considered.
     * @return Vector with the written bit.
     */
    public T writeBit(int bit) {
        return writeBit((bit & 0x1) == 1);
    }


    /**
     * @param bit Bit to write. Only the least significant bit is considered.
     * @return Vector with the written bit.
     */
    public T writeBit(long bit) {
        return writeBit((bit & 0x1) == 1);
    }

    /**
     * @param value Byte to write to the vector.
     * @return Vector with the written byte.
     */
    public T writeByte(byte value) {
        return writeByte(value & 0xFF);
    }

    /**
     * @param value Byte to write to the vector. Only the least significant 8 bits are considered.
     * @return Vector with the written byte.
     */
    public T writeByte(int value) {
        T v = (T) this;

        for (int i = 0; i < Byte.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Short value to write to the vector.
     * @return Vector with the written short value.
     */
    public T writeShort(short value) {
        return writeShort(value & 0xFFFF);
    }

    /**
     * @param value Short value to write. Only the least significant 16 bits are considered.
     * @return Vector with the written short value.
     */
    public T writeShort(int value) {
        T v = (T) this;
        for (int i = 0; i < Short.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Int value to write.
     * @return Vector with the written int value.
     */
    public T writeInt(int value) {
        T v = (T) this;
        for (int i = 0; i < Integer.SIZE; i++) {
            v = v.writeBit(value >>> i);
        }

        return v;
    }

    /**
     * @param value Long value to write.
     * @return Vector with the written long value.
     */
    public T writeLong(long value) {
        T v = (T) this;
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
    public T writeBits(int value, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bit count too small. " + amount);
        } else if (amount > Integer.SIZE) {
            throw new IllegalArgumentException("Bit count exceeds maximum size. " + amount + "/" + Integer.SIZE);
        }

        T v = (T) this;
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
    public T writeBits(long value, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Bit count too small. " + amount);
        } else if (amount > Long.SIZE) {
            throw new IllegalArgumentException("Bit count exceeds maximum size. " + amount + "/" + Long.SIZE);
        }

        T v = (T) this;

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
    public T writeBytes(byte[] data) {
        T v = (T) this;
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
    public T writeBytes(byte[] data, int offset, int length) {
        T v = (T) this;
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
    public T setBit(int index, boolean bit) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds. (Size: " + size() + ")");
        }

        return setBitUnchecked(index, bit);
    }

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param bit   The bit to set.
     * @return Vector with the bit at $index set to $bit.
     */
    protected abstract T setBitUnchecked(int index, boolean bit);

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param value The bit to set (only the least significant bit is considered).
     * @return Vector with the bit at $index set to $bit.
     */
    public T setBit(int index, int value) {
        return setBit(index, (value & 0x1) == 1);
    }

    /**
     * @param index Index in the BitVector where to set the bit.
     * @param value The bit to set (only the least significant bit is considered).
     * @return Vector with the bit at $index set to $bit.
     */
    public T setBit(int index, long value) {
        return setBit(index, (value & 0x1L) == 1L);
    }

    /**
     * @param index Index in the BitVector where to set the byte.
     * @param value The byte to set.
     * @return Vector with the byte at $index set to $value.
     */
    public T setByte(int index, byte value) {
        return setBits(index, value, Byte.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the byte.
     * @param value The byte to set (only the least significant 8 bits are considered).
     * @return Vector with the byte at $index set to $value.
     */
    public T setByte(int index, int value) {
        return setBits(index, value, Byte.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the short.
     * @param value The short to set.
     * @return Vector with the short at $index set to $value.
     */
    public T setShort(int index, short value) {
        return setBits(index, value, Short.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the short.
     * @param value The short to set (only the least significant 16 bits are considered).
     * @return Vector with the short at $index set to $value.
     */
    public T setShort(int index, int value) {
        return setBits(index, value, Short.SIZE);
    }

    /**
     * @param index Index in the BitVector where to set the int.
     * @param value The short to set.
     * @return Vector with the int at $index set to $value.
     */
    public T setInt(int index, int value) {
        return setBits(index, value, Integer.SIZE);
    }


    /**
     * @param index Index in the BitVector where to set the long.
     * @param value The short to set.
     * @return Vector with the long at $index set to $value.
     */
    public T setLong(int index, long value) {
        return setBits(index, value, Long.SIZE);
    }

    /**
     * @param index  Index in the BitVector where to set the bits.
     * @param value  Value of which to write the bits from least significant to most significant bit.
     * @param amount Amount of bits to write.
     * @return
     */
    public T setBits(int index, int value, int amount) {
        T v = (T) this;
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
    public T setBits(int index, long value, int amount) {
        T v = (T) this;
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
    public T setBytes(int index, byte[] data) {
        T v = (T) this;
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
    public T setBytes(int index, byte[] data, int offset, int length) {
        T v = (T) this;
        for (int i = 0; i < length; i++) {
            v = v.setByte(index, data[offset + i]);
        }

        return v;
    }

    /**
     * @param other BitVector to write.
     * @return BitVector with the other BitVector written at the end.
     */
    public T write(AbstractReadableBitVector other) {
        return write(other, 0, other.size());
    }

    /**
     * @param other  BitVector to write.
     * @param offset Offset from which to read the bits in other.
     * @param amount Amount of bits to write.
     * @return
     */
    public T write(AbstractReadableBitVector other, int offset, int amount) {
        T v = (T) this;

        for (int i = 0; i < amount; i++) {
            v = v.writeBit(other.get(offset + i));
        }
        return v;
    }

    /**
     * @param values Writes the boolean values as bits to the bitvector.
     * @return BitVector with the bits written at the end.
     */
    public T write(boolean[] values) {
        return write(values, values.length);
    }

    /**
     * @param values Writes the boolean values as bits to the bitvector.
     * @param amount Amount of bits from values to write to the vector.
     * @return
     */
    public T write(boolean[] values, int amount) {
        T v = (T) this;
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
    public T set(int index, AbstractReadableBitVector other) {
        return set(index, other, 0, other.size());
    }

    /**
     * @param index  Index from which to start setting the bits of other.
     * @param other  BitVector to read the bits from.
     * @param offset offset from which to start reading in other.
     * @param amount Amount of bits to read from other.
     * @return BitVector with the bits of other set at $index.
     */
    public T set(int index, AbstractReadableBitVector other, int offset, int amount) {
        T v = (T) this;

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
    public T replaceFirst(AbstractReadableBitVector find, AbstractReadableBitVector replace) {
        return replaceFirst(0, find, replace);
    }

    /**
     * @param index   Index at which to start searching for find.
     * @param find    Substring to find within this BitVector.
     * @param replace Replacement string to insert instead of $find within this BitVector.
     * @return BitVector with the first occurance after $index of $find replaced with $replace.
     */
    public abstract T replaceFirst(int index, AbstractReadableBitVector find, AbstractReadableBitVector replace);

    /**
     * @param other Returns a bitvector that is the logical or of this and the other bitvector.
     * @return Logical Or product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public T or(AbstractReadableBitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedOr(other);
    }

    /**
     * @param other Returns a bitvector that is the logical or of this and the other bitvector.
     * @return Logical Or product of this and other vector.
     */
    protected abstract T uncheckedOr(AbstractReadableBitVector other);

    /**
     * @param other Returns a bitvector that is the logical and of this and the other bitvector.
     * @return Logical And product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public T and(AbstractReadableBitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedAnd(other);
    }

    /**
     * @param other Returns a bitvector that is the logical and of this and the other bitvector.
     * @return Logical And product of this and other vector.
     */
    protected abstract T uncheckedAnd(AbstractReadableBitVector other);

    /**
     * @param other Returns a bitvector that is the logical xor of this and the other bitvector.
     * @return Logical Xor product of this and other vector.
     * @throws IllegalArgumentException If the other vector is too short or too long.
     */
    public T xor(AbstractReadableBitVector other) {
        if (other.size() != size()) {
            throw new IllegalArgumentException("Size mismatch: " + size() + "/" + other.size());
        }

        return uncheckedXor(other);
    }

    /**
     * @param other Returns a bitvector that is the logical xor of this and the other bitvector.
     * @return Logical Xor product of this and other vector.
     */
    protected abstract T uncheckedXor(AbstractReadableBitVector other);

    /**
     * @return Returns the logical not of this vector.
     */
    public abstract T not();
}
