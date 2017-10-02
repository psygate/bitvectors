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

package com.github.psygate.bitutils.bitvectors;

import java.util.Arrays;

/**
 * Created by psygate on 02.09.2017.
 * <p>
 * Utility for manipulating arrays.
 */
class ArrayUtils {
    public static boolean[] copy(boolean[] v) {
        return Arrays.copyOf(v, v.length);
    }

    public static boolean[] concat(boolean[] a, boolean[] b) {
        if (a.length == 0) {
            return copy(b);
        } else if (b.length == 0) {
            return copy(a);
        } else {
            boolean[] output = new boolean[a.length + b.length];
            System.arraycopy(a, 0, output, 0, a.length);
            System.arraycopy(b, 0, output, a.length, b.length);

            return output;
        }
    }

    public static boolean[] pad(boolean[] value, int size) {
        boolean[] out = new boolean[size];
        System.arraycopy(value, 0, out, 0, value.length);

        for (int i = 0; i < size - 1; i++) {
            out[i + value.length] = false;
        }

        return out;
    }

    public static boolean[] byteAsBooleanArray(byte value) {
        return byteAsBooleanArray(value & 0xFF);
    }

    public static boolean[] asBooleanArray(int value, int size) {
        if (size > Integer.SIZE) {
            throw new IllegalArgumentException("Size too big for integer: " + size + "/" + Integer.SIZE);
        } else if (size < 0) {
            throw new IllegalArgumentException("Size too small. " + size);
        }

        boolean[] array = new boolean[size];

        for (int i = 0; i < size; i++) {
            array[i] = ((value >> i) & 1) == 1;
        }

        return array;
    }

    public static boolean[] asBooleanArray(long value, int size) {
        if (size > Long.SIZE) {
            throw new IllegalArgumentException("Size too big for long: " + size + "/" + Long.SIZE);
        } else if (size < 0) {
            throw new IllegalArgumentException("Size too small. " + size);
        }

        boolean[] array = new boolean[size];

        for (int i = 0; i < size; i++) {
            array[i] = ((value >> i) & 1) == 1;
        }

        return array;
    }

    public static boolean[] byteAsBooleanArray(int value) {
        return asBooleanArray(value, Byte.SIZE);
    }

    public static boolean[] longAsBooleanArray(long value) {
        return asBooleanArray(value, Long.SIZE);
    }

    public static boolean[] shortAsBooleanArray(int value) {
        return asBooleanArray(value, Short.SIZE);
    }

    public static boolean[] intAsBooleanArray(int value) {
        return asBooleanArray(value, Integer.SIZE);
    }


    public static boolean[] striped(int size) {
        boolean[] values = new boolean[size];

        for (int i = 0; i < size; i++) {
            values[i] = (i & 1) == 0;
        }

        return values;
    }

    public static byte byteAt(boolean[] reference, int index) {
        int value = 0;
        for (int i = 0; i < Byte.SIZE; i++) {
            value |= ((reference[i + index] ? 1 : 0) << i);
        }

        return (byte) value;
    }

    public static short shortAt(boolean[] reference, int index) {
        return (short) ((byteAt(reference, index) & 0xFF) | (byteAt(reference, index + Byte.SIZE) & 0xFF) << Byte.SIZE);
    }

    public static int intAt(boolean[] reference, int index) {
        return ((shortAt(reference, index) & 0xFFFF) | (shortAt(reference, index + Short.SIZE) & 0xFFFF) << Short.SIZE);
    }


    public static long longAt(boolean[] reference, int index) {
        return ((intAt(reference, index) & 0xFFFFFFFFL) | (intAt(reference, index + Integer.SIZE) & 0xFFFFFFFFL) << Integer.SIZE);
    }

    public static boolean[] not(boolean[] values) {
        boolean[] output = new boolean[values.length];

        for (int i = 0; i < values.length; i++) {
            output[i] = !values[i];
        }

        return output;
    }

    public static boolean[] insertByte(int index, boolean[] target, int value) {
        boolean[] tgtcpy = copy(target);
        boolean[] valueArray = byteAsBooleanArray(value);
        System.arraycopy(valueArray, 0, tgtcpy, index, valueArray.length);
        return tgtcpy;
    }

    public static boolean[] insertShort(int index, boolean[] target, int value) {
        boolean[] tgtcpy = copy(target);
        boolean[] valueArray = shortAsBooleanArray(value);
        System.arraycopy(valueArray, 0, tgtcpy, index, valueArray.length);
        return tgtcpy;
    }

    public static boolean[] insertInt(int index, boolean[] target, int value) {
        boolean[] tgtcpy = copy(target);
        boolean[] valueArray = intAsBooleanArray(value);
        System.arraycopy(valueArray, 0, tgtcpy, index, valueArray.length);
        return tgtcpy;
    }

    public static boolean[] insertLong(int index, boolean[] target, long value) {
        boolean[] tgtcpy = copy(target);
        boolean[] valueArray = longAsBooleanArray(value);
        System.arraycopy(valueArray, 0, tgtcpy, index, valueArray.length);
        return tgtcpy;
    }

    public static int bitsAt(boolean[] reference, int index, int size) {
        int out = 0;

        for (int i = 0; i < size; i++) {
            out |= ((reference[i + index] ? 1 : 0) << i);
        }

        return out;
    }

    public static int bitsAtLong(boolean[] reference, int index, int size) {
        int out = 0;

        for (int i = 0; i < size; i++) {
            out |= ((reference[i + index] ? 1 : 0) << i);
        }

        return out;
    }


    public static boolean[] trueArray(int size) {
        boolean[] value = new boolean[size];
        Arrays.fill(value, true);
        return value;
    }

    public static boolean[] falseArray(int size) {
//        boolean[] value = new boolean[size];
        return new boolean[size];
    }

    public static byte[] getBytes(boolean[] reference, int index, int bytes) {
        byte[] values = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            values[i] = byteAt(reference, index + i * 8);
        }

        return values;
    }

    public static boolean[] setBits(int offset, int bitcount, int value, boolean[] reference) {
        boolean[] tgtcpy = copy(reference);

        for (int i = 0; i < bitcount; i++) {
            tgtcpy[offset + i] = ((value >> i) & 0x1) == 1;
        }

        return tgtcpy;
    }

    public static boolean[] setBits(int offset, int bitcount, long value, boolean[] reference) {
        boolean[] tgtcpy = copy(reference);

        for (int i = 0; i < bitcount; i++) {
            tgtcpy[offset + i] = ((value >> i) & 0x1L) == 1L;
        }

        return tgtcpy;
    }

    public static String bitString(boolean[] value) {
        String out = "";

        for (int i = 0; i < value.length; i++) {
            out += (value[i] ? "1" : "0");

            if (i < value.length - 1) {
                out += ", ";
            }
        }

        return out;
    }

    public static int highestBit(int value) {
        int highest = -1;
        for (int i = 0; i < Integer.SIZE && (value >>> i) != 0; i++) {
            if ((value >>> i) != 0) {
                highest = i;
            } else {
                return highest;
            }
        }

        return highest;
    }

    public static int highestBit(long value) {
        int highest = -1;
        for (int i = 0; i < Long.SIZE && (value >>> i) != 0; i++) {
            if ((value >>> i) != 0) {
                highest = i;
            } else {
                return highest;
            }
        }

        return highest;
    }

    public static byte[] byteArray(int... values) {
        byte[] out = new byte[values.length];

        for (int i = 0; i < values.length; i++) {
            if (values[i] < 0 || values[i] > 255) {
                throw new IllegalArgumentException("Cannot convert " + values[i] + " @ " + i + " to byte.");
            }

            out[i] = (byte) values[i];
        }

        return out;
    }

    public static long[] packToLong(String values) {
        if (values.length() == 0) {
            return new long[0];
        }

        int outputSize = arraySize(values.length(), Long.SIZE);
        long[] packed = new long[outputSize];

        long buffer = 0;

        for (int i = 0; i < values.length(); i++) {
            if (i != 0 && ((i % Long.SIZE) == 0)) {
                packed[i / Long.SIZE - 1] = buffer;
                buffer = 0;
            }

            if (values.charAt(i) == '1') {
                buffer |= 1L << (i % Long.SIZE);
            }
        }

        packed[packed.length - 1] = buffer;

        return packed;
    }

    public static long[] packToLong(boolean[] values) {
        if (values.length == 0) {
            return new long[0];
        }

        int outputSize = arraySize(values.length, Long.SIZE);
        long[] packed = new long[outputSize];

        long buffer = 0;

        for (int i = 0; i < values.length; i++) {
            if (i != 0 && ((i % Long.SIZE) == 0)) {
                packed[i / Long.SIZE - 1] = buffer;
                buffer = 0;
            }

            if (values[i]) {
                buffer |= 1L << (i % Long.SIZE);
            }
        }

        packed[packed.length - 1] = buffer;

        return packed;
    }


    public static long[] packToLong(byte[] values) {
        if (values.length == 0) {
            return new long[0];
        }

        int outputSize = arraySize(values.length * 8, Long.SIZE);

        long[] packed = new long[outputSize];

        for (int longIndex = 0; longIndex < packed.length; longIndex++) {
            packed[longIndex] = packToLong(values, longIndex * Long.BYTES);
        }

        return packed;
    }

    public static long[] packToLong(short[] values) {
        if (values.length == 0) {
            return new long[0];
        }

        int outputSize = arraySize(values.length * 8, Short.SIZE);

        long[] packed = new long[outputSize];

        for (int longIndex = 0; longIndex < packed.length; longIndex++) {
            packed[longIndex] = packToLong(values, longIndex * Long.BYTES);
        }

        return packed;
    }

    public static long[] packToLong(int[] values) {
        if (values.length == 0) {
            return new long[0];
        }

        int outputSize = arraySize(values.length * 8, Integer.SIZE);

        long[] packed = new long[outputSize];

        for (int longIndex = 0; longIndex < packed.length; longIndex++) {
            packed[longIndex] = packToLong(values, longIndex * Long.BYTES);
        }

        return packed;
    }

    public static long packToLong(byte[] values, int offset) {
        long buffer = 0;

        for (int byteIndex = 0; byteIndex < Long.BYTES && (byteIndex + offset < values.length); byteIndex++) {
            buffer |= (values[byteIndex + offset] & 0xFFL) << (byteIndex * Byte.SIZE);
        }

        return buffer;
    }

    public static long packToLong(short[] values, int offset) {
        long buffer = 0;

        for (int shortIndex = 0; shortIndex < Long.BYTES / Short.BYTES && (shortIndex + offset < values.length); shortIndex++) {
            buffer |= (values[shortIndex + offset] & 0xFFFFL) << (shortIndex * Short.SIZE);
        }

        return buffer;
    }

    public static long packToLong(int[] values, int offset) {
        long buffer = 0;

        for (int intIndex = 0; intIndex < Long.BYTES / Integer.BYTES && (intIndex + offset < values.length); intIndex++) {
            buffer |= (values[intIndex + offset] & 0xFFFF_FFFFL) << (intIndex * Integer.SIZE);
        }

        return buffer;
    }


    public static int arraySize(int size, int containerSize) {
        if (!hasRest(size, containerSize)) {
            return size / containerSize;
        } else {
            return size / containerSize + 1;
        }
    }

    public static boolean hasRest(int size, int containerSize) {
        return (size % containerSize) != 0;
    }
}
