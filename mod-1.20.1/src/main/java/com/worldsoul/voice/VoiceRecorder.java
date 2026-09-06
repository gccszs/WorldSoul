package com.worldsoul.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

/** One push-to-talk microphone capture with a fixed packet-safe duration ceiling. */
public final class VoiceRecorder {
    private static final int MAX_PCM_BYTES = WaveEncoder.SAMPLE_RATE * 2 * 15;
    private volatile boolean recording;
    private TargetDataLine line;

    public synchronized boolean start(Consumer<byte[]> completed, Consumer<Exception> failed) {
        if (recording) return false;
        AudioFormat format = new AudioFormat(WaveEncoder.SAMPLE_RATE, 16, 1, true, false);
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (Exception error) {
            line = null;
            failed.accept(error);
            return false;
        }
        recording = true;
        TargetDataLine activeLine = line;
        Thread capture = new Thread(() -> capture(activeLine, completed, failed), "worldsoul-voice-capture");
        capture.setDaemon(true);
        capture.start();
        return true;
    }

    public synchronized void stop() {
        recording = false;
        if (line != null) {
            line.stop();
            line.close();
            line = null;
        }
    }

    public boolean isRecording() { return recording; }

    private void capture(TargetDataLine activeLine, Consumer<byte[]> completed, Consumer<Exception> failed) {
        ByteArrayOutputStream pcm = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            while (recording && pcm.size() < MAX_PCM_BYTES) {
                int remaining = MAX_PCM_BYTES - pcm.size();
                int read = activeLine.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read > 0) pcm.write(buffer, 0, read);
            }
            if (WaveEncoder.isLongEnough(pcm.toByteArray())) {
                completed.accept(WaveEncoder.encode(pcm.toByteArray()));
            } else {
                failed.accept(new IllegalArgumentException("录音时间过短，请按住 V 键至少 0.25 秒。"));
            }
        } catch (RuntimeException error) {
            if (recording) failed.accept(error);
        } finally {
            synchronized (this) {
                recording = false;
                if (line == activeLine) line = null;
            }
            activeLine.stop();
            activeLine.close();
        }
    }
}
