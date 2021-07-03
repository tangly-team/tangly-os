/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.utilities;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.tangly.commons.utilities.BitUtilities.BYTE;
import static net.tangly.commons.utilities.BitUtilities.DWORD;
import static net.tangly.commons.utilities.BitUtilities.appendBits;
import static net.tangly.commons.utilities.BitUtilities.extractBitsToByte;
import static net.tangly.commons.utilities.BitUtilities.extractBitsToInt;
import static net.tangly.commons.utilities.BitUtilities.extractBitsToShort;
import static net.tangly.commons.utilities.BitUtilities.toHex;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the bit utilities static class.
 */
class BitUtilitiesTest {

    /**
     * Number of bytes in the stream of bytes used for testing.
     */
    private static final int SIZE = DWORD / BYTE;


    /**
     * Tests appending bits stored in a byte to a byte stream.
     */
    @DisplayName("Test adding bits to a byte (8 bits)")
    @Test
    void testAppendBitsWithByte() {
        {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++) {
                assertThat(appendBits(stream, (byte) 0x01, i, 1)).isEqualTo(i + 1);
            }
            assertThat(expected).isEqualTo(stream);
        }
        {
            byte[] expected = {(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++) {
                appendBits(stream, (byte) 0x01, i, 1);
            }
            assertThat(expected).isEqualTo(stream);
        }
        {
            byte[] expected = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++, i++) {
                appendBits(stream, (byte) 0x01, i, 1);
            }
            assertThat(expected).isEqualTo(stream);
        }
    }

    /**
     * Tests extracting bits stored in a byte stream to a byte.
     */
    @DisplayName("Test extracting bits from a byte (8 bits)")
    @Test
    void testExtractBitsToByte() {
        {
            byte[] stream = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            for (int i = 0; i < DWORD; i++) {
                assertThat(extractBitsToByte(stream, i, 1)).isEqualTo((byte) 0x01);
            }
        }
        {
            byte[] stream = {(byte) 0x49, (byte) 0x92, (byte) 0x70, (byte) 0xF6};
            assertThat(extractBitsToByte(stream, 0, 8)).isEqualTo((byte) 0x49);
            assertThat(extractBitsToByte(stream, 0, 4)).isEqualTo((byte) 0x09);
            assertThat(extractBitsToByte(stream, 4, 4)).isEqualTo((byte) 0x04);
            assertThat(extractBitsToByte(stream, 4, 8)).isEqualTo((byte) 0x24);
            assertThat(extractBitsToByte(stream, 8, 8)).isEqualTo((byte) 0x092);
            assertThat(extractBitsToByte(stream, 16, 8)).isEqualTo((byte) 0x70);
            assertThat(extractBitsToByte(stream, 24, 8)).isEqualTo((byte) 0xF6);
        }
    }

    /**
     * Tests appending bits stored in a short to a byte stream.
     */
    @DisplayName("Test adding bits to a word (16 bits)")
    @Test
    void testAppendBitsWithShort() {
        {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++) {
                appendBits(stream, (short) 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++) {
                appendBits(stream, (short) 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++, i++) {
                appendBits(stream, (short) 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            byte[] stream = new byte[SIZE];
            int pos = 0;
            pos = appendBits(stream, (short) 0x9249, pos, 8);
            pos = appendBits(stream, (short) (0x9249 >>> 8), pos, 7);
            pos = appendBits(stream, (short) (0x9249 >>> 15), pos, 1);
            pos = appendBits(stream, (short) 0x4924, pos, 16);
            assertThat(pos).isEqualTo(DWORD);
            assertThat(stream).isEqualTo(expected);
        }
    }

    /**
     * Tests extracting bits stored in a byte stream to a short.
     */
    @DisplayName("Test extracting bits from a word (16 bits)")
    @Test
    void testExtractBitsToShort() {
        byte[] stream = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        for (int i = 0; i < DWORD; i++) {
            assertThat(extractBitsToShort(stream, i, 1)).isEqualTo((short) 0x01);
        }
        for (int i = 0; i < 20; i++) {
            assertThat(extractBitsToShort(stream, i, 12)).isEqualTo((short) 0xFFF);
        }
        assertThat(extractBitsToShort(stream, 8, 16)).isEqualTo((short) 0xFFFF);
        assertThat(extractBitsToShort(stream, 0, 16)).isEqualTo((short) 0xFFFF);
    }

    /**
     * Tests appending bits stored in an integer to a byte stream.
     */
    @DisplayName("Test adding bits to a double word (32 bits)")
    @Test
    void testAppendBitsWithInt() {
        {
            byte[] expected = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++) {
                appendBits(stream, 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++) {
                appendBits(stream, 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            byte[] stream = new byte[SIZE];
            for (int i = 0; i < DWORD; i++, i++, i++) {
                appendBits(stream, 0x01, i, 1);
            }
            assertThat(stream).isEqualTo(expected);
        }
        {
            byte[] expected = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            byte[] stream = new byte[SIZE];
            int pos = 0;
            pos = appendBits(stream, 0x49249249, pos, 9);
            pos = appendBits(stream, (0x49249249 >>> 9), pos, 17);
            pos = appendBits(stream, (0x49249249 >>> 26), pos, 4);
            pos = appendBits(stream, (0x49249249 >>> 30), pos, 2);
            assertThat(pos).isEqualTo(DWORD);
            assertThat(stream).isEqualTo(expected);
        }
    }

    /**
     * Tests extracting bits stored in a byte stream to an integer.
     */
    @DisplayName("Test extracting bits from a double word (32 bits)")
    @Test
    void testExtractBitsToInt() {
        byte[] stream = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        for (int i = 0; i < DWORD; i++) {
            assertThat(extractBitsToInt(stream, i, 1)).isEqualTo(0x01);
        }
        for (int i = 0; i < 20; i++) {
            assertThat(extractBitsToShort(stream, i, 12)).isEqualTo((short) 0xFFF);
        }
        assertThat(extractBitsToInt(stream, 0, 24)).isEqualTo(0xFFFFFF);
    }

    /**
     * Tests the hexadecimal representation of a byte stream.
     */
    @Test
    void testToHex() {
        {
            byte[] stream = {(byte) 0x49, (byte) 0x92, (byte) 0x24, (byte) 0x49};
            assertThat(toHex(stream)).isEqualTo("0x49, 0x92, 0x24, 0x49");
        }
        byte[] stream = {};
        assertThat(toHex(stream)).isEmpty();
    }
}
