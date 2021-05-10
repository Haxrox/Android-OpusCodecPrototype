package com.example.opustest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.example.opustest.utils.OpusDecoder;

import java.util.Arrays;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    public static final int SAMPLE_RATE = 48000; // 8000;
    public final int pTime = 40;//packetization times in ms

    // frames per second, depends on packetization time
    public final int FRAME_RATE = 1000 / pTime;

    public final int SAMPLES_PER_SECOND = SAMPLE_RATE / FRAME_RATE;

    // frame size depends on sample rate based on packetization time for next processing
    //x2 because PCM_16 bit = 2 bytes per sample  //320; //opus_encode() - pcm param-> frame_size*sizeof(opus_int16)
    public final int FRAME_SIZE = SAMPLES_PER_SECOND * 2;
    public final int BUF_SIZE = FRAME_SIZE / 2;

    private AudioTrack track;
    private OpusDecoder opusDecoder;

    public AudioPlayer() {
        // min buffer size
        int minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // init audio track
        track = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize,
                AudioTrack.MODE_STREAM);
        track.play();

        // init opus decoder
        opusDecoder = new OpusDecoder();
        opusDecoder.init(SAMPLE_RATE, 1, FRAME_SIZE / 2);

        Log.e(TAG, "AudioPlayer + OpusDecoder initialized | MinBufferSize: " + minBufSize);
    }

    // playOpus
    public void playOpus(byte[] data) {
        Log.e(TAG, "Play Opus audio data");
        Thread audioPlaybackThread = new Thread(new Runnable() {
            public void run() {
                short[] decodedData = new short[1024];
                int decoded = opusDecoder.decode(data, decodedData);
                Log.i(TAG, "Data [" + data.length + "]: " + Arrays.toString(data));
                Log.i(TAG, "Decoded: " + decoded);
                Log.i(TAG, "Decoded Data ["+ decodedData.length + "]: " + Arrays.toString(decodedData));
                track.write(decodedData, 0, decoded);
            }
        });
        audioPlaybackThread.start();
    }

    // play
    public void play(byte[] data) {
        Log.e(TAG, "Play audio data: " + data.length);
        Thread audioPlaybackThread = new Thread(new Runnable() {
            public void run() {
                // Log.i(TAG, "Data [" + data.length + "]: " + Arrays.toString(data) + " | Decoded: " + decoded + " | decodedData: " + decodedData.length);
                track.write(data, 0, data.length);
            }
        });
        audioPlaybackThread.start();
    }

    // play
    public void play(short[] data) {
        Log.e(TAG, "Play audio data: " + data.length);
        Thread audioPlaybackThread = new Thread(new Runnable() {
            public void run() {
                // Log.i(TAG, "Data [" + data.length + "]: " + Arrays.toString(data) + " | Decoded: " + decoded + " | decodedData: " + decodedData.length);
                track.write(data, 0, data.length);
            }
        });
        audioPlaybackThread.start();
    }

    // Decode
    public short[] decode(byte[] data) {
        short[] decodedData = new short[1024];
        int decoded = opusDecoder.decode(data, decodedData);
        return Arrays.copyOfRange(decodedData, 0, decoded);
    }
}
