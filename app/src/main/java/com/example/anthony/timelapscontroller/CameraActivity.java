package com.example.anthony.timelapscontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.ErrorResponse;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
    private TimelapseClient client;
    private ScheduledFuture scheduledFuture;
    private ScheduledExecutorService executor;
    private CameraState cameraState;
    private TextView allume;
    private TextView veille;
    private TextView Signe_vie;
    private final Runnable getCameraState = new Runnable() {
        @Override
        public void run() {
            client.getCameraState(new Callback<CameraState>() {
                @Override
                public void onSuccess(int i, final CameraState cameraState) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CameraActivity.this.cameraState = cameraState;
                            updateCameraView(cameraState);
                            //TODO Nelson: changer le name du bouton SLEEP/WAKE UP
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
        allume = (TextView) findViewById(R.id.Camera_On);
        veille = (TextView) findViewById(R.id.Camera_Veille);
        Signe_vie = (TextView) findViewById(R.id.signedevie);
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
        //TODO mettre a jour la valeur des Textview qui affiche les données dans Camerastate1

        if(cameraState.isTurnedOn()) {
            allume.setText("oui");

        }else{
            allume.setText("non");
        }

        if(cameraState.isSleeping()){
            veille.setText("oui");
        }else{
            veille.setText("non");
        }

        Signe_vie.setText(sdf.format(cameraState.getLastTimeAlive()));
        // utilise cameraState.isTurnedOn() pour savoir si la camera est allumée
        // utilise cameraState.isSleeping() pour savoir si la camera est en veille
        // utilise cameraState.getLastTimeAlive() pour avoir le dernier signe de vie (un long qu'il faut convertir en string avec un SimpleDateFormat, comme dans la MainActivity)

    }

    public void onSleepClick(View v) {
        if (cameraState != null && !cameraState.isSleeping()) {
            onCommandClick(Command.WAKE_UP);
        } else {
            onCommandClick(Command.SLEEP);
        }
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
