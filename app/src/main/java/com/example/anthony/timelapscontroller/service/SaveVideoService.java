package com.example.anthony.timelapscontroller.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.example.anthony.timelapscontroller.ClientSingleton;
import com.example.anthony.timelapscontroller.R;
import com.example.anthony.timelapscontroller.ViewPageAdapter;

import org.jcodec.api.android.AndroidSequenceEncoder;

import java.io.File;
import java.io.IOException;

public class SaveVideoService extends JobService {

    private static final String ACTION_CANCEL = SaveVideoService.class.getName() + ".CANCEL";
    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private AsyncTask task;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(ACTION_CANCEL);
        registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        NotificationManager notificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notifId = bundle.getInt("id");
        int imagesCount = bundle.getInt("imagesCount");
        int executionId = bundle.getInt("executionId");
        int fps = bundle.getInt("fps");
        String fileName = bundle.getString("fileName");

        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_CANCEL), 0);
        Runnable endRunnable = new Runnable() {
            @Override
            public void run() {
                unregisterReceiver(notificationBroadcastReceiver);
                jobFinished(params, false);
            }
        };

        NotificationCompat.Builder notifBuilder = notificationBuilder(notificationManager, notifId);
        notifBuilder.addAction(android.R.drawable.ic_delete, "cancel", cancelIntent);
        task = new VideoSavingTask(notifBuilder, notificationManager, notifId, imagesCount, fileName, endRunnable)
        .execute(executionId, fps);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }


    public class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public final void onReceive(Context context, Intent intent) {
            if (ACTION_CANCEL.equals(intent.getAction()) && task != null) {
                task.cancel(true);
                task = null;
            }
        }
    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        if (channelExists(notificationManager))
            return;
        NotificationChannel channel = new NotificationChannel(getClass().getName(), getClass().getName(), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("notifications for " + getClass().getName());
        channel.enableLights(false);
        notificationManager.createNotificationChannel(channel);
    }

    @TargetApi(26)
    private boolean channelExists(NotificationManager notificationManager) {
        NotificationChannel channel = notificationManager.getNotificationChannel(getClass().getName());
        return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    private NotificationCompat.Builder notificationBuilder(NotificationManager notificationManager, int notifId) {
        if (Build.VERSION.SDK_INT >= 26) {
            createChannel(notificationManager);
        }
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, getClass().getName());
        notifBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.time_lapse)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.time_lapse))
                .setColor(getResources().getColor(R.color.colorPrimary));
        Notification notification = notifBuilder.build();
        notificationManager.notify(notifId, notification);
        return notifBuilder;
    }

    private static class VideoSavingTask extends AsyncTask<Integer,Integer,Exception> {

        private NotificationCompat.Builder notifBuilder;
        private NotificationManager notificationManager;
        private final int notifId;
        private int imagesCount;
        private String fileName;
        private Runnable endRunnable;
        private final File file;

        public VideoSavingTask(NotificationCompat.Builder notifBuilder, NotificationManager notificationManager, int notifId, int imagesCount, String fileName,
                               Runnable endRunnable) {
            this.notifBuilder = notifBuilder;
            this.notificationManager = notificationManager;
            this.notifId = notifId;
            this.imagesCount = imagesCount;
            this.fileName = fileName;
            this.endRunnable = endRunnable;
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileName);
        }

        @Override
        protected void onPreExecute() {
            notifBuilder.setContentTitle("Sauvegarde de la video...");
            onProgressUpdate(0);
        }

        @Override
        protected Exception doInBackground(Integer... args) {
            int executionId = args[0];
            int fps = args[1];
            TimelapseBasicClient client = new TimelapseBasicClient(ClientSingleton.API_URL);


            try {
                AndroidSequenceEncoder sequenceEncoder = AndroidSequenceEncoder.createSequenceEncoder(file, fps);

                for (int i = 0; i < imagesCount; i++) {
                    TimelapseResponse<Bitmap> response = client.getImage(ViewPageAdapter.bitmapResponseHandler, executionId, i);
                    if (!response.isSuccessful()) {
                        Log.e("SaveVideoService", "Couldn't save image " + i);
                        //TODO
                        continue;
                    }
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

            notifBuilder.setProgress(imagesCount, value, false);
            notificationManager.notify(notifId, notifBuilder.build());
        }

        @Override
        protected void onPostExecute(Exception e) {
            notifBuilder.mActions.clear();
            notifBuilder
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0,0, false);

            if (e == null) {
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent chooser = Intent.createChooser(intent, "Ouvrir la video avec...");
                intent.setDataAndType(path, "video/*");
                PendingIntent pendingIntent = PendingIntent.getActivity(notifBuilder.mContext, 0, chooser, PendingIntent.FLAG_CANCEL_CURRENT);
                notifBuilder.setContentTitle("Sauvegarde terminéee");
                notifBuilder.setContentText(fileName);
                notifBuilder.setContentIntent(pendingIntent);
            } else {
                notifBuilder.setContentTitle("Une erreur est survenue");
                notifBuilder.setContentText(String.valueOf(e.getMessage()));
            }
            notificationManager.notify(notifId, notifBuilder.build());
            endRunnable.run();
            dispose();
        }

        void dispose() {
            notifBuilder = null;
            notificationManager = null;
        }
    }
}
