package com.example.opustest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.opustest.utils.OpusDecoder;
import com.example.opustest.utils.OpusEncoder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    AudioRecorder audioRecorder;
    AudioPlayer audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioPlayer = new AudioPlayer();
        audioRecorder = new AudioRecorder(audioPlayer);

        Button playButton = (Button) findViewById(R.id.playButton);
        Button stopButton = (Button) findViewById(R.id.stopButton);
        Button recordButton = (Button) findViewById(R.id.recordButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // audioPlayer.play(audioRecorder.consumeBytes());
                // audioPlayer.playOpus(audioRecorder.consumeBytes());
                byte[] audioData = audioRecorder.consumeBytes();
                short[] decodedData = audioPlayer.decode(audioData);
                audioData = audioRecorder.consumeBytes();
                while (audioData != null) {
                    /*
                    audioPlayer.playOpus(audioData);
                     */
                    short[] decoded = audioPlayer.decode(audioData);
                    short[] audioBuffer = new short[decodedData.length + decoded.length];
                    System.arraycopy(decodedData, 0, audioBuffer, 0, decodedData.length);
                    System.arraycopy(decoded, 0, audioBuffer, decodedData.length, decoded.length);
                    decodedData = audioBuffer;
                    audioData = audioRecorder.consumeBytes();
                }
                audioPlayer.play(decodedData);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorder.stop();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // audioRecorder.record();
                audioRecorder.recordOpus();
            }
        });
    }
}