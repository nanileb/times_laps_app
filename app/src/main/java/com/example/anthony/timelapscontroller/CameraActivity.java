package com.example.anthony.timelapscontroller;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.CameraState;
import com.app4.project.timelapse.model.Command;
import com.app4.project.timelapse.model.ErrorResponse;

import java.text.SimpleDateFormat;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends TimelapseActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
    private ScheduledFuture scheduledFuture;
    private CameraState cameraState;
    private TextView allume;
    private TextView veille;
    private TextView Signe_vie;
    private Button veilleButton;
    private final Runnable getCameraState = new Runnable() {
        @Override
        public void run() {
            final TimelapseResponse<CameraState> response = getClient().getCameraState();
            if (response.isSuccessful()) {
                final CameraState state = response.getData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CameraActivity.this.cameraState = state;
                        updateCameraView(cameraState);
                        veilleButton.setText(cameraState.isSleeping() ? "REVEILLER" : "METTRE EN VEILLE");
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        allume = (TextView) findViewById(R.id.Camera_On);
        veille = (TextView) findViewById(R.id.Camera_Veille);
        Signe_vie = (TextView) findViewById(R.id.signedevie);
        veilleButton = findViewById(R.id.Mise_en_veille);
    }

    @Override
    protected void onStart() {
        super.onStart();
        long period = 4;
        scheduledFuture = ((ScheduledExecutorService)getExecutor()).scheduleAtFixedRate(getCameraState, period, period, TimeUnit.SECONDS);
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
        if (cameraState == null) {
            Toast.makeText(this, "Attendez la fin du chargement", Toast.LENGTH_SHORT).show();
            return;
        }
        onCommandClick(cameraState.isSleeping() ? Command.WAKE_UP : Command.SLEEP);
    }

    public void onTurnOffClick(View v) {
        onCommandClick(Command.TURN_OFF);
    }

    private void onCommandClick(final Command command) {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                TimelapseResponse response = getClient().postCommand(command);
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(veilleButton, "La commande a bien été envoyée. Attendez 10s pour voir le changement", Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(CameraActivity.this, "Erreur: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    Executor initializeExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
