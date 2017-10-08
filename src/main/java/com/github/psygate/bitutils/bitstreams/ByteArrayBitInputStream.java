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
import java.util.Map;

/**
 * Created by psygate on 07.10.2017.
 */
public class ByteArrayBitInputStream extends InputStream implements DataInput {
    private final byte[] data;
    private long position = 0;
    private long mark = -1;

    public ByteArrayBitInputStream(byte[] data) {
        this.data = data;
    }

    public ByteArrayBitInputStream(ByteArrayBitInputStream in) throws IOException {
        this.data = in.data;
        this.position = in.position;
        this.mark = in.mark;
    }

    public ByteArrayBitInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;

        while ((read = in.read()) != -1) {
            out.write(read);
        }

        data = out.toByteArray();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    public synchronized void mark() {
        mark = position;
    }

    @Override
    public synchronized void mark(int readlimit) {
        mark = position;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mark == -1) {
            throw new IOException("Mark not set.");
        } else {
            position = mark;
        }
    }

    public void setPosition(long position) {
        if (position < 0 || position > data.length * 8L) {
            throw new IllegalArgumentException("Position out of bounds: " + position + "/" + (data.length * 8L));
        }

        this.position = position;
    }

    public long availableBits() {
        return (data.length * 8L) - position;
    }

    @Override
    public int available() throws IOException {
        return (int) (availableBits() / 8L);
    }

    @Override
    public int read() throws IOException {
        assertReadable(Byte.SIZE);
        return readByte() & 0xFF;
    }

    public long getPosition() {
        return position;
    }

    public int getBytePosition() {
        return (int) (position / 8L);
    }

    public int readBit() throws IOException {
        assertReadable(1);
        int bit = (data[getBytePosition()] & 0xFF) >> (position % 8);
        position++;
        return bit & 0x1;
    }

    public int readBits(int amount) throws IOException {
        if (amount > Integer.SIZE) {
            throw new IllegalArgumentException("Requested bits (" + amount + ") exceed data type size.");
        }
        assertReadable(amount);
        int out = 0;

        for (int i = 0; i < amount; i++) {
            out |= readBit() << i;
        }

        return out;
    }

    public long readBitsLong(int amount) throws IOException {
        if (amount > Long.SIZE) {
            throw new IllegalArgumentException("Requested bits (" + amount + ") exceed data type size.");
        }
        assertReadable(amount);

        if (amount < Integer.SIZE) {
            return readBits(amount) & 0xFFFFFFFFL;
        } else {
            long lower = readBits(Integer.SIZE) & 0xFFFFFFFFL;
            long upper = readBits(amount - Integer.SIZE) & 0xFFFFFFFFL;
            return lower | (upper << Integer.SIZE);
        }
    }

    private void assertReadable(long size) throws IOException {
        if (position + size > data.length * 8L) {
            throw new IOException("Cannot read " + size + " bits from bitstream, end is at " + (data.length * 8L));
        }
    }

    public Map.Entry<byte[], Long> restToByteArray() throws IOException {
        long size = data.length * 8L - position;
        int bytes = ((int) (size / 8L)) + ((size % 8 > 0) ? 1 : 0);
        byte[] output = new byte[bytes];

        for (int i = 0; i < bytes; i++) {
            if (data.length * 8L - position > 8) {
                output[i] = readByte();
            } else {
                output[i] = (byte) (readBits((int) (data.length * 8L - position)));
            }
        }

        return Map.entry(output, size);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        assertReadable(len * 8L);

        for (int i = 0; i < b.length; i++) {
            b[i] = readByte();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int maxSkip = data.length - getBytePosition();
        if (n > maxSkip) {
            position = data.length * 8L;
            return maxSkip;
        } else {
            position += (n * 8L);
            return n;
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        return false;
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

    public ByteArrayBitInputStream copy() throws IOException {
        return new ByteArrayBitInputStream(this);
    }
}
