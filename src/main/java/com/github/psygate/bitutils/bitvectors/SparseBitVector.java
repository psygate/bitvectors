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

import java.util.TreeMap;

/**
 * Created by psygate on 19.09.2017.
 */
public class SparseBitVector extends AbstractWriteableBitVector<SparseBitVector> {
    private TreeMap<Integer, BitVector> vectors = new TreeMap<>();

    @Override
    public SparseBitVector writeBit(boolean bit) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SparseBitVector setBitUnchecked(int index, boolean bit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SparseBitVector replaceFirst(int index, AbstractReadableBitVector find, AbstractReadableBitVector replace) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SparseBitVector uncheckedOr(AbstractReadableBitVector other) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SparseBitVector uncheckedAnd(AbstractReadableBitVector other) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SparseBitVector uncheckedXor(AbstractReadableBitVector other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SparseBitVector not() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean getUnchecked(int position) {
        return false;
    }

    @Override
    protected SparseBitVector subVectorUnchecked(int position, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SparseBitVector empty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SparseBitVector copy() {
        throw new UnsupportedOperationException();
    }
}
