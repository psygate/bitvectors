package com.github.psygate.bitutils.bitvectors;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static com.github.psygate.bitutils.bitvectors.ArrayUtils.*;
import static org.junit.Assert.*;

/**
 * Created by psygate on 04.09.2017.
 */
public class BitVectorTest {
    public static Random random() {
        return new Random(175195L);
    }

    @Test
    public void toBooleanArray() throws Exception {
        assertArrayEquals(new boolean[0], new BitVector().toBooleanArray());
        assertArrayEquals(new boolean[0], BitVector.emptyVector().toBooleanArray());
        assertArrayEquals(new boolean[0], new BitVector(0).toBooleanArray());
        assertArrayEquals(new boolean[0], BitVector.of(new byte[0]).toBooleanArray());
        assertArrayEquals(new boolean[0], BitVector.of(new long[0]).toBooleanArray());
        assertArrayEquals(new boolean[0], BitVector.of(new boolean[0]).toBooleanArray());

        for (int i = 0; i < 256; i++) {
            assertArrayEquals(byteAsBooleanArray(i), BitVector.of(byteAsBooleanArray(i)).toBooleanArray());
        }

        for (int i = 0; i < 256; i++) {
            int value = 0xFFAB0000 | i;
            assertArrayEquals(intAsBooleanArray(value), BitVector.of(intAsBooleanArray(value)).toBooleanArray());
        }

        for (int i = 0; i < 256; i++) {
            boolean[] value = concat(longAsBooleanArray(0xFFAB0000 | i), longAsBooleanArray(0xFFAB0000 | i));
            assertArrayEquals(value, BitVector.of(value).toBooleanArray());
        }
    }

    @Test
    public void ofTest() {
        assertArrayEquals(new boolean[]{true}, BitVector.of(true).toBooleanArray());
        assertArrayEquals(new boolean[]{false}, BitVector.of(false).toBooleanArray());

        for (int i = 0; i < 256; i++) {
            assertArrayEquals(byteAsBooleanArray(i), BitVector.of((byte) i).toBooleanArray());
        }

        for (int i = 0; i < 0xFFFF + 1; i++) {
            assertArrayEquals(shortAsBooleanArray(i), BitVector.of((short) i).toBooleanArray());
        }

        assertArrayEquals(intAsBooleanArray(0), BitVector.of(0).toBooleanArray());
        assertArrayEquals(intAsBooleanArray(1), BitVector.of(1).toBooleanArray());
        assertArrayEquals(intAsBooleanArray(0xABCD_1234), BitVector.of(0xABCD_1234).toBooleanArray());

        assertArrayEquals(longAsBooleanArray(0), BitVector.of(0L).toBooleanArray());
        assertArrayEquals(longAsBooleanArray(1), BitVector.of(1L).toBooleanArray());
        assertArrayEquals(longAsBooleanArray(0xABCD_1234_9876_5EF0L), BitVector.of(0xABCD_1234_9876_5EF0L).toBooleanArray());

    }

    @Test
    public void equalityTest() {
        assertFalse(new BitVector(0).equals(new Object()));
        assertFalse(new BitVector(12).equals(new BitVector(13)));
        assertFalse(BitVector.of(new boolean[]{true, false, true}).equals(BitVector.of(new boolean[]{true, true, true})));
        BitVector v = BitVector.of(new boolean[]{true, false, true});
        assertTrue(v.equals(v));
    }

    @Test
    public void writeBit() throws Exception {
        int TEST_SIZE = Long.SIZE * 8;

        assertArrayEquals(new boolean[0], new BitVector().toBooleanArray());
        boolean[] values = striped(TEST_SIZE);

        BitVector vec = new BitVector();

        for (int i = 0; i < values.length; i++) {
            vec.writeBit(values[i]);
            assertEquals(i + 1, vec.size());
            assertArrayEquals(Arrays.copyOf(values, i + 1), vec.toBooleanArray());
        }

        BitVector vec2 = new BitVector();

        boolean[] values2 = not(striped(TEST_SIZE));

        for (int i = 0; i < values2.length; i++) {
            vec2.writeBit(values2[i]);
            assertEquals(i + 1, vec2.size());
            assertArrayEquals(Arrays.copyOf(values2, i + 1), vec2.toBooleanArray());
        }


        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            BitVector write = new BitVector();
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
                write.writeBit(check[j]);
            }

            assertArrayEquals(check, write.toBooleanArray());
        }
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void setBitThrows() throws Exception {
        BitVector.of(new boolean[256]).setBit(-1, true);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void setBitThrows1() throws Exception {
        BitVector.of(new boolean[256]).setBit(257, true);
    }

    @Test
    public void setBit() throws Exception {
        int TEST_SIZE = Long.SIZE * 8;

        assertArrayEquals(new boolean[0], new BitVector().toBooleanArray());
        boolean[] values = new boolean[TEST_SIZE];
        boolean[] reference = striped(TEST_SIZE);

        BitVector vec = BitVector.of(new boolean[TEST_SIZE]);

        assertEquals(TEST_SIZE, vec.size());

        for (int i = 0; i < values.length; i++) {
            vec.setBit(i, reference[i]);
            assertEquals(TEST_SIZE, vec.size());
            assertArrayEquals(concat(Arrays.copyOf(reference, i + 1), Arrays.copyOf(values, values.length - (i + 1))), vec.toBooleanArray());
        }

        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            BitVector write = BitVector.of(new boolean[size]);
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
                write.setBit(j, check[j]);
            }

            assertArrayEquals(check, write.toBooleanArray());
        }
    }

    @Test
    public void size() throws Exception {
        for (int i = 0; i < Long.SIZE * 8; i++) {
            assertEquals(i, BitVector.of(new boolean[i]).size());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getThrows() throws Exception {
        BitVector.of(new boolean[256]).get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getThrows1() throws Exception {
        BitVector.of(new boolean[256]).get(257);
    }

    @Test
    public void get() throws Exception {
        BitVector vec = BitVector.of(new boolean[Long.SIZE * 8]);
        for (int i = 0; i < vec.size(); i++) {
            assertEquals(false, vec.get(i));
        }

        boolean[] trueArray = new boolean[Long.SIZE * 8];
        Arrays.fill(trueArray, true);
        BitVector vec2 = BitVector.of(trueArray);

        for (int i = 0; i < vec2.size(); i++) {
            assertEquals(true, vec2.get(i));
        }

        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
            }
            BitVector vec3 = BitVector.of(Arrays.copyOf(check, check.length));

            for (int j = 0; j < check.length; j++) {
                assertEquals(check[j], vec3.get(j));
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVectorThrows() throws Exception {
        BitVector.emptyVector().subVector(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVectorThrows1() throws Exception {
        BitVector.emptyVector().subVector(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVectorThrows2() throws Exception {
        BitVector.emptyVector().subVector(0, 1);
    }

    @Test
    public void subVector() throws Exception {
        assertEquals(BitVector.emptyVector(), BitVector.emptyVector().subVector(0, 0));

        for (int position = 0; position < 8; position++) {
            for (int length = 0; length < Byte.SIZE - position; length++) {
                assertEquals(BitVector.of(Arrays.copyOfRange(byteAsBooleanArray(0xFF), position, position + length)), BitVector.of(new byte[]{(byte) 0xFF}).subVector(position, length));
            }
        }

        boolean[] doubled = concat(longAsBooleanArray(-1L), longAsBooleanArray(-1L));

        for (int position = 0; position < doubled.length; position++) {
            for (int length = 0; length < doubled.length - position; length++) {
                assertEquals(BitVector.of(Arrays.copyOfRange(doubled, position, position + length)), BitVector.of(copy(doubled)).subVector(position, length));
            }
        }

        boolean[] doubled2 = striped(doubled.length);

        for (int position = 0; position < doubled2.length; position++) {
            for (int length = 0; length < doubled2.length - position; length++) {
                assertEquals(BitVector.of(Arrays.copyOfRange(doubled2, position, position + length)), BitVector.of(copy(doubled2)).subVector(position, length));
            }
        }
    }

    @Test
    public void writeBit1() throws Exception {
        int TEST_SIZE = Long.SIZE * 8;

        assertArrayEquals(new boolean[0], new BitVector().toBooleanArray());
        boolean[] values = striped(TEST_SIZE);

        BitVector vec = new BitVector();

        for (int i = 0; i < values.length; i++) {
            vec.writeBit(values[i] ? 0xFFFFFFFF : 0xFFFFFFFE);
            assertEquals(i + 1, vec.size());
            assertArrayEquals(Arrays.copyOf(values, i + 1), vec.toBooleanArray());
        }

        BitVector vec2 = new BitVector();

        boolean[] values2 = not(striped(TEST_SIZE));

        for (int i = 0; i < values2.length; i++) {
            vec2.writeBit(values2[i] ? 0xFFFFFFFF : 0xFFFFFFFE);
            assertEquals(i + 1, vec2.size());
            assertArrayEquals(Arrays.copyOf(values2, i + 1), vec2.toBooleanArray());
        }


        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            BitVector write = new BitVector();
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
                write.writeBit(check[j] ? 0xFFFFFFFF : 0xFFFFFFFE);
            }

            assertArrayEquals(check, write.toBooleanArray());
        }
    }

    @Test
    public void writeBit2() throws Exception {
        int TEST_SIZE = Long.SIZE * 8;

        assertArrayEquals(new boolean[0], new BitVector().toBooleanArray());
        boolean[] values = striped(TEST_SIZE);

        BitVector vec = new BitVector();

        for (int i = 0; i < values.length; i++) {
            vec.writeBit(values[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
            assertEquals(i + 1, vec.size());
            assertArrayEquals(Arrays.copyOf(values, i + 1), vec.toBooleanArray());
        }

        BitVector vec2 = new BitVector();

        boolean[] values2 = not(striped(TEST_SIZE));

        for (int i = 0; i < values2.length; i++) {
            vec2.writeBit(values2[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
            assertEquals(i + 1, vec2.size());
            assertArrayEquals(Arrays.copyOf(values2, i + 1), vec2.toBooleanArray());
        }


        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            BitVector write = new BitVector();
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
                write.writeBit(check[j] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
            }

            assertArrayEquals(check, write.toBooleanArray());
        }
    }


    @Test
    public void writeByte() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int i = 0; i < 256; i++) {
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeByte((byte) i);
                assertArrayEquals(concat(padding, byteAsBooleanArray(i)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void writeByte1() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int b = 0; b < 256; b++) {
                int value = 0xFFFFFFF0 | b;
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeByte(value);
                assertArrayEquals(concat(padding, byteAsBooleanArray(value)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void writeShort() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int b = 0; b < 256; b++) {
                short value = (short) (b | 0xFFFFAB00);
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeShort(value);
                assertArrayEquals(concat(padding, shortAsBooleanArray(value)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void writeShort1() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int b = 0; b < 256; b++) {
                int value = (b | 0xFFFFAB00);
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeShort(value);
                assertArrayEquals(concat(padding, shortAsBooleanArray(value)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void writeInt() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int b = 0; b < 256; b++) {
                int value = (b | 0xABCDEF00);
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeInt(value);
                assertArrayEquals(concat(padding, intAsBooleanArray(value)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void writeLong() throws Exception {
        for (int offset = 0; offset < Long.SIZE + 2; offset++) {
            boolean[] padding = new boolean[offset];
            for (int b = 0; b < 256; b++) {
                long value = (b | 0xABCDEF1234567890L);
                BitVector vec = BitVector.emptyVector();
                vec = vec.write(padding);
                vec = vec.writeLong(value);
                assertArrayEquals(concat(padding, longAsBooleanArray(value)), vec.toBooleanArray());
            }
        }
    }

    @Test
    public void setBit1() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i]);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setBit2() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFF : 0xFFFFFFFE);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setBit3() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setByte() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Byte.SIZE; i++) {
            for (int value = 0; value < 256; value++) {
                boolean[] output = insertByte(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setByte(i, (byte) value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }


    @Test
    public void setByte1() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Byte.SIZE; i++) {
            for (int value = 0; value < 256; value++) {
                boolean[] output = insertByte(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setByte(i, value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setShort() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Short.SIZE; i++) {
            for (int b = 0; b < 256; b++) {
                short value = (short) (b | 0xFF00);
                boolean[] output = insertShort(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setShort(i, value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setShort1() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Short.SIZE; i++) {
            for (int b = 0; b < 256; b++) {
                int value = (b | 0xFFFFAB00);
                boolean[] output = insertShort(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setShort(i, value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setInt() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Integer.SIZE; i++) {
            for (int b = 0; b < 256; b++) {
                int value = (b | 0xABCDEF00);
                boolean[] output = insertInt(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setInt(i, value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setLong() throws Exception {
        boolean[] testArray = new boolean[Long.SIZE * 8];

        for (int i = 0; i < testArray.length - Long.SIZE; i++) {
            for (int b = 0; b < 256; b++) {
                long value = (b | 0xFFFFFFFF_FFFFAB00L);
                boolean[] output = insertLong(i, testArray, value);
                BitVector vec = BitVector.of(new boolean[testArray.length]);
                vec.setLong(i, value);
                assertArrayEquals(output, vec.toBooleanArray());
            }
        }
        BitVector vec = BitVector.of(copy(testArray));
        boolean[] striped = striped(testArray.length);

        for (int i = 0; i < striped.length; i++) {
            vec.setBit(i, striped[i] ? 0xFFFFFFFFFFFFFFFFL : 0xFFFFFFFFFFFFFFFEL);
        }

        assertArrayEquals(striped, vec.toBooleanArray());
    }

    @Test
    public void setBitsTest() throws Exception {
        for (int bitcount = 0; bitcount < 128; bitcount++) {
            for (int offset = 0; offset < Long.SIZE * 3 - bitcount; offset++) {
                boolean[] reference = new boolean[Long.SIZE * 8];

                boolean[] finalReference = setBits(offset, bitcount, bitcount, reference);

                BitVector vec = new BitVector(reference.length);
                vec.setBits(offset, bitcount, bitcount);


                assertArrayEquals(finalReference, vec.toBooleanArray());
            }
        }
    }


    @Test
    public void setBits1() throws Exception {
        for (long bitcount = 0; bitcount < 128; bitcount++) {
            for (int offset = 0; offset < Long.SIZE * 3 - bitcount; offset++) {
                boolean[] reference = new boolean[Long.SIZE * 8];

                boolean[] finalReference = setBits(offset, (int) bitcount, bitcount, reference);

                BitVector vec = new BitVector(reference.length);
                vec.setBits(offset, bitcount, (int) bitcount);


                assertArrayEquals(finalReference, vec.toBooleanArray());
            }
        }
    }

    @Test
    public void write() throws Exception {
        assertEquals(BitVector.emptyVector(), BitVector.emptyVector().write(BitVector.emptyVector()));

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = new boolean[Long.SIZE * 8];
            BitVector vec = new BitVector();
            vec.write(BitVector.of(Arrays.copyOf(source, i)));
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = new boolean[Long.SIZE * 8];
            Arrays.fill(source, true);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(Arrays.copyOf(source, i)));
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = striped(Long.SIZE * 8);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(Arrays.copyOf(source, i)));
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = striped(Long.SIZE * 8);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(Arrays.copyOf(source, i)));
            vec.write(BitVector.of(Arrays.copyOfRange(source, i, source.length)));
            assertEquals(BitVector.of(Arrays.copyOf(source, source.length)), vec);
        }
    }

    @Test
    public void write1() throws Exception {
        assertEquals(BitVector.emptyVector(), BitVector.emptyVector().write(BitVector.emptyVector(), 0, 0));

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = new boolean[Long.SIZE * 8];
            BitVector vec = new BitVector();
            vec.write(BitVector.of(source), 0, i);
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = new boolean[Long.SIZE * 8];
            Arrays.fill(source, true);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(source), 0, i);
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = striped(Long.SIZE * 8);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(source), 0, i);
            assertEquals(BitVector.of(Arrays.copyOf(source, i)), vec);
        }

        for (int i = 0; i < Long.SIZE * 8; i++) {
            boolean[] source = striped(Long.SIZE * 8);
            BitVector vec = new BitVector();
            vec.write(BitVector.of(source), 0, i);
            vec.write(BitVector.of(Arrays.copyOfRange(source, i, source.length)));
            assertEquals(BitVector.of(Arrays.copyOf(source, source.length)), vec);
        }

        for (int j = 0; j < Long.SIZE; j++) {
            for (int i = 0; i < Long.SIZE * 2; i++) {
                boolean[] source = striped(Long.SIZE * 8);
                BitVector vec = new BitVector();
                vec.write(BitVector.of(source), j, i);
                assertEquals(BitVector.of(Arrays.copyOfRange(source, j, j + i)), vec);
            }
        }
    }

    @Test
    public void set() throws Exception {
        BitVector.emptyVector().set(0, BitVector.emptyVector(), 0, 0);

        for (int index = 0; index < Long.SIZE * 2; index++) {
            for (int offset = 0; offset < 16; offset++) {
                for (int bitcount = 0; bitcount < 16 - offset; bitcount++) {

                    boolean[] insert = striped(Long.SIZE); //new boolean[Long.SIZE];
                    BitVector insertvec = BitVector.of(copy(insert));

                    BitVector original = new BitVector(Long.SIZE * 8);

                    original.set(index, insertvec, offset, bitcount);

                    boolean[] referencearray = new boolean[Long.SIZE * 8];
                    System.arraycopy(striped(Long.SIZE), offset, referencearray, index, bitcount);

                    assertArrayEquals(referencearray, original.toBooleanArray());
                }
            }
        }
    }

    @Test
    public void set1() throws Exception {
        BitVector.emptyVector().set(0, BitVector.emptyVector());

        for (int index = 0; index < Long.SIZE * 2; index++) {
//            for (int offset = 0; offset < 16; offset++) {
//                for (int bitcount = 0; bitcount < 16 - offset; bitcount++) {

            boolean[] insert = striped(Long.SIZE); //new boolean[Long.SIZE];
            BitVector insertvec = BitVector.of(copy(insert));

            BitVector original = new BitVector(Long.SIZE * 8);

            original.set(index, insertvec);

            boolean[] referencearray = new boolean[Long.SIZE * 8];
            System.arraycopy(striped(Long.SIZE), 0, referencearray, index, insert.length);

            assertArrayEquals(referencearray, original.toBooleanArray());
//                }
//            }
        }
    }

    @Test
    public void size1() throws Exception {
        assertEquals(0, BitVector.emptyVector().size());
        assertEquals(0, new BitVector(0).size());
        assertEquals(0, BitVector.of(new byte[0]).size());
        assertEquals(0, BitVector.of(new long[0]).size());
        assertEquals(0, BitVector.of(new boolean[0]).size());

        for (int i = 0; i < 8; i++) {
            assertEquals(i, new BitVector(i).size());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i * Byte.SIZE, BitVector.of(new byte[i]).size());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i * Long.SIZE, BitVector.of(new long[i]).size());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i, BitVector.of(new boolean[i]).size());
        }
    }

    @Test
    public void isEmpty() throws Exception {
        assertTrue(BitVector.emptyVector().isEmpty());
        assertTrue(new BitVector(0).isEmpty());
        assertTrue(BitVector.of(new byte[0]).isEmpty());
        assertTrue(BitVector.of(new long[0]).isEmpty());
        assertTrue(BitVector.of(new boolean[0]).isEmpty());

        for (int i = 0; i < 8; i++) {
            assertEquals(i == 0, new BitVector(i).isEmpty());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i == 0, BitVector.of(new byte[i]).isEmpty());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i == 0, BitVector.of(new long[i]).isEmpty());
        }

        for (int i = 0; i < 8; i++) {
            assertEquals(i == 0, BitVector.of(new boolean[i]).isEmpty());
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBit(vec.size());
    }

    @Test
    public void getBit() throws Exception {
        BitVector vec = BitVector.of(new boolean[Long.SIZE * 8]);
        for (int i = 0; i < vec.size(); i++) {
            assertEquals(0, vec.getBit(i));
        }

        boolean[] trueArray = new boolean[Long.SIZE * 8];
        Arrays.fill(trueArray, true);
        BitVector vec2 = BitVector.of(trueArray);

        for (int i = 0; i < vec2.size(); i++) {
            assertEquals(1, vec2.getBit(i));
        }

        Random rand = random();
        for (int i = 0; i < 128; i++) {
            int size = rand.nextInt(1000);
            boolean[] check = new boolean[size];
            for (int j = 0; j < check.length; j++) {
                check[j] = rand.nextBoolean();
            }
            BitVector vec3 = BitVector.of(Arrays.copyOf(check, check.length));

            for (int j = 0; j < check.length; j++) {
                assertEquals(check[j] ? 1 : 0, vec3.getBit(j));
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getByteThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getByte(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getByteThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getByte(vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getByteThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getByte(vec.size() - Byte.SIZE + 1);
    }


    @Test
    public void getByte() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 8 - Byte.SIZE; i++) {
            assertEquals("Failed @" + i, byteAt(reference, i), vec.getByte(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getShortThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getShort(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getShortThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getShort(vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getShortThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getShort(vec.size() - Short.SIZE + 1);
    }


    @Test
    public void getShort() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 8 - Short.SIZE; i++) {
            assertEquals("Failed @" + i, shortAt(reference, i), vec.getShort(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getIntThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getInt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getIntThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getInt(vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getIntThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getInt(vec.size() - Byte.SIZE + 1);
    }

    @Test
    public void getInt() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 8 - Integer.SIZE; i++) {
            assertEquals("Failed @" + i, intAt(reference, i), vec.getInt(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getLongThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getLong(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getLongThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getLong(vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getLongThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getLong(vec.size() - Long.SIZE + 1);
    }

    @Test
    public void getLong() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 8 - Long.SIZE; i++) {
            assertEquals("Failed @" + i, longAt(reference, i), vec.getLong(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(vec.size(), 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(vec.size(), 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows3() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows4() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(-1, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsThrows5() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(0, vec.size() + 1);
    }

    @Test
    public void getBits() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 4 - 16; i++) {
            for (int j = 0; j < 16; j++) {
                assertEquals("Failed @" + i, bitsAt(reference, i, j), vec.getBits(i, j));
            }
        }
    }


    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBits(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows1() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBitsLong(vec.size(), 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows2() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBitsLong(vec.size(), 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows3() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBitsLong(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows4() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBitsLong(-1, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBitsLThrows5() {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBitsLong(0, vec.size() + 1);
    }

    @Test
    public void getBitsLong() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int i = 0; i < Long.SIZE * 4 - 16; i++) {
            for (int j = 0; j < 16; j++) {
                assertEquals("Failed @" + i, bitsAtLong(reference, i, j), vec.getBits(i, j));
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBytesThrows() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBytes(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBytesThrows1() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBytes(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getBytesThrows2() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.getBytes(0, vec.size());
    }

    @Test
    public void getBytesTest() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        BitVector vec = BitVector.of(copy(reference));
        for (int index = 0; index < Long.SIZE - 16; index++) {
            for (int bytes = 0; bytes < 8; bytes++) {
                assertArrayEquals(getBytes(reference, index, bytes), vec.getBytes(index, bytes));
            }
        }
    }
//TODO

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVector1Throws() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.subVector(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVector1Throws1() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.subVector(vec.size(), 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVector1Throws2() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.subVector(0, vec.size() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void subVector1Throws3() throws Exception {
        BitVector vec = new BitVector(Long.SIZE * 8);
        vec.subVector(0, -1);
    }

    @Test
    public void subVector1() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        for (int size = 1; size < Long.SIZE * 2; size++) {
            for (int i = 1; i < reference.length - Long.SIZE * 2; i++) {
                assertEquals(BitVector.of(Arrays.copyOfRange(reference, i, i + size)), BitVector.of(reference).subVector(i, size));
            }
        }
    }

    @Test
    public void subVector2() throws Exception {
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        for (int i = 1; i < reference.length - Long.SIZE * 2; i++) {
            assertEquals(BitVector.of(Arrays.copyOfRange(reference, i, reference.length)), BitVector.of(reference).subVector(i));
        }
    }

    @Test
    public void matches() throws Exception {
        assertTrue(BitVector.emptyVector().matches(0, BitVector.emptyVector()));
        assertTrue(BitVector.emptyVector().matches(0, new BitVector(0)));
        assertFalse(BitVector.emptyVector().matches(0, BitVector.of(true)));
        assertFalse(BitVector.emptyVector().matches(0, new BitVector(1)));
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));
        boolean[] cpyref = Arrays.copyOf(reference, reference.length - 1);

        for (int i = 1; i < reference.length - Long.SIZE * 2; i++) {
            BitVector needle = BitVector.of(Arrays.copyOfRange(reference, i, reference.length));

            assertTrue(BitVector.of(reference).matches(i, needle));
            assertTrue(needle.matches(0, needle));
            assertTrue(BitVector.of(reference).matches(0, BitVector.of(reference)));
            assertFalse(needle.matches(1, needle));
            assertFalse(BitVector.of(reference).matches(1, BitVector.of(reference)));
            assertFalse(BitVector.of(reference).matches(i + 1, needle));
        }
    }

    @Test
    public void matches1() throws Exception {
        assertTrue(BitVector.emptyVector().matches(BitVector.emptyVector()));
        assertTrue(BitVector.emptyVector().matches(new BitVector(0)));
        assertFalse(BitVector.emptyVector().matches(BitVector.of(true)));
        assertFalse(BitVector.emptyVector().matches(new BitVector(1)));
        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));
        assertTrue(BitVector.of(reference).matches(0, BitVector.of(reference)));
        boolean[] cpy = Arrays.copyOf(reference, reference.length);
        cpy[5] = !cpy[5];
        assertFalse(BitVector.of(reference).matches(BitVector.of(cpy)));
    }

    @Test
    public void indexOf() throws Exception {
        assertEquals(0, BitVector.of(true).indexOf(BitVector.emptyVector()));
        assertEquals(0, BitVector.of(true).indexOf(BitVector.of(true)));
        assertEquals(0, BitVector.of(false).indexOf(BitVector.of(false)));

        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        for (int i = 0; i < Long.SIZE * 4; i++) {
            boolean[] truevalues = trueArray(Long.SIZE * 4);
            boolean[] falsevalues = falseArray(Long.SIZE * 4);

            truevalues[i] = !truevalues[i];
            falsevalues[i] = !falsevalues[i];

            assertEquals(i, BitVector.of(truevalues).indexOf(BitVector.of(false)));
            assertEquals(i, BitVector.of(falsevalues).indexOf(BitVector.of(true)));
        }

        for (int length = 1; length < Long.SIZE; length++) {
            for (int offset = 0; offset < Long.SIZE - length; offset++) {
                BitVector refvec = BitVector.of(reference);
                boolean[] subvecref = Arrays.copyOfRange(reference, offset, offset + length);
                BitVector subvec = BitVector.of(subvecref);

                for (int i = 0; i < refvec.size() - subvec.size(); i++) {
                    if (refvec.matches(i, subvec)) {
                        assertEquals("\n" + refvec + "\n" + subvec, i, refvec.indexOf(subvec));
                        break;
                    }
                }
            }
        }
    }

    @Test
    public void indexOf1() throws Exception {
        assertEquals(0, BitVector.of(true).indexOf(BitVector.emptyVector()));
        assertEquals(0, BitVector.of(true).indexOf(BitVector.of(true)));
        assertEquals(0, BitVector.of(false).indexOf(BitVector.of(false)));

        assertEquals(-1, BitVector.of(true).indexOf(BitVector.of(false)));
        assertEquals(-1, BitVector.of(false).indexOf(BitVector.of(true)));

        boolean[] reference = concat(concat(striped(Long.SIZE * 4), trueArray(Long.SIZE * 2)), falseArray(Long.SIZE * 2));

        for (int length = 1; length < Byte.SIZE; length++) {
            for (int offset = Long.SIZE - 8; offset < Long.SIZE + 8 - length; offset++) {
                BitVector refvec = BitVector.of(reference);
                boolean[] subvecref = Arrays.copyOfRange(reference, offset, offset + length);
                BitVector subvec = BitVector.of(subvecref);

                for (int i = 0; i < Math.min(refvec.size() - subvec.size(), 16); i++) {
                    if (refvec.matches(i, subvec)) {
                        assertEquals("\n" + refvec + "\n" + subvec, i, refvec.indexOf(i, subvec));
                    }
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeBitsThrows() {
        BitVector.emptyVector().writeBits(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeBitsThrows1() {
        BitVector.emptyVector().writeBits(0, 33);
    }

    @Test
    public void writeBits() {
        assertEquals(BitVector.emptyVector(), new BitVector().writeBits(0, 0));
        for (int i = 0; i < 256; i++) {
            int highest = highestBit(i);

            if (highest == -1) {
                assertEquals(BitVector.emptyVector(), new BitVector().writeBits(i, 0));
            } else {
                boolean[] boolArray = Arrays.copyOf(intAsBooleanArray(i), highest);
                assertEquals(BitVector.of(boolArray), new BitVector().writeBits(i, highest));
            }
        }


        boolean[] trueValue = trueArray(63);
        boolean[] intValue = intAsBooleanArray(0xABCDDEF1);
        assertEquals(BitVector.of(concat(trueValue, intValue)), BitVector.of(trueValue).writeBits(0xABCDDEF1, Integer.SIZE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeBitsLThrows() {
        BitVector.emptyVector().writeBits(0L, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeBitsLThrows1() {
        BitVector.emptyVector().writeBits(0L, 65);
    }

    @Test
    public void writeBitsL() {
        assertEquals(BitVector.emptyVector(), new BitVector().writeBits(0L, 0));
        for (long i = 0; i < 256; i++) {
            int highest = highestBit(i);

            if (highest == -1) {
                assertEquals(BitVector.emptyVector(), new BitVector().writeBits(i, 0));
            } else {
                boolean[] boolArray = Arrays.copyOf(longAsBooleanArray(i), highest);
                assertEquals(BitVector.of(boolArray), new BitVector().writeBits(i, highest));
            }
        }


        boolean[] trueValue = trueArray(63);
        boolean[] longValue = longAsBooleanArray(0xABCDDEF123456789L);
        assertEquals(BitVector.of(concat(trueValue, longValue)), BitVector.of(trueValue).writeBits(0xABCDDEF123456789L, Long.SIZE));
    }

    @Test
    public void toByteArrayTest() {
        assertArrayEquals(new byte[0], BitVector.emptyVector().toByteArray());

        assertArrayEquals(new byte[]{1}, BitVector.of(true).toByteArray());
        boolean[] longValue = longAsBooleanArray(0xABCDEF0123456789L);
        BitVector vec = BitVector.of(longValue).writeBits(5, 3);
        assertEquals(3 + Long.SIZE, vec.size());
        assertArrayEquals(byteArray(new int[]{0x89, 0x67, 0x45, 0x23, 0x01, 0xEF, 0xCD, 0xAB, 5}), vec.toByteArray());

        BitVector vec2 = BitVector.of(longValue).writeBits(1, 1);
        assertEquals(1 + Long.SIZE, vec2.size());
        assertArrayEquals(byteArray(new int[]{0x89, 0x67, 0x45, 0x23, 0x01, 0xEF, 0xCD, 0xAB, 1}), vec2.toByteArray());
    }

    @Test
    public void replaceFirst() {
        //replaceFirst(int index, AbstractReadableBitVector find, AbstractReadableBitVector replace)
        assertEquals(BitVector.of(new boolean[]{true, false, true}), BitVector.of(new boolean[]{true, false, true}).replaceFirst(BitVector.emptyVector(), BitVector.emptyVector()));
        assertEquals(BitVector.emptyVector(), BitVector.emptyVector().replaceFirst(BitVector.emptyVector(), BitVector.of(new boolean[]{true})));
        assertEquals(BitVector.emptyVector(), BitVector.emptyVector().replaceFirst(BitVector.emptyVector(), BitVector.of(new boolean[]{false})));


        assertEquals(
                BitVector.of(new boolean[]{true}),
                BitVector.of(new boolean[]{false}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        assertEquals(
                BitVector.of(new boolean[]{false}),
                BitVector.of(new boolean[]{true}).replaceFirst(BitVector.of(true), BitVector.of(false))
        );


        assertEquals(
                BitVector.of(new boolean[]{false, true}),
                BitVector.of(new boolean[]{true, true}).replaceFirst(BitVector.of(true), BitVector.of(false))
        );

        assertEquals(
                BitVector.of(new boolean[]{true, true, true}),
                BitVector.of(new boolean[]{false, true, true}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        assertEquals(
                BitVector.of(new boolean[]{false, true, false, true, false}),
                BitVector.of(new boolean[]{true, true, false, true, false}).replaceFirst(BitVector.of(true), BitVector.of(false))
        );

        assertEquals(
                BitVector.of(new boolean[]{false, false, false, true, false}),
                BitVector.of(new boolean[]{false, true, false, true, false}).replaceFirst(BitVector.of(true), BitVector.of(false))
        );

        assertEquals(
                BitVector.of(new boolean[]{true, true, true, true, true}),
                BitVector.of(new boolean[]{true, true, false, true, true}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        assertEquals(
                BitVector.of(new boolean[]{true, false, true}),
                BitVector.of(new boolean[]{false, false, true}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        assertEquals(
                BitVector.of(new boolean[]{true, true, true}),
                BitVector.of(new boolean[]{true, false, true}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );


        assertEquals(
                BitVector.of(new boolean[]{true, true, true}),
                BitVector.of(new boolean[]{true, false, true}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        assertEquals(
                BitVector.of(new boolean[]{true, true, false}),
                BitVector.of(new boolean[]{false, true, false}).replaceFirst(BitVector.of(false), BitVector.of(true))
        );

        Random rand = random();
        int sizebound = 5;

        for (int i = 0; i < 1024; i++) {
            boolean[] base = randomArray(rand, 2 + random().nextInt(sizebound));
            boolean[] replacement = randomArray(rand, 2 + random().nextInt(sizebound));
            boolean[] needle = subArray(base, rand);

            String basestr = binaryString(base);
            String replacementstr = binaryString(replacement);
            String needlestr = binaryString(needle);

            BitVector basevec = BitVector.of(base);
            BitVector replacementvec = BitVector.of(replacement);
            BitVector needlevec = BitVector.of(needle);

            assertEquals(basevec, BitVector.ofBinaryString(basestr));
            assertEquals(replacementvec, BitVector.ofBinaryString(replacementstr));
            assertEquals(needlevec, BitVector.ofBinaryString(needlestr));

            String finalstr = basestr.replaceFirst(needlestr, replacementstr);
            BitVector finalvec = basevec.replaceFirst(needlevec, replacementvec);

            assertEquals(
                    "Failed:\n\"" + basestr + "\"\n\"" + needlestr + "\"\n\"" + replacementstr + "\"\n\"" + finalstr + "\"",
                    BitVector.ofBinaryString(finalstr), finalvec
            );
        }
    }

    private boolean[] subArray(boolean[] value, Random rand) {
        int lower = rand.nextInt(value.length), upper = rand.nextInt(value.length);

        return Arrays.copyOfRange(value, Math.min(lower, upper), Math.max(lower, upper));
    }

    private static String binaryString(boolean[] value) {
        StringBuilder builder = new StringBuilder(value.length);

        for (int i = 0; i < value.length; i++) {
            builder.append(value[i] ? "1" : "0");
        }

        return builder.toString();
    }

    private static boolean[] randomArray(Random rand, int size) {
        boolean[] output = new boolean[size];

        for (int i = 0; i < size; i++) {
            output[i] = rand.nextBoolean();
        }

        return output;
    }

    private static String binaryString(Random rand, int size) {
        String out = "";

        for (int i = 0; i < size; i++) {
            out += rand.nextBoolean() ? "1" : "0";
        }

        return out;
    }
}