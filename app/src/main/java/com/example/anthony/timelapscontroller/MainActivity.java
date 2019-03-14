package com.example.anthony.timelapscontroller;

import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseFakeClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.GlobalState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends TimelapseActivity {

    public static final String EXECUTION_ID_KEY = "EXECUTION_ID_KEY";
    private List<Execution> executions = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
    RecyclerView recyclerView;
    private ExecutionAdapter adapter;
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview);
        progressbar = findViewById(R.id.Progressbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_plus);



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this,NewExecutionActivity.class));

            }
        });



        /*
        fab_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Rappel_simple.class));

            }
        });
        */






        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.camera_state) {
            startActivity(new Intent(this, CameraActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class ExecutionAdapter extends RecyclerView.Adapter<MyViewHolder>  {


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            // create a new view
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.execution_layout, viewGroup, false);

            MyViewHolder vh = new MyViewHolder(v);





            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
            final Execution execution = executions.get(i);
            myViewHolder.title.setText(execution.getTitle());
            myViewHolder.dateCommencement.setText("Début: " + sdf.format(new Date(execution.getStartTime())));
            myViewHolder.datefin.setText("Fin: " + sdf.format(new Date(execution.getEndTime())));
            myViewHolder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this,ConsultActivity.class);
                    intent.putExtra(EXECUTION_ID_KEY, execution.getId());
                    startActivity(intent);
                }
            });

            if (i % 2 == 0) {
                //view.setBackgroundColor(activity.getColor(R.color.colorPrimaryDark));
                myViewHolder.root.setBackgroundResource(R.color.ColorView2);

            } else {
                myViewHolder.root.setBackgroundResource(R.color.ColorView);
            }
        }

        @Override
        public int getItemCount() {
            return executions.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView datefin;
        public TextView dateCommencement;
        public View root;
        public MyViewHolder(View v) {
            super(v);
            root = v;
            title = v.findViewById(R.id.title);
            dateCommencement = v.findViewById(R.id.datecommencement);
            datefin = v.findViewById(R.id.datefin);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Toast.makeText(this, "SISI", Toast.LENGTH_SHORT).show();
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                TimelapseResponse<GlobalState> response = getClient().getGlobalState();
                if (response.isSuccessful()) {
                    final GlobalState globalState = response.getData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // RecyclerView.setHasFixedSize(true);
                            // use a linear layout manager
                            LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                            recyclerView.setLayoutManager(mLayoutManager);
                            executions.clear();
                            executions.addAll(Arrays.asList(globalState.getExecutions()));
                            // specify an adapter (see also next example)
                            adapter = new ExecutionAdapter();
                            recyclerView.setAdapter(adapter);


                            progressbar.setVisibility(View.GONE);
                        }
                    });
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressbar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Erreur lors du chargement des Executions", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
