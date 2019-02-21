package com.example.anthony.timelapscontroller;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app4.project.timelapse.api.client.Callback;
import com.app4.project.timelapse.api.client.TimelapseAsyncClient;
import com.app4.project.timelapse.api.client.TimelapseClient;
import com.app4.project.timelapse.model.ErrorResponse;
import com.app4.project.timelapse.model.Execution;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NewExecutionActivity extends AppCompatActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private Execution execution = new Execution(null, 0, 0, 0, 0);
    private TextView textdatedebut;
    private TextView textdatefin;
    private TimelapseClient client;
    private EditText edittitre;
    private EditText frequency;
    private Long Calcul = 0L;
    private TextView nbphoto;
    //private TextView nombrephoto;
    //private EditText editfrequency;
    //private int calcul;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_execution);
        textdatedebut=findViewById(R.id.datedebuttext);
        textdatefin = findViewById(R.id.datefintext);
        client  = ClientSingleton.getClient();
        edittitre=findViewById(R.id.edittitle);
        frequency=findViewById(R.id.editfrequency);
        nbphoto=findViewById(R.id.nombrephoto);
        //nombrephoto=findViewById(R.id.nombrephoto);
        //editfrequency=findViewById(R.id.editfrequency);
        //String result = editfrequency.toString();
        //Long nombre = nombrephoto.toString();
        //nombrephoto = (execution.getStartTime()-execution.getEndTime()/result;


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


    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void choisirTemps(final View v) {
        final Date date = new Date();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date.setTime(new Date(year-1900, month, dayOfMonth).getTime());

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewExecutionActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewExecutionActivity.this, new TimePickerDialog.OnTimeSetListener() {
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


    public void confirmation(final View v){
        execution.setTitle(edittitre.getEditableText().toString());


        if(execution.getTitle()==null || execution.getTitle().trim().isEmpty()){
            Toast.makeText(NewExecutionActivity.this,"Veuillez mettre un titre", Toast.LENGTH_SHORT).show();
            return;
        }

        String f = frequency.getEditableText().toString();

        if(f.trim().isEmpty()){
            Toast.makeText(NewExecutionActivity.this,"Veuillez mettre une fréquence", Toast.LENGTH_SHORT).show();
            return;
        }

        execution.setPeriod(Long.parseLong(f));


        if (execution.getStartTime()<execution.getEndTime()){
        client.postExecution(execution, new Callback<Execution>() {
            @Override
            public void onSuccess(int i, Execution execution) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(int i, final ErrorResponse errorResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(textdatedebut, "Une erreur est survenue: " + errorResponse.getTitle(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

        });
        }
        else{
            Toast.makeText(NewExecutionActivity.this,"La date de début est supérieur à la date de fin !!", Toast.LENGTH_SHORT).show();
            }




    }

    private void updateCalcul() {
        if (execution.getPeriod() > 0) {
            Calcul = (execution.getEndTime()-execution.getStartTime())/(1000 *execution.getPeriod());
        }
    }
}
