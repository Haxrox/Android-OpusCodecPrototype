package com.example.opustest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.example.opustest.utils.OpusDecoder;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    static Object audioThreadPlaybackLock = new Object();

    public static final int SAMPLE_RATE = 8000;
    public final int pTime = 20;//packetization times in ms

    // frames per second, depends on packetization time
    public final int FRAME_RATE = 1000 / pTime;

    public final int SAMPLES_PER_SECOND = SAMPLE_RATE / FRAME_RATE;

    // frame size depends on sample rate based on packetization time for next processing
    //x2 because PCM_16 bit = 2 bytes per sample  //320; //opus_encode() - pcm param-> frame_size*sizeof(opus_int16)
    public final int FRAME_SIZE = SAMPLES_PER_SECOND * 2;
    public final int BUF_SIZE = FRAME_SIZE / 2;
    // min buffer size
    int minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

    private AudioTrack track;
    private OpusDecoder opusDecoder;

    public AudioPlayer() {
        Log.e(TAG, "Constructor called");
        // init audio track
        track = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize,
                AudioTrack.MODE_STREAM);

        // init opus decoder
        opusDecoder = new OpusDecoder();
        opusDecoder.init(SAMPLE_RATE, 1, FRAME_SIZE / 2);
    }

    // play
    public void play(byte[] data) {
        Log.e(TAG, "Playing audio");
        Thread audioPlaybackThread = new Thread(new Runnable()
        {
            public void run()
            {
                synchronized(audioThreadPlaybackLock)
                {
                    short[] decodedData = new short[BUF_SIZE];
                    int decoded = opusDecoder.decode(data, decodedData);
                    Log.i(TAG, "Data: " + data.length + " | Decoded: " + decoded + " | decodedData: " + decodedData.length);
                    track.write(decodedData, 0, decoded, AudioTrack.WRITE_BLOCKING);
                }
            }});
        audioPlaybackThread.start();
    }
}
