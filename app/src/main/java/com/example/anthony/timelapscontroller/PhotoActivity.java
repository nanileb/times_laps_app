package com.example.anthony.timelapscontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Une requete au serveur est faite toutes les 10 secondes pour voir si de nouvelles
 * images ont été prises
 */
public class PhotoActivity extends AppCompatActivity {

    private TimelapseClient client;
    ViewPager viewpager;
    TextView textView;
    private int executionId;
    private ViewPageAdapter viewPageAdapter;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture scheduledFuture;
    private final Runnable getImageTask = new Runnable() {
        @Override
        public void run() {
            client.getImagesCount(executionId, new Callback<Integer>() {
                @Override
                public void onSuccess(int responseCode, final Integer nbImages) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(nbImagesText(nbImages));
                            viewPageAdapter.updateCount(nbImages);
                        }
                    });
                }

                @Override
                public void onError(int responseCode, final ErrorResponse errorResponse) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(viewpager, "Une erreur est survenue:" + errorResponse.getMessage(),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        client = ClientSingleton.getClient();


        viewpager = (ViewPager) findViewById(R.id.VP);
        textView = (TextView) findViewById(R.id.nbImagesText);
        executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        client.getImagesCount(executionId, new Callback<Integer>() {
            @Override
            public void onSuccess(int responseCode, final Integer nbImages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPageAdapter = new ViewPageAdapter(PhotoActivity.this, executionId, nbImages);
                        viewpager.setAdapter(viewPageAdapter);
                        textView.setText(nbImagesText(nbImages));
                    }
                });
            }

            @Override
            public void onError(int responseCode, final ErrorResponse errorResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(viewpager, "Une erreur est survenue lors du chargement des images:" + errorResponse.getMessage(),
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        long period = 4;
        scheduledFuture = executor.scheduleAtFixedRate(getImageTask, period, period, TimeUnit.SECONDS);
    }

    @Override
    protected void onStop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        super.onStop();
    }

    private String nbImagesText(int nbImages) {
        String text = nbImages  == 1 ?
                "Il y a %d image" :
                "Il y a %d images";
        return String.format(text, nbImages);
    }
}
