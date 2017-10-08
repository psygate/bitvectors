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

/**
 * Created by psygate on 08.10.2017.
 */
public abstract class AbstractBitStream extends InputStream implements DataInput, AutoCloseable {
    protected long position = 0;

    public int readBit() throws IOException {
        return readBits(1);
    }

    public int readBits(int amount) throws IOException {
        if (amount < 0 || amount > Integer.SIZE) {
            throw new IOException("Requested bit amount exceeds data type. (" + amount + "/" + Integer.SIZE + ")");
        } else {
            checkAvailable(amount);
            int value = readBitsUnchecked(amount);
            return value;
        }
    }

    public long readBitsLong(int amount) throws IOException {
        if (amount < 0 || amount > Long.SIZE) {
            throw new IOException("Requested bit amount exceeds data type. (" + amount + "/" + Integer.SIZE + ")");
        } else if (amount <= Integer.SIZE) {
            return readBits(amount) & 0xFFFFFFFFL;
        } else {
            long lower = (readBits(Integer.SIZE) & 0xFFFFFFFFL);
            long upper = (readBits(amount - Integer.SIZE) & 0xFFFFFFFFL) << Integer.SIZE;
            return lower | upper;
        }
    }


    public long getPosition() {
        return position;
    }

    protected void setPosition(long position) {
        this.position = position;
    }

    protected abstract int readBitsUnchecked(int amount) throws IOException;

    protected abstract void checkAvailable(long amount) throws IOException;

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        checkAvailable(len * 8L);
        if (off + len > b.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + (off + len) + "/" + b.length);
        }

        for (int i = 0; i < len; i++) {
            b[off + i] = readByte();
        }
    }

    @Override
    public int skipBytes(int n) {
        int discarded = 0;
        try {
            for (int i = 0; i < n; i++) {
                readUnsignedByte();
                discarded++;
            }

            return n;
        } catch (IOException e) {
            return discarded;
        }
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
        return (char) readUnsignedShort();
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

    @Override
    public int read() throws IOException {
        return readUnsignedByte();
    }

    /**
     * This method is unchecked so amounts bigger the Long.SIZE or amounts smaller than 0 may work.
     *
     * @param amount Amount of bits to mask (from the lsb to the msb).
     * @return Masked value.
     */
    protected static int fitIntegerMask(int amount) {
        return 0xFFFFFFFF >>> (Integer.SIZE - amount);
    }

    /**
     * This method is unchecked so amounts bigger the Long.SIZE or amounts smaller than 0 may work.
     *
     * @param amount Amount of bits to mask (from the lsb to the msb).
     * @return Masked value.
     */
    protected static long fitMask(int amount) {
        return 0xFFFFFFFFFFFFFFFFL >>> (Long.SIZE - amount);
    }
}
