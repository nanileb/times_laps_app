package com.example.anthony.timelapscontroller;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class PictureViewActivity extends TimelapseActivity {
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);
        progressbar = findViewById(R.id.progressbar2);
        Intent intent = getIntent();
        final int executionId = intent.getIntExtra("executionId", 0);
        final int fileId = intent.getIntExtra("imageId", 0);


        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(R.id.imageView);

        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final TimelapseResponse<Bitmap> response = getClient().getImage(ViewPageAdapter.bitmapResponseHandler, executionId, fileId);
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(ImageSource.bitmap(response.getData()));
                            progressbar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressbar.setVisibility(View.INVISIBLE);
                            imageView.setImage(ImageSource.resource(R.drawable.error));
                        }
                    });
                }
            }
        });
    }
}
