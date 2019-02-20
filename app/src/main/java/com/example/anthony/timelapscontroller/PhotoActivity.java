package com.example.anthony.timelapscontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;

import java.util.Locale;
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
    private Button videoButton;
    private ViewPageAdapter viewPageAdapter;
    private ScheduledExecutorService executor;
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
                            videoButton.setVisibility(nbImages > 1 ? View.VISIBLE : View.GONE);
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

        executor = Executors.newSingleThreadScheduledExecutor();
        client = ClientSingleton.getClient();

        viewpager = (ViewPager) findViewById(R.id.VP);
        videoButton = findViewById(R.id.voir_video);
        final TextView imageIndexText = (TextView) findViewById(R.id.imageIndexText);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                PagerAdapter adapter = viewpager.getAdapter();
                int count = 0;
                if (adapter != null) {
                    count = adapter.getCount();
                }
                imageIndexText.setText(String.format(Locale.getDefault(), "image %d sur %d", i + 1, count));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        textView = (TextView) findViewById(R.id.nbImagesText);

        executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        client.getImagesCount(executionId, new Callback<Integer>() {
            @Override
            public void onSuccess(int responseCode, final Integer nbImages) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewPageAdapter = new ViewPageAdapter(PhotoActivity.this, executionId, nbImages);
                        imageIndexText.setText(nbImages == 0 ? "Pas d'images" :
                                String.format(Locale.getDefault(), "image %d sur %d", 1, nbImages));

                        viewpager.setAdapter(viewPageAdapter);
                        textView.setText(nbImagesText(nbImages));
                        videoButton.setVisibility(nbImages > 1 ? View.VISIBLE : View.GONE);
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
        String text = "Il y a %d " + ( nbImages  == 1 ? "image" : "images");
        return String.format(text, nbImages);
    }

    public void videoActivity(View v) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(MainActivity.EXECUTION_ID_KEY, executionId);
        startActivity(intent);
    }
}
