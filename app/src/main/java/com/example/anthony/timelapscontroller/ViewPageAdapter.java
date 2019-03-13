package com.example.anthony.timelapscontroller;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.ErrorResponse;
import com.tambapps.http.restclient.request.handler.response.ResponseHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

public class ViewPageAdapter extends PagerAdapter {

    private final TimelapseBasicClient client;
    private final Executor executor;
    private Activity activity;
    private LayoutInflater layoutInflater;
    private int nbImages;
    private final int executionId;


    public static final ResponseHandler<Bitmap> bitmapResponseHandler = new ResponseHandler<Bitmap>() {
        @Override
        public Bitmap convert(InputStream inputStream) throws IOException {
            return BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
        }
    };

    public ViewPageAdapter(TimelapseBasicClient client, Executor executor, Activity activity, int executionId, int nbImages) {
        this.client = client;
        this.executor = executor;
        this.activity = activity;
        this.nbImages = nbImages;
        this.executionId = executionId;
    }

    //to run on UI thread
    public void updateCount(int nbImages) {
        if (this.nbImages != nbImages) {
            this.nbImages = nbImages;
            notifyDataSetChanged();
        }
    }
    @Override
    public int getCount() {
        return nbImages;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {


        layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_layout, null);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.imageProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        //TODO (pour Nelson) les images sont coupees
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final TimelapseResponse<Bitmap> response = client.getImage(bitmapResponseHandler, executionId, position);
                if (response.isSuccessful()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(response.getData());
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageResource(R.drawable.error);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        ViewPager Vp2 = (ViewPager) container;
        Vp2.addView(view, 0);




        return view;


    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager Vp2 = (ViewPager) container;
        View view = (View) object;
        Vp2.removeView(view);
    }
}
