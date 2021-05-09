package com.example.opustest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
                audioPlayer.play(audioRecorder.consumeBytes());
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
                audioRecorder.record();
            }
        });
    }


}