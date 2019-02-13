package com.example.anthony.timelapscontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;

public class PhotoActivity extends AppCompatActivity {

    private TimelapseClient client;
    ViewPager viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = ClientSingleton.getClient();


        viewpager = (ViewPager) findViewById(R.id.VP);
        final int executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        client.getExecution(executionId, new Callback<Execution>() {
            @Override
            public void onSuccess(int i, final Execution execution) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        Toast.makeText(PhotoActivity.this, "Erreur lors du chargement de l'execution", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(this);

        viewpager.setAdapter(viewPageAdapter);
    }

}
