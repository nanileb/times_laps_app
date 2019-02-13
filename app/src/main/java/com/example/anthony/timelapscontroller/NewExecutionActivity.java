package com.example.anthony.timelapscontroller;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private SimpleDateFormat sdf = new SimpleDateFormat("kk:mm dd/MM/yyyy");
    private Execution execution = new Execution(null, 0, 0, 0, 0);
    private TextView textdatedebut;
    private TextView textdatefin;
    private TimelapseClient client;
    private EditText edittitre;
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
        //nombrephoto=findViewById(R.id.nombrephoto);
        //editfrequency=findViewById(R.id.editfrequency);
        //String result = editfrequency.toString();
        //Long nombre = nombrephoto.toString();
        //nombrephoto = (execution.getStartTime()-execution.getEndTime()/result;
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
                        execution.setTitle(edittitre.getEditableText().toString());

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
                    }
                }, date.getHours(), date.getMinutes(), true);
                timePickerDialog.show();

            }
        });
        datePickerDialog.show();
    }


    public void confirmation(final View v){

        if (execution.getStartTime()<execution.getEndTime()){
        client.postExecution(execution, new Callback<Execution>() {
            @Override
            public void onSuccess(int i, Execution execution) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewExecutionActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(int i, ErrorResponse errorResponse) {
                Log.e("fdyfyf", errorResponse.getMessage());
            }

        });
            finish();
        }
        else{
            Toast.makeText(NewExecutionActivity.this,"La date de début est supérieur à la date de fin !!", Toast.LENGTH_SHORT).show();
            }




    }

}
