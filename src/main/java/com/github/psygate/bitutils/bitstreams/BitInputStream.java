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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Created by psygate on 01.10.2017.
 * <p>
 * A simple BitInputStream wrapper for input streams.
 */
public class BitInputStream extends AbstractBitStream {
    private final static int BUFFER_SIZE = Byte.SIZE;

    private final InputStream underlying;
    private int buffer;
    private int bufferedBits;

    /**
     * Constructs a new BitInputStream from the input stream.
     *
     * @param in InputStream to read from.
     */
    public BitInputStream(InputStream in) {
        underlying = Objects.requireNonNull(in, "InputStream to BitInputStream cannot be null.");
    }

    protected int readBitsUnchecked(final int amount) throws IOException {
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


    @Override
    protected void checkAvailable(long amount) {

    }

    @Override
    public void close() throws IOException {
        underlying.close();
        bufferedBits = 0;
        buffer = 0;
    }
}
