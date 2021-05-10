package com.example.opustest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.opustest.utils.OpusEncoder;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


public class AudioRecorder {
    private static final String TAG = "AudioRecorder";

    public static final int SAMPLE_RATE = 48000; // 8000;
    public final int pTime = 40; // packetization times in ms

    // frames per second, depends on packetization time
    public final int FRAME_RATE = 1000 / pTime;

    public final int SAMPLES_PER_SECOND = SAMPLE_RATE / FRAME_RATE;

    // frame size depends on sample rate based on packetization time for next processing
    //x2 because PCM_16 bit = 2 bytes per sample  //320; //opus_encode() - pcm param-> frame_size*sizeof(opus_int16)
    public final int FRAME_SIZE = SAMPLES_PER_SECOND * 2;
    public final int BUF_SIZE = FRAME_SIZE / 2;

    private AudioRecord recorder;
    private OpusEncoder opusEncoder = new OpusEncoder();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    Queue<byte[]> mAudioQueue = new LinkedList<byte[]>();

    AudioPlayer audioPlayer;

    private boolean capturingAudio = false;

    public AudioRecorder(AudioPlayer audioPlayer) {
        Log.i(TAG, "Constructor called");
        Log.e(TAG, "FrameSize: " + FRAME_SIZE);
        this.audioPlayer = audioPlayer;
    }

    // record and encode via opus encoder
    public void recordOpus(){
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

                // Init OpusEncoder
                opusEncoder.init(SAMPLE_RATE, 1, FRAME_SIZE/2);
                Log.e(TAG, "AudioRecord + OpusEncoder initialized | MinBufferSize: " + minBufSize + " | FrameSize: " + (FRAME_SIZE/2));

                short[] recordedData = new short[BUF_SIZE];
                byte[] encodedData = new byte[1024];

                while (capturingAudio) {
                    recorder.read(recordedData, 0, recordedData.length);
                    Log.i(TAG, "RecordedData [" + recordedData.length + "]: " + Arrays.toString(recordedData));
                    int encoded = opusEncoder.encode(recordedData, encodedData);
                    Log.i(TAG, "Encoded: " + encoded);
                    Log.i(TAG, "Encoded Data ["+ encodedData.length + "]: " + Arrays.toString(encodedData));
                    if (encoded > 0) {
                        mAudioQueue.add(Arrays.copyOfRange(encodedData, 0, encoded));
                    }
                }

                recorder.stop();
                recorder.release();
            }
        });
        audioCaptureThread.start();
    }

    public void record() {
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

                Log.e(TAG, "AudioRecord initialized | MinBufferSize: " + minBufSize);

                byte[] recordedData = new byte[BUF_SIZE];

                while (capturingAudio) {
                    // read to buffer
                    // compress with codec
                    recorder.read(recordedData, 0, recordedData.length);
                    try {
                        byteArrayOutputStream.write(recordedData, 0, recordedData.length);
                        Log.i(TAG, "ByteArrayOutputStream: " + byteArrayOutputStream.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                recorder.stop();
                recorder.release();
            }
        });
        audioCaptureThread.start();
    }

    public void stop() {
        capturingAudio = false;
    }

    public byte[] consumeBytes() {
        return mAudioQueue.poll();
    }
}