package com.example.opustest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.opustest.utils.OpusEncoder;

import java.io.ByteArrayOutputStream;


public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    public static final int SAMPLE_RATE = 8000;
    public final int pTime = 20;//packetization times in ms

    // frames per second, depends on packetization time
    public final int FRAME_RATE = 1000 / pTime;

    public final int SAMPLES_PER_SECOND = SAMPLE_RATE / FRAME_RATE;

    // frame size depends on sample rate based on packetization time for next processing
    //x2 because PCM_16 bit = 2 bytes per sample  //320; //opus_encode() - pcm param-> frame_size*sizeof(opus_int16)
    public final int FRAME_SIZE = SAMPLES_PER_SECOND * 2;
    public final int BUF_SIZE = FRAME_SIZE / 2;

    private AudioRecord recorder;
    private OpusEncoder opusEncoder;

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    AudioPlayer audioPlayer;

    private boolean capturingAudio = false;

    public AudioRecorder(AudioPlayer audioPlayer) {
        Log.i(TAG, "Constructor called");
        this.audioPlayer = audioPlayer;
    }

    // record and encode via opus encoder
    public void record(){
        Log.e(TAG, "Recording audio");
        Thread audioCaptureThread = new Thread(new Runnable() {
            public void run() {
                capturingAudio = true;
                // initialize audio recorder
                int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        minBufSize);
                recorder.startRecording();

                // init opus encoder
                opusEncoder = new OpusEncoder();
                opusEncoder.init(SAMPLE_RATE, 1, FRAME_SIZE / 2);

                short[] recordedData = new short[BUF_SIZE];
                byte[] encodedData = new byte[1024];

                while (capturingAudio) {
                    // read to buffer
                    // compress with codec
                    recorder.read(recordedData, 0, recordedData.length);
                    int encoded = opusEncoder.encode(recordedData, encodedData);
                    Log.i(TAG, "RecordedData: " + recordedData.length + " | Encoded: " + encoded + " | EncodedData: " + encodedData.length);
                    try {
                        Thread.sleep(1000);
                        byte[] audioData = new byte[encoded];
                        System.arraycopy(encodedData, 0, audioData, 0, encoded);
                        audioPlayer.play(audioData);
                        // byteArrayOutputStream.write(encodedData, 0, encoded);
                        // Log.i(TAG, "ByteArrayOutputStream: " + byteArrayOutputStream.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                recorder.stop();
                recorder.release();
                opusEncoder.close();
            }
        });
        audioCaptureThread.start();
    }

    public void stop() {
        capturingAudio = false;
    }

    public byte[] consumeBytes() {
        byte[] data = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        Log.e(TAG, "Consume bytes: " + data.length + " | ByteArrayOutputStream: " + byteArrayOutputStream.size());
        return data;
    }
}