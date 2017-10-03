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

package com.github.psygate.bitutils.bitstreams;

import java.io.*;
import java.util.Objects;

/**
 * Created by psygate on 01.10.2017.
 * <p>
 * A simple BitInputStream wrapper for input streams.
 */
public class BitInputStream extends InputStream implements AutoCloseable, DataInput {
    private final static int BUFFER_SIZE = Byte.SIZE;

    private final InputStream underlying;
    private int buffer;
    private int bufferedBits;
    private long position;

    /**
     * Constructs a new BitInputStream from the input stream.
     *
     * @param in InputStream to read from.
     */
    public BitInputStream(InputStream in) {
        underlying = Objects.requireNonNull(in, "InputStream to BitInputStream cannot be null.");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int available() throws IOException {
        return underlying.available();
    }

    @Override
    public int read() throws IOException {
        try {
            return readBits(Byte.SIZE);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Reads the specified amount of bits from the stream.
     *
     * @param amount Amount of bits to read.
     * @return Integer containing the requested amount of bits.
     * @throws IOException              If the underlying stream does not contain enough bytes to satisfy the requested amount.
     * @throws IllegalArgumentException If the requested amount of bits too large for an integer or smaller than 0.
     */
    public int readBits(final int amount) throws IOException {
        if (amount > Integer.SIZE || amount < 0) {
            throw new IllegalArgumentException("Requested amount exceeds data type size. (" + amount + "/" + Integer.SIZE + ")");
        } else if (amount == 0) {
            return 0;
        } else {
            if (bufferedBits == 0) {
                fillBuffer();
            }

            int value = 0;
            int readBits = 0;
            while (readBits < amount && bufferedBits > 0) {
                int remaining = (amount - readBits) < BUFFER_SIZE ? amount - readBits : BUFFER_SIZE;

                if (bufferedBits >= remaining) {
                    int readValue = (int) (buffer & fitMask(remaining));

                    value |= readValue << readBits;
                    buffer >>>= remaining;
                    bufferedBits -= remaining;
                    readBits += remaining;
                } else if (bufferedBits > 0) {
                    int readValue = (int) (buffer & fitMask(remaining));

                    value |= readValue << readBits;
                    readBits += bufferedBits;
                    bufferedBits = 0;
                    buffer = 0;
                }

                fillBuffer();
            }

            if (amount - readBits > 0) {
                throw new EOFException("Stream exhausted, failed to read " + amount + " bits. (" + readBits + " bits read.)");
            }

            position += amount;
            return value & fitIntegerMask(amount);
        }
    }

    /**
     * Reads the specified amount of bits from the stream.
     *
     * @param amount Amount of bits to read.
     * @return Long containing the requested amount of bits.
     * @throws IOException              If the underlying stream does not contain enough bytes to satisfy the requested amount.
     * @throws IllegalArgumentException If the requested amount of bits too large for a long or smaller than 0.
     */
    public long readBitsLong(final int amount) throws IOException {
        if (amount <= Integer.SIZE) {
            return readBits(amount);
        } else if (amount < 0 || amount > Long.SIZE) {
            throw new IllegalArgumentException("Requested amount exceeds data type size. (" + amount + "/" + Long.SIZE + ")");
        } else {
            long lowerValue = ((long) readBits(Integer.SIZE)) & 0xFFFFFFFFL;
            long upperValue = (((long) readBits(Integer.SIZE)) & 0xFFFFFFFFL) << Integer.SIZE;

            return (lowerValue | upperValue) & fitMask(amount);
        }
    }

    /**
     * @return Current bit position in the stream.
     */
    public long getPosition() {
        return position;
    }

    private void fillBuffer() throws IOException {
        if (bufferedBits <= 0) {
            int read = underlying.read();
            if (read == -1) {
                bufferedBits = 0;
                buffer = 0;
            } else {
                bufferedBits = Byte.SIZE;
                buffer = read & 0xFF;
            }
        }
    }

    /**
     * This method is unchecked so amounts bigger the Long.SIZE or amounts smaller than 0 may work.
     *
     * @param amount Amount of bits to mask (from the lsb to the msb).
     * @return Masked value.
     */
    private static long fitMask(int amount) {
        return 0xFFFFFFFFFFFFFFFFL >>> (Long.SIZE - amount);
    }

    /**
     * This method is unchecked so amounts bigger the Long.SIZE or amounts smaller than 0 may work.
     *
     * @param amount Amount of bits to mask (from the lsb to the msb).
     * @return Masked value.
     */
    private static int fitIntegerMask(int amount) {
        return 0xFFFFFFFF >>> (Integer.SIZE - amount);
    }

    @Override
    public void close() throws IOException {
        underlying.close();
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            b[i + off] = readByte();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        for (int i = 0; i < n; i++) {
            try {
                readByte();
            } catch (EOFException e) {
                return i;
            }
        }

        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return readUnsignedByte() != 0;
    }

    @Override
    public byte readByte() throws IOException {
        return (byte) readUnsignedByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readBits(Byte.SIZE);
    }

    @Override
    public short readShort() throws IOException {
        return (short) readUnsignedShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readBits(Short.SIZE);
    }

    @Override
    public char readChar() throws IOException {
        return (char) readBits(Character.SIZE);
    }

    @Override
    public int readInt() throws IOException {
        return readBits(Integer.SIZE);
    }

    @Override
    public long readLong() throws IOException {
        return readBitsLong(Long.SIZE);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        return new BufferedReader(new InputStreamReader(this)).readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
}
