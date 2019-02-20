package com.example.anthony.timelapscontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.ErrorResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {

    private TimelapseClient client;
    private ScheduledFuture scheduledFuture;
    private ScheduledExecutorService executor;
    private final Runnable getCameraState = new Runnable() {
        @Override
        public void run() {
            client.getCameraState(new Callback<CameraState>() {
                @Override
                public void onSuccess(int i, final CameraState cameraState) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCameraView(cameraState);
                        }
                    });
                }

                @Override
                public void onError(int i, final ErrorResponse errorResponse) {
                    /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(viewpager, "Une erreur est survenue:" + errorResponse.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                    */
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        client = ClientSingleton.getClient();
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        long period = 4;
        scheduledFuture = executor.scheduleAtFixedRate(getCameraState, period, period, TimeUnit.SECONDS);
    }

    @Override
    protected void onStop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.onStop();
    }

    private void updateCameraView(CameraState cameraState) {
        //TODO mettre a jour la valeur des Textview qui affiche les données dans Camerastate
    }

    public void onWakeUpClick(View v) {
        onCommandClick(Command.WAKE_UP);
    }

    public void onSleepClick(View v) {
        onCommandClick(Command.SLEEP);
    }

    public void onTurnOffClick(View v) {
        onCommandClick(Command.TURN_OFF);
    }

    private void onCommandClick(Command command) {
        client.postCommand(command, new Callback<Command>() {
            @Override
            public void onSuccess(int i, Command command) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraActivity.this, "La commande a bien été envoyée", Toast.LENGTH_SHORT).show();
                        getCameraState.run();
                    }
                });
            }

            @Override
            public void onError(int i, ErrorResponse errorResponse) {
                 /*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(viewpager, "Une erreur est survenue:" + errorResponse.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });
                    */
            }
        });
    }
}
