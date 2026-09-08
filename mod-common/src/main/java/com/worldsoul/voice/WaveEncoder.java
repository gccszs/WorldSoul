package com.worldsoul.voice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Encodes signed 16-bit mono PCM as an in-memory WAV file. */
public final class WaveEncoder {
    public static final int SAMPLE_RATE = 16_000;
    public static final int CHANNELS = 1;
    public static final int BITS_PER_SAMPLE = 16;
    public static final int MIN_CAPTURE_BYTES = SAMPLE_RATE * CHANNELS * (BITS_PER_SAMPLE / 8) / 4;

    private WaveEncoder() {}

    public static boolean isLongEnough(byte[] pcm) {
        return pcm.length >= MIN_CAPTURE_BYTES;
    }

    public static byte[] encode(byte[] pcm) {
        ByteBuffer output = ByteBuffer.allocate(44 + pcm.length).order(ByteOrder.LITTLE_ENDIAN);
        output.put(new byte[]{'R', 'I', 'F', 'F'});
        output.putInt(36 + pcm.length);
        output.put(new byte[]{'W', 'A', 'V', 'E', 'f', 'm', 't', ' '});
        output.putInt(16);
        output.putShort((short) 1);
        output.putShort((short) CHANNELS);
        output.putInt(SAMPLE_RATE);
        output.putInt(SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8);
        output.putShort((short) (CHANNELS * BITS_PER_SAMPLE / 8));
        output.putShort((short) BITS_PER_SAMPLE);
        output.put(new byte[]{'d', 'a', 't', 'a'});
        output.putInt(pcm.length);
        output.put(pcm);
        return output.array();
    }
}
