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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Created by psygate on 01.10.2017.
 * <p>
 * A simple BitInputStream wrapper for input streams.
 */
public class BitOutputStream extends OutputStream implements AutoCloseable, DataOutput {
    //This MUST be a multiple of 8.
    private final static int BUFFER_SIZE = Byte.SIZE;

    private final OutputStream underlying;
    private int buffer;
    private int bufferedBits;
    private long position;

    /**
     * Constructs a new BitOutputStream from the output stream.
     *
     * @param out OutputStream to write to.
     */
    public BitOutputStream(OutputStream out) {
        underlying = Objects.requireNonNull(out, "OutputStream to BitOutputStream cannot be null.");
    }

    public void writeBits(int bits, int amount) throws IOException {
        if (amount < 0 || amount > Integer.SIZE) {
            throw new IllegalArgumentException("Requested amount exceeds data type size. (" + amount + "/" + Integer.SIZE + ")");
        } else if (amount == 0) {
            //Skip
        } else {
            int writeable = BUFFER_SIZE - bufferedBits;

            if (amount <= writeable) {
                buffer |= (bits & fitMask(amount)) << bufferedBits;
                bufferedBits += amount;
            } else if (amount > writeable) {
                buffer |= (bits & fitMask(writeable)) << bufferedBits;
                bufferedBits += writeable;
                flushBuffer(false);
                writeBits(bits >>> writeable, amount - writeable);
            }

            flushBuffer(false);
            position += amount;
        }
    }

    public void writeBitsLong(long bits, int amount) throws IOException {
        if (amount < 0 || amount > Long.SIZE) {
            throw new IllegalArgumentException("Requested amount exceeds data type size. (" + amount + "/" + Long.SIZE + ")");
        } else if (amount == 0) {
            //Skip
        } else {
            int lower = (int) (bits & 0xFFFFFFFF);
            int upper = (int) ((bits >>> 32) & 0xFFFFFFFF);
            int lowerwrite = Math.min(amount, 32);
            int upperwrite = Math.max(0, amount - 32);
            writeBits(lower, lowerwrite);
            writeBits(upper, upperwrite);

            position += amount;
        }
    }

    private void flushBuffer(final boolean force) throws IOException {
        if (bufferedBits == BUFFER_SIZE || force) {
            for (int i = 0; i < BUFFER_SIZE && (i * BUFFER_SIZE) < bufferedBits; i++) {
                underlying.write((buffer >>> (i * Byte.SIZE)) & 0xFF);
            }
            bufferedBits = 0;
            buffer = 0;
        }
    }


    @Override
    public void flush() throws IOException {
        flushBuffer(bufferedBits > 0);
        underlying.flush();
    }

    @Override
    public void close() throws IOException {
        flushBuffer(bufferedBits > 0);
        underlying.close();
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
     * @return Current bit position in the stream.
     */
    public long getPosition() {
        return position;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        writeBits((v) ? 1 : 0, Byte.SIZE);
    }

    @Override
    public void writeByte(int v) throws IOException {
        writeBits(v, Byte.SIZE);
    }

    @Override
    public void writeShort(int v) throws IOException {
        writeBits(v, Short.SIZE);
    }

    @Override
    public void writeChar(int v) throws IOException {
        writeBits(v >> (Byte.SIZE), Byte.SIZE); //Write high order bits
        writeBits(v, Byte.SIZE);    //Write low order bits.
    }

    @Override
    public void writeInt(int v) throws IOException {
        writeBits(v, Integer.SIZE);
    }

    @Override
    public void writeLong(long v) throws IOException {
        writeBitsLong(v, Long.SIZE);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(String s) throws IOException {
        try {
            s.chars().map(v -> v & 0xFF).forEach(v -> {
                try {
                    writeByte(v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        try {
            s.chars().forEach(v -> {
                try {
                    writeChar(v);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        new DataOutputStream(this).writeUTF(s);
    }

    @Override
    public void write(int b) throws IOException {
        writeBits(b, Byte.SIZE);
    }
}
