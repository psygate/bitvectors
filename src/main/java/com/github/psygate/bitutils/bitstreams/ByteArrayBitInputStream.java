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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by psygate on 07.10.2017.
 */
public class ByteArrayBitInputStream extends AbstractBitStream {
    private final byte[] data;
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

    public void setPosition(long position) {
        if (position < 0 || position > data.length * 8L) {
            throw new IllegalArgumentException("Position out of bounds: " + position + "/" + (data.length * 8L));
        }

        this.position = position;
    }

    @Override
    protected int readBitsUnchecked(int amount) throws IOException {
        int output = 0;

        for (int i = 0; i < amount; i++) {
            output |= readBitUnchecked() << i;
        }

        return output;
    }

    private int readBitUnchecked() {
        int value = (((data[getByteIndex()] & 0xFF) >> getBitIndex()) & 0x1);

        position++;
        return value;
    }

    public int getByteIndex() {
        return (int) (position / 8L);
    }

    public int getBitIndex() {
        return (int) (position & 0x7);
    }

    @Override
    protected void checkAvailable(long amount) throws IOException {
        long available = data.length * 8L - position;
        if (available < amount) {
            throw new IOException("Not enough bits left in stream. (" + available + "/" + amount + ")");
        }
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

    public long availableBits() {
        return (data.length * 8L) - position;
    }

    @Override
    public int available() throws IOException {
        return (int) (availableBits() / 8L);
    }

    public ByteArrayBitInputStream copy() throws IOException {
        return new ByteArrayBitInputStream(this);
    }

}
