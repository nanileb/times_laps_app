package com.example.anthony.timelapscontroller;

import android.content.Intent;
import android.provider.Contacts;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;


public class ConsultActivity extends AppCompatActivity {

    private TimelapseClient client;
    private Execution execution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult_);
        client = ClientSingleton.getClient();

        final int executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        client.getExecution(executionId, new Callback<Execution>() {
            @Override
            public void onSuccess(int i, final Execution execution) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConsultActivity.this.execution = execution;
                        //TODO REMPLIR LES CHAMPS DE L'ACTIVITE (nom de l'execution, heure de debut, etc...)
                        //...

                    }
                });
            }

            @Override
            public void onError(int i, ErrorResponse errorResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ConsultActivity.this, "Erreur lors du chargement de l'execution", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Button Photos = (Button) findViewById(R.id.PhotosVisua);

        Photos.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                //Create the intent to start another activity
                Intent intent = new Intent(view.getContext(), PhotoActivity.class);
                intent.putExtra(MainActivity.EXECUTION_ID_KEY, executionId);
                startActivity(intent);
            }
        });

        Button supprimer = (Button) findViewById(R.id.Delete);

        supprimer.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                //Create the intent to start another activity
                client.deleteExecution(executionId, new Callback<Boolean>() {
                    @Override
                    public void onSuccess(int i, final Boolean success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    Toast.makeText(getApplicationContext(), "L'execution a bien ete supprimée", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ConsultActivity.this, "L'execution n'a pas pu être suprimée", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(int i, final ErrorResponse errorResponse) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ConsultActivity.this, "Une erreur est survenue: " + errorResponse.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });



}}





