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

import com.github.psygate.bitutils.bitstreams.BitOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by psygate on 02.10.2017.
 */
public class BitOutputStreamTest {
    @Test
    public void testWriteBits() throws IOException {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(base);

        out.writeBits(0, 0);
        Assert.assertEquals(0, out.getPosition());
        out.close();
        Assert.assertArrayEquals(new byte[0], base.toByteArray());
    }

    @Test
    public void testWriteBits1() throws IOException {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(base);

        out.writeBits(0, 1);
        Assert.assertEquals(1, out.getPosition());
        out.close();
        Assert.assertArrayEquals(new byte[1], base.toByteArray());
    }

    @Test
    public void testWriteBits2() throws IOException {
        for (int i = 0; i < 256; i++) {
            ByteArrayOutputStream base = new ByteArrayOutputStream();
            BitOutputStream out = new BitOutputStream(base);

            for (int j = 0; j < i; j++) {
                out.writeBits(0, 1);
            }

            Assert.assertEquals(i, out.getPosition());
            out.close();

            int size = (i / Byte.SIZE) + ((i % Byte.SIZE) != 0 ? 1 : 0);
            Assert.assertArrayEquals("Failed @" + i + ", " + Arrays.toString(base.toByteArray()), new byte[size], base.toByteArray());
        }
    }

    @Test
    public void testWriteBits3() throws IOException {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(base);

        out.writeBits(0xABCDEF01, Integer.SIZE);
        out.writeBits(0xABCDEF01, Integer.SIZE);
        out.writeBits(0xABCDEF01, Integer.SIZE);
        out.writeBits(0xABCDEF01, Integer.SIZE);

        out.close();
        Assert.assertArrayEquals(concat(getBytes(0xABCDEF01), 4), base.toByteArray());
    }

    @Test
    public void testWriteBits4() throws IOException {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(base);

        out.writeBits(0xFA, 4);

        out.close();
        Assert.assertArrayEquals(new byte[]{(byte) 0xA}, base.toByteArray());
    }

    @Test
    public void testWriteBits5() throws IOException {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(base);

        out.writeBits(0xFA, 8);

        out.close();
        Assert.assertArrayEquals(new byte[]{(byte) 0xFA}, base.toByteArray());
    }

    private static byte[] concat(byte[] bytes, int amount) {
        byte[] output = new byte[bytes.length * amount];

        for (int i = 0; i < amount; i++) {
            System.arraycopy(bytes, 0, output, bytes.length * i, bytes.length);
        }

        return output;
    }

    private static byte[] getBytes(int value) {
        byte[] out = new byte[Integer.BYTES];

        for (int i = 0; i < Integer.BYTES; i++) {
            out[i] = (byte) ((value >>> (i * Byte.SIZE)) & 0xFF);
        }

        return out;
    }
}