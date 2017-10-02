package com.github.psygate.bitutils.bitstreams;

import com.github.psygate.bitutils.bitstreams.BitInputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by psygate on 02.10.2017.
 */
public class BitInputStreamTest {
    @Test
    public void testReadBits() throws IOException {
        new BitInputStream(new ByteArrayInputStream(new byte[0])).readBits(0);
        new BitInputStream(new ByteArrayInputStream(new byte[1])).readBits(8);
    }

    @Test
    public void testReadBits1() throws IOException {
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(new byte[]{(byte) 0xFF}));
        Assert.assertEquals(0x3, in.readBits(2));
        Assert.assertEquals(0xFF >>> 2, in.readBits(6));
    }

    @Test
    public void testReadBits2() throws IOException {
        int SIZE = 256;
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) i;
        }
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(data));
        byte[] dataRead = new byte[SIZE];
        in.read(dataRead);
        Assert.assertArrayEquals(data, dataRead);
    }

    @Test
    public void testReadBits3() throws IOException {
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(new byte[]{(byte) 0xFF, 0x1}));
        Assert.assertEquals(0x3, in.readBits(2));
        Assert.assertEquals(0xFF >>> 2, in.readBits(6));
        Assert.assertEquals(1, in.readBits(8));
    }

    @Test
    public void testReadBits4() throws IOException {
        int SIZE = 256;
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) i;
        }
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(data));

        Assert.assertEquals(data[0] & 0xF, in.readBits(4));
        Assert.assertEquals(4, in.getPosition());

        for (int i = 0; i < SIZE - 1; i++) {
            int value = in.readBits(Byte.SIZE);
            Assert.assertEquals(4 + ((i + 1) * 8), in.getPosition());

            int checkLower = (data[i] >> 4) & 0xF;
            int checkUpper = ((data[i + 1] & 0xF) << 4);
            int checkValue = checkLower | checkUpper;
            Assert.assertEquals("Failure @" + i, checkValue, value);
        }
    }

    @Test
    public void testReadBits6() throws IOException {
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(new byte[]{0x0, 0x1}));
        in.readBits(4);
        Assert.assertEquals(16, in.readBits(8));
    }

    @Test
    public void testReadBits5() throws IOException {
        int SIZE = 256;
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) i;
        }
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(data));

        for (int i = 0; i < SIZE; i++) {
            int read = data[i] & 0xFF;
            Assert.assertEquals(read & 0x3, in.readBits(2));
            Assert.assertEquals(read >>> 2, in.readBits(6));
        }
    }


    @Test(expected = IOException.class)
    public void testReadBitsFail1() throws IOException {
        new BitInputStream(new ByteArrayInputStream(new byte[0])).readBits(1);
    }

    @Test(expected = IOException.class)
    public void testReadBitsFail2() throws IOException {
        new BitInputStream(new ByteArrayInputStream(new byte[1])).readBits(9);
    }

    @Test
    public void testReadBits7() throws IOException {
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(new byte[]{0x1, 0x2, 0x3, 0x4}));
        Assert.assertEquals(0x04030201, in.readBits(Integer.SIZE));

        BitInputStream in2 = new BitInputStream(new ByteArrayInputStream(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}));
        Assert.assertEquals(0xFFFFFFFF, in2.readBits(Integer.SIZE));
    }

    @Test
    public void testReadBits8() throws IOException {
        int SIZE = 256;
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) i;
        }
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(data));
        for (int i = 0; i < SIZE; i += Integer.BYTES) {
            int value = 0;
            for (int j = 0; j < Integer.BYTES; j++) {
                value |= ((data[i + j] & 0xFF) << (j * Byte.SIZE));
            }

            int readvalue = in.readBits(Integer.SIZE);
            Assert.assertEquals(
                    "Mismatch: @" + i + ", " + Integer.toHexString(value) + "/" + Integer.toHexString(readvalue),
                    value,
                    readvalue
            );
        }
    }

    @Test
    public void testReadBits9() throws IOException {
        int SIZE = 256;
        byte[] data = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            data[i] = (byte) i;
        }
        BitInputStream in = new BitInputStream(new ByteArrayInputStream(data));
        for (int i = 0; i < SIZE; i += Long.BYTES) {
            long value = 0;
            for (int j = 0; j < Long.BYTES; j++) {
                long local = data[i + j] & 0xFF;
                value |= local << (j * Byte.SIZE);
            }

            long readvalue = in.readBitsLong(Long.SIZE);
            Assert.assertEquals(
                    "Mismatch: @" + i + ", " + Long.toHexString(value) + "/" + Long.toHexString(readvalue),
                    value,
                    readvalue
            );
        }
    }
}