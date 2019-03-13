package com.example.anthony.timelapscontroller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.app4.project.timelapse.api.client.TimelapseBasicClient;

import java.util.concurrent.Executor;

public abstract class TimelapseActivity extends AppCompatActivity {

    private TimelapseBasicClient client;
    private Executor executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = ClientSingleton.getClient();
        executor = initializeExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //TODO authenticate
            }
        });
    }


    public Executor getExecutor() {
        return executor;
    }

    public TimelapseBasicClient getClient() {
        return client;
    }


    Executor initializeExecutor() {
        return ClientSingleton.getExecutor();
    }
}
