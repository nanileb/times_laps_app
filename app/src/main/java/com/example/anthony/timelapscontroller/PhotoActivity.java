package com.example.anthony.timelapscontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;

public class PhotoActivity extends AppCompatActivity {

    private TimelapseClient client;
    ViewPager viewpager;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = ClientSingleton.getClient();


        viewpager = (ViewPager) findViewById(R.id.VP);
        textView = (TextView) findViewById(R.id.nbImagesText);
        final int executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        client.getImagesCount(executionId, new Callback<Integer>() {
            @Override
            public void onSuccess(int responseCode, final Integer nbImages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(PhotoActivity.this, executionId, nbImages);
                        viewpager.setAdapter(viewPageAdapter);
                        String formatText = nbImages  == 1 ?
                                "Il y a %d image" :
                                "Il y a %d images";
                        textView.setText(String.format(formatText, nbImages));
                    }
                });
            }

            @Override
            public void onError(int responseCode, ErrorResponse errorResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PhotoActivity.this, "Erreur lors du chargement des images", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

}
