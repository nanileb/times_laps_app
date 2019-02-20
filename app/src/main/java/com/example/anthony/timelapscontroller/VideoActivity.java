package com.example.anthony.timelapscontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        int executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
    }
}
