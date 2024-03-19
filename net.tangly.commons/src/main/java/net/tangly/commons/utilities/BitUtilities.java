/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          https://apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.commons.utilities;

import java.util.Objects;

import static net.tangly.commons.lang.Preconditions.checkArgument;

/**
 * <p> The bit utility class provides utility functions to manipulate bit streams as used on older communication
 * protocols. The functions manipulate byte (8 bits), short (16 bits) and long (32 bits) bit streams and append them into the bit stream at location. Mirror
 * functions support the extraction of bits from a bit stream.</p>
 * <p>Any legacy bit oriented communication protocols can efficiently be handled with the provided functions.</p>
 */
public final class BitUtilities {

    /**
     * Size of a byte in bits.
     */
    public static final int BYTE = 8;

    /**
     * Size of a word in bits.
     */
    public static final int WORD = 2 * BYTE;

    /**
     * Size of a double word in bits.
     */
    public static final int DWORD = 4 * BYTE;

    /**
     * Mask structure to select the first n [0..8] bits of a byte. The first element is defined to have logical indexes in the code.
     */
    private static final byte[] MASKS =
        {0b0000_0000, 0b0000_0001, 0b0000_0011, 0b0000_0111, 0b0000_1111, 0b0001_1111, 0b0011_1111, 0b0111_1111, (byte) 0b1111_1111};

    /**
     * Private constructor for a utility class.
     */
    private BitUtilities() {
    }

    /**
     * Writes length number of bits from data into the byte stream at the bit position. The assumption is that bits are appended and the stream is initialized
     * to 0.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param data     byte of data to be inserted into the bit stream
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..8]
     * @return the position at the end of the written stream
     */
    public static int appendBits(byte[] stream, byte data, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= BYTE));
        int byteIndex = position / BYTE;
        int bitIndex = position % BYTE;
        int nrOfBits = 0;
        if (bitIndex != 0) {
            // the current byte has free room, and we find how many bits can be appended.
            if (bitIndex + length > BYTE) {
                nrOfBits = BYTE - bitIndex;
            } else {
                nrOfBits = length;
            }
            // copy the first bits shifted up into the free bits in the byte
            byte bits = (byte) ((data & MASKS[nrOfBits]) << bitIndex);
            stream[byteIndex] = (byte) (stream[byteIndex] | bits);
            byteIndex++;
        }
        if (nrOfBits < length) {
            // copy the remaining bits, at most 8, shifted down into the next byte
            int nrOfRemainingBits = length - nrOfBits;
            byte bits = (byte) ((data >>> nrOfBits) & MASKS[nrOfRemainingBits]);
            stream[byteIndex] = (byte) (stream[byteIndex] | bits);
        }
        return position + length;
    }

    /**
     * Extracts length number of bits from the byte stream at the bit position.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..8]
     * @return the extracted data from the byte stream
     */
    public static byte extractBitsToByte(byte[] stream, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= BYTE));
        int byteIndex = position / BYTE;
        int bitIndex = position % BYTE;
        int lowerBits;
        int higherBits;
        if (bitIndex + length > BYTE) {
            lowerBits = BYTE - bitIndex;
            higherBits = length - lowerBits;
        } else {
            lowerBits = length;
            higherBits = 0;
        }
        byte bits = (byte) ((stream[byteIndex] >>> bitIndex) & MASKS[lowerBits]);
        if (higherBits > 0) {
            byteIndex++;
            bits = (byte) (bits | ((stream[byteIndex] & MASKS[higherBits]) << lowerBits));
        }
        return bits;
    }

    /**
     * Writes length number of bits from data into the byte stream at the bit position. The assumption is that bits are appended and the stream is initialized
     * to 0.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param data     word of data to be inserted into the bit stream
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..16]
     * @return the position at the end of the written stream
     */
    public static int appendBits(byte[] stream, short data, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= WORD));
        if (length > BYTE) {
            appendBits(stream, (byte) (data & 0xFF), position, BYTE);
            appendBits(stream, (byte) ((data >>> BYTE) & 0xFF), position + BYTE, length - BYTE);
        } else {
            appendBits(stream, (byte) (data & 0xFF), position, length);
        }
        return position + length;
    }

    /**
     * Extracts length number of bits from the byte stream at the bit position.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..16]
     * @return the extracted data from the byte stream
     */
    public static short extractBitsToShort(byte[] stream, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= WORD));
        int bits = extractBitsToByte(stream, position, Math.min(length, BYTE)) & 0xFF;
        if (length > BYTE) {
            bits = bits | (extractBitsToByte(stream, position + BYTE, length - BYTE) << BYTE);
        }
        return (short) (bits & 0xFFFF);
    }

    /**
     * Writes length number of bits from data into the byte stream at the bit position. The assumption is that bits are appended and the stream is initialized
     * to 0.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param data     long word of data to be inserted into the bit stream
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..32]
     * @return the position at the end of the written stream
     */
    public static int appendBits(byte[] stream, int data, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= DWORD));
        if (length > WORD) {
            appendBits(stream, (short) (data & 0xFFFF), position, WORD);
            appendBits(stream, (short) ((data >>> WORD) & 0xFFFF), position + WORD, length - WORD);
        } else {
            appendBits(stream, (short) (data & 0xFFFF), position, length);
        }
        return position + length;
    }

    /**
     * Extracts length number of bits from the byte stream starting at the bit position.
     *
     * @param stream   stream of bits encoded as an array of bytes. The stream cannot be null
     * @param position start index in bits in the stream, starting at 0
     * @param length   number of bits to insert into the stream. The format is constrained in [1..32]
     * @return the extracted data from the byte stream
     */
    public static int extractBitsToInt(byte[] stream, int position, int length) {
        Objects.requireNonNull(stream);
        Objects.checkIndex(position, stream.length * BYTE - length + 1);
        checkArgument((length >= 1) && (length <= DWORD));
        int bits = extractBitsToShort(stream, position, Math.min(length, WORD)) & 0xFFFF;
        if (length > WORD) {
            bits = bits | (extractBitsToShort(stream, position + WORD, length - WORD) << WORD);
        }
        return bits;
    }

    /**
     * Transforms the stream of byte into a human-readable sequence of hexadecimal values.
     *
     * @param stream the byte stream to transform. The stream must not be null
     * @return the hexadecimal representation of the byte stream
     */
    public static String toHex(byte[] stream) {
        Objects.requireNonNull(stream);
        StringBuilder buffer = new StringBuilder();
        if (stream.length > 0) {
            buffer.append(String.format("0x%02X", stream[0]));
            for (int i = 1; i < stream.length; i++) {
                buffer.append(", ").append(String.format("0x%02X", stream[i]));
            }
        }
        return buffer.toString();
    }
}
