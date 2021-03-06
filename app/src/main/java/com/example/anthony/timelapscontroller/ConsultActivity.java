package com.example.anthony.timelapscontroller;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;

import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseBasicClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.api.client.TimelapseResponse;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;


public class ConsultActivity extends TimelapseActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Execution execution;
    private Long Calcul = 0L;
    private TextView nbphoto;
    private TextView textdatedebut;
    private EditText frequency;
    private TextView textdatefin;
    private EditText edittitre;
    private int executionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult_);

        nbphoto=findViewById(R.id.photoreel);
        textdatedebut=findViewById(R.id.heuredebutreelle);
        textdatefin = findViewById(R.id.heurefinreelle);
        frequency = findViewById(R.id.frequencereelle);
        edittitre = findViewById(R.id.Nomdexe);

        frequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    return;
                }
                execution.setPeriod(Long.parseLong(s.toString()));
                updateCalcul();
                nbphoto.setText(Calcul.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        executionId = getIntent().getIntExtra(MainActivity.EXECUTION_ID_KEY, 0);

        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final TimelapseResponse<Execution> response = getClient().getExecution(executionId);
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ConsultActivity.this.execution = response.getData();
                            edittitre.setText(execution.getTitle());
                            textdatedebut.setText(sdf.format(execution.getStartTime()));
                            textdatefin.setText(sdf.format(execution.getEndTime()));
                            frequency.setText( String.valueOf(execution.getPeriod()));
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ConsultActivity.this, "Erreur lors du chargement de l'execution: " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        Button Photos = (Button) findViewById(R.id.PhotosVisua);

        Photos.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                //Create the intent to start another activity
                Intent intent = new Intent(view.getContext(), PhotoActivity.class);
                intent.putExtra(MainActivity.EXECUTION_ID_KEY, executionId);
                intent.putExtra(PhotoActivity.EXECUTION_NAME_KEY, edittitre.getText().toString());
                startActivity(intent);
            }
        });

        Button supprimer = (Button) findViewById(R.id.Delete);

        supprimer.setOnClickListener(new View.OnClickListener(){
            @Override
            //On click function
            public void onClick(View view) {
                //Create the intent to start another activity
                getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        final TimelapseResponse<Boolean> response = getClient().deleteExecution(executionId);
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.getData()) {
                                        Toast.makeText(getApplicationContext(), "L'execution a bien ete supprimée", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(ConsultActivity.this, "L'execution n'a pas pu être suprimée", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ConsultActivity.this, "Une erreur est survenue: " + response.getError().getTitle(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void choisirTemps(final View v) {
        final Date date = new Date();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setTime(new Date(year-1900, month, dayOfMonth).getTime());

                TimePickerDialog timePickerDialog = new TimePickerDialog(ConsultActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.setHours( hourOfDay);
                        date.setMinutes(minute);
                        execution.setStartTime(date.getTime());

                        textdatedebut.setText(sdf.format(date));

                        updateCalcul();
                        nbphoto.setText(Calcul.toString());

                    }
                }, date.getHours(), date.getMinutes(), true);
                timePickerDialog.show();
            }
        });

        datePickerDialog.show();

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void choisirfin(final View v) {
        final Date date = new Date();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setTime(new Date(year-1900, month, dayOfMonth).getTime());

                TimePickerDialog timePickerDialog = new TimePickerDialog(ConsultActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.setHours(hourOfDay);
                        date.setMinutes(minute);
                        execution.setEndTime(date.getTime());

                        textdatefin.setText(sdf.format(date));

                        updateCalcul();
                        nbphoto.setText(Calcul.toString());
                    }
                }, date.getHours(), date.getMinutes(), true);
                timePickerDialog.show();

            }
        });
        datePickerDialog.show();
    }


    public void Modification(final View v){
        execution.setTitle(edittitre.getEditableText().toString());


        if(execution.getTitle()==null || execution.getTitle().trim().isEmpty()){
            Toast.makeText(ConsultActivity.this,"Veuillez mettre un titre", Toast.LENGTH_SHORT).show();
            return;
        }

        String f = frequency.getEditableText().toString();

        if(f.trim().isEmpty()){
            Toast.makeText(ConsultActivity.this,"Veuillez mettre une fréquence", Toast.LENGTH_SHORT).show();
            return;
        }

        execution.setPeriod(Long.parseLong(f));


        if (execution.getStartTime()<execution.getEndTime()){
            getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final TimelapseResponse<Execution> response = getClient().putExecution(executionId, execution);
                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(textdatedebut, "Une erreur est survenue: " + response.getData().getTitle(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
        else{
            Toast.makeText(ConsultActivity.this,"La date de début est supérieur à la date de fin !!", Toast.LENGTH_SHORT).show();
        }




    }

    private void updateCalcul() {
        if (execution.getPeriod() > 0) {
            Calcul = (execution.getEndTime()-execution.getStartTime()+4)/(1000 *execution.getPeriod());
        }
    }
}





