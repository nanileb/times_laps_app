package com.example.anthony.timelapscontroller;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.example.anthony.timelapscontroller.service.SaveVideoService;

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

    public static final String EXECUTION_NAME_KEY = "EXNAMESTRINGKEY";
    private TimelapseClient client;
    ViewPager viewpager;
    TextView textView;
    private int executionId;
    private String fileName;
    private Button videoButton;
    private ViewPageAdapter viewPageAdapter;
    private ScheduledExecutorService executor;
    private ScheduledFuture scheduledFuture;
    private final static int WRITE_PERMISSION_REQUEST = 2;

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

        Intent intent = getIntent();
        executionId = intent.getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);
        String executionName = intent.getStringExtra(EXECUTION_NAME_KEY);
        if (executionName == null) {
            executionName = "";
        }
        fileName = createName(executionName) + ".mp4";
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

    private String createName(String executionName) {
        String result = executionName.replace("[^A-Za-z0-9()\\[\\]]", "");
        if (result.trim().isEmpty()) {
            return "video";
        }
        return result;
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

    public void saveVideo(View v) {
        if (!hasWritePermission()) {
            requestWritePermission();
            return;
        }

        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setValue(24);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Choisissez le fps")
                .setMessage("Nombre d'images par seconde")
                .setView(numberPicker)
                .setNeutralButton("Annuler", null)
                .setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSaving(numberPicker.getValue());
                    }
                }).create();

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newFps) {
                long nbImages = viewPageAdapter.getCount();
                long length = nbImages / newFps;
                long hours = TimeUnit.SECONDS.toHours(length);
                long minutes = TimeUnit.SECONDS.toMinutes(length - TimeUnit.HOURS.toSeconds(hours));
                long seconds = length - (TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes));
                dialog.setMessage(String.format(Locale.FRANCE, "La video durera %dh%dm%ds", hours, minutes, seconds));
            }
        });

        dialog.show();

    }

    private void startSaving(int fps) {
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle bundle = new PersistableBundle();

        bundle.putInt("id", 8);
        bundle.putInt("imagesCount",viewPageAdapter.getCount());
        bundle.putInt("fps", fps);
        bundle.putInt("executionId", executionId);
        bundle.putString("fileName", fileName);

        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(1,
                new ComponentName(this, SaveVideoService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(bundle);
        jobScheduler.schedule(jobInfoBuilder.build());
    }

    private boolean hasWritePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("S'il vous plait, autorisez l'application à avoir accès à votre mémoire")
                    .setNeutralButton("Non", null)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestWritePermission();
                        }
                    })
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You didn't grant write permissions", Toast.LENGTH_SHORT).show();
                } else {
                    saveVideo(null);
                }
            }
        }
    }


}
