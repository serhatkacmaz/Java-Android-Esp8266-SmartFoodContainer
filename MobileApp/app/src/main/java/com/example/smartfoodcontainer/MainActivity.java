package com.example.smartfoodcontainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private HashMap<String, Object> updateHashMapData;
    private int openClosedCount = 0;    // her 4 kapanmada bir
    private int openingClosingBoundary = 4;
    private String bowl_state;

    // init
    private void init() {
        databaseReference = FirebaseDatabase.getInstance().getReference(); // root
        updateHashMapData = new HashMap<String, Object>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getData();
        init();
    }

    public void giveFood(View view) {
        // System.out.println(openClosedCount);
         System.out.println(bowl_state);
        if (bowl_state.equals("Empty")){
            System.out.println("ok1");
            if (openClosedCount < openingClosingBoundary) {
                updateData();
            } else {
                Toast.makeText(MainActivity.this, "Mama bitti veremiyoruz.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(MainActivity.this, "Kapta mama var.", Toast.LENGTH_SHORT).show();
        }

        getData();
    }

    private void updateData() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        updateHashMapData.put("State", "True");
        updateHashMapData.put("OpenCloseCount", openClosedCount + 1);
        databaseReference.updateChildren(updateHashMapData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Mama Verildi.", Toast.LENGTH_SHORT).show();
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
                    } else if (dataSnapshot.getKey().equals("bowl_state")) {
                        bowl_state = dataSnapshot.getValue().toString();
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