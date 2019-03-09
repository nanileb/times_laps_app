package com.example.anthony.timelapscontroller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.ErrorResponse;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.BitmapUtil;

import java.io.File;
import java.io.IOException;
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

        new AlertDialog.Builder(this)
                .setTitle("Choisissez le fps")
                .setMessage("nombre d'images par seconde")
                .setView(numberPicker)
                .setNeutralButton("Annuler", null)
                .setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSaving(numberPicker.getValue());
                    }
                })
                .show();

    }

    private void startSaving(int fps) {
        new VideoSavingTask(getApplicationContext(), viewPageAdapter.getCount(), fileName)
                .execute(executionId, fps);
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

    private static class VideoSavingTask extends AsyncTask<Integer,Integer,Exception> {

        private ProgressDialog dialog;
        private int imagesCount;
        private String fileName;

        public VideoSavingTask(Context context, int imagesCount, String fileName) {
            this.dialog = new ProgressDialog(context);
            this.fileName = fileName;
            this.imagesCount = imagesCount;
        }

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setTitle("Création du fichier mp4");
            dialog.setCancelable(false);
            dialog.setMax(imagesCount);
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Annuler", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel(true);
                }
            });
            //dialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... args) {
            int executionId = args[0];
            int fps = args[1];
            TimelapseBasicClient client = new TimelapseBasicClient(ClientSingleton.API_URL);

            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileName);
            try {
                //SequenceEncoder sequenceEncoder = new SequenceEncoder(NIOUtils.writableChannel(file), Rational.R(fps, 1), Format.MPEG_PS, Codec.MPEG4, null);
                AndroidSequenceEncoder sequenceEncoder = AndroidSequenceEncoder.createSequenceEncoder(file, fps);

                for (int i = 0; i < imagesCount; i++) {
                    TimelapseResponse<Bitmap> response = client.getImage(ViewPageAdapter.bitmapResponseHandler, executionId, i);
                    if (!response.isSuccessful()) {
                        //TODO
                        continue;
                    }
                    //sequenceEncoder.encodeNativeFrame(BitmapUtil.fromBitmap(response.getData()));
                    sequenceEncoder.encodeImage(response.getData());
                    publishProgress(i + 1);
                }
                sequenceEncoder.finish();
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int value = values[0];
            dialog.setProgress(value);
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null) {
                dialog.setTitle("Une erreur est survenue");
                dialog.setMessage(e.getMessage());
                Log.e("ERROR", "Error", e);
            } else {
                dialog.setTitle("Sauvegarde terminéee");
            }
            dialog.setCancelable(true);
            dialog.setButton(ProgressDialog.BUTTON_NEUTRAL, "ok", (DialogInterface.OnClickListener) null);
            dialog = null;
        }
    }
}
