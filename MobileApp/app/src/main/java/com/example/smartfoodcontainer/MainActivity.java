package com.example.smartfoodcontainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private HashMap<String, Object> updateHashMapData;
    private HashMap<String, Object> registerAlarm;
    private int openClosedCount = 0;    // her 4 kapanmada bir
    private int openingClosingBoundary = 4;
    private String bowl_state;

    int alarmCount = 0;

    private ImageView bowl_state_image, system_state_image;
    private TextView text_state_sytem;

    // init
    private void init() {
        databaseReference = FirebaseDatabase.getInstance().getReference(); // root
        updateHashMapData = new HashMap<String, Object>();
        registerAlarm = new HashMap<String, Object>();

        bowl_state_image = (ImageView) findViewById(R.id.bowl_state_image);
        system_state_image = (ImageView) findViewById(R.id.system_state_image);
        text_state_sytem=(TextView) findViewById(R.id.text_state_sytem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // koyu mod

        getData();
        init();
    }


    public void fillContainer(View view){
        AlertDialog.Builder checkAlertDialog = new AlertDialog.Builder(MainActivity.this);
        checkAlertDialog.setTitle("Akıllı Mama");
        checkAlertDialog.setMessage("Hazneyi maksimim seviyesine kadar doldurdunuz mu?");
        checkAlertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateHashMapData = new HashMap<String, Object>();
                databaseReference = FirebaseDatabase.getInstance().getReference();
                updateHashMapData.put("OpenCloseCount", 0);
                databaseReference.updateChildren(updateHashMapData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        checkAlertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Hayır Bastın");
            }
        });
        checkAlertDialog.create().show();

        getData();
    }

    public void setAlarm(View view) {
        registerAlarm = new HashMap<String, Object>();
        // https://www.youtube.com/watch?v=XG8OpQ7X-nY
        final Calendar calendar = Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                // System.out.println(simpleDateFormat.format(calendar.getTime().toString()));

                // alarm ekleme
                databaseReference.child("Alarm").child(String.valueOf(alarmCount + 1)).setValue(simpleDateFormat.format(calendar.getTime()));
                updateAlarmCount();
                Toast.makeText(MainActivity.this, "Alarm kuruldu.", Toast.LENGTH_SHORT).show();
            }
        };

        new TimePickerDialog(MainActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
        // https://www.youtube.com/watch?v=XG8OpQ7X-nY

        getData();
    }


    public void giveFood(View view) {
        // System.out.println(openClosedCount);
        // System.out.println(bowl_state);

        AlertDialog.Builder checkAlertDialog = new AlertDialog.Builder(MainActivity.this);
        checkAlertDialog.setTitle("Akıllı Mama");
        checkAlertDialog.setMessage("Mama vermek istiyor musunuz?");
        checkAlertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (bowl_state.equals("Empty")) {
                    if (openClosedCount < openingClosingBoundary) {
                        updateData();
                    } else {
                        Toast.makeText(MainActivity.this, "Mama bitti veremiyoruz.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Kapta mama var.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        checkAlertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Hayır Bastın");
            }
        });
        checkAlertDialog.create().show();

        getData();
    }

    private void updateData() {
        updateHashMapData = new HashMap<String, Object>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        updateHashMapData.put("State", "True");
        updateHashMapData.put("OpenCloseCount", openClosedCount + 1);
        databaseReference.updateChildren(updateHashMapData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Mama verildi.", Toast.LENGTH_SHORT).show();
                        getData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAlarmCount() {
        updateHashMapData = new HashMap<String, Object>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        updateHashMapData.put("AlarmCount", alarmCount + 1);
        databaseReference.updateChildren(updateHashMapData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        getData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getData() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("OpenCloseCount")) {
                        openClosedCount = Integer.parseInt(dataSnapshot.getValue().toString());

                       switch (openClosedCount){
                           case 0:
                               system_state_image.setImageResource(R.drawable.battery_100);
                               text_state_sytem.setText("  Sistem Durumu %100");
                               break;
                           case 1:
                               system_state_image.setImageResource(R.drawable.battery_75);
                               text_state_sytem.setText("  Sistem Durumu %75");
                               break;
                           case 2:
                               system_state_image.setImageResource(R.drawable.battery_50);
                               text_state_sytem.setText("  Sistem Durumu %50");
                               break;
                           case 3:
                               system_state_image.setImageResource(R.drawable.battery_25);
                               text_state_sytem.setText("  Sistem Durumu %25");
                               break;
                           case 4:
                               system_state_image.setImageResource(R.drawable.charge);
                               text_state_sytem.setText("Hazneyi Doldurun");

                               break;
                           default:
                               break;
                       }

                    } else if (dataSnapshot.getKey().equals("bowl_state")) {
                        bowl_state = dataSnapshot.getValue().toString();
                        if (bowl_state.equals("Empty")) {
                            bowl_state_image.setImageResource(R.drawable.blow_empty);
                        } else {
                            bowl_state_image.setImageResource(R.drawable.blow_full);
                        }
                    } else if (dataSnapshot.getKey().equals("AlarmCount")) {
                        alarmCount = Integer.parseInt(dataSnapshot.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Liste: " + error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}