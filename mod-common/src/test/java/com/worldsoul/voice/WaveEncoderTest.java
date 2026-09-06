package com.worldsoul.voice;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WaveEncoderTest {
    @Test
    public void writesCanonicalPcmWaveHeaderAndPayload() {
        byte[] wave = WaveEncoder.encode(new byte[]{1, 2, 3, 4});
        assertEquals(48, wave.length);
        assertArrayEquals(new byte[]{'R', 'I', 'F', 'F'}, java.util.Arrays.copyOfRange(wave, 0, 4));
        assertEquals(16000, ByteBuffer.wrap(wave, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        assertEquals(4, ByteBuffer.wrap(wave, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, java.util.Arrays.copyOfRange(wave, 44, 48));
    }

    @Test
    public void rejectsCaptureShorterThanQuarterSecond() {
        assertFalse(WaveEncoder.isLongEnough(new byte[WaveEncoder.MIN_CAPTURE_BYTES - 1]));
        assertTrue(WaveEncoder.isLongEnough(new byte[WaveEncoder.MIN_CAPTURE_BYTES]));
    }
}
