package com.example.anthony.timelapscontroller;

import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseFakeClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;
import com.app4.project.timelapse.model.GlobalState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String EXECUTION_ID_KEY = "EXECUTION_ID_KEY";
    private List<Execution> executions = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy kk:mm");
    RecyclerView recyclerView;
    private ExecutionAdapter adapter;
    TextView Textinvisible1,Texinvisible2;
    Animation FabOpen, FabClose, FabRclockwise,FabRanticlockwise;
    boolean isOpen = false;
    private TimelapseClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client  = ClientSingleton.getClient();
        /*
        executions.add(new Execution(System.currentTimeMillis(), System.currentTimeMillis() + 10000,0, 100, "FLEUR"));
        executions.add(new Execution(System.currentTimeMillis() + 2032040, System.currentTimeMillis() + 10000,0, 100, "Eclipse"));
        executions.add(new Execution(System.currentTimeMillis() + 48324235325, System.currentTimeMillis() + 10000,0, 100, "Pop corn qui explose"));
        executions.add(new Execution(System.currentTimeMillis() + 8238385, System.currentTimeMillis() + 10000,0, 100, "SATELLITE"));
        */
        recyclerView = findViewById(R.id.recyclerview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_plus);
        final FloatingActionButton fab_new = (FloatingActionButton) findViewById(R.id.fab_new);
        final FloatingActionButton fab_new2 = (FloatingActionButton) findViewById(R.id.fab_new2);

        Textinvisible1 = (TextView) findViewById(R.id.textinvisible);
        Texinvisible2 =(TextView) findViewById(R.id.textinvisible2);

        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        FabRclockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        FabRanticlockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOpen)
                {
                    fab_new2.startAnimation(FabClose);
                    fab_new.startAnimation(FabClose);
                    Textinvisible1.startAnimation(FabClose);
                    Texinvisible2.startAnimation(FabClose);
                    fab.startAnimation(FabRanticlockwise);
                    fab_new.setClickable(false);
                    fab_new2.setClickable(false);
                    isOpen=false;
                }
                else
                {
                    fab_new2.startAnimation(FabOpen);
                    fab_new.startAnimation(FabOpen);
                    Textinvisible1.startAnimation(FabOpen);
                    Texinvisible2.startAnimation(FabOpen);
                    fab.startAnimation(FabRclockwise);
                    fab_new.setClickable(true);
                    fab_new2.setClickable(true);
                    isOpen=true;

                }


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

        fab_new2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,NewExecutionActivity.class));
            }
        });


        fab_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ActionActivity.class));
            }
        });





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
        client.getGlobalState(new Callback<GlobalState>() {
            @Override
            public void onSuccess(int i, final GlobalState globalState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // RecyclerView.setHasFixedSize(true);

                        // use a linear layout manager
                        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
                        recyclerView.setLayoutManager(mLayoutManager);
                        executions.clear();
                        for (Execution e : globalState.getExecutions()) {
                            executions.add(e);
                        }
                        // specify an adapter (see also next example)
                        adapter = new ExecutionAdapter();
                        recyclerView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onError(int i, ErrorResponse errorResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Erreur lors du chargement des Executions", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
