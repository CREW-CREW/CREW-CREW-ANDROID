package com.example.crewcrew_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ExerciseSelection extends AppCompatActivity {

    private ImageButton runningBtn;
    private Button swimmingBtn;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_selection);
        runningBtn = findViewById(R.id.runningBtn);

        runningBtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RunningCrewInfoList.class);
                startActivity(intent);
            }
        });
    }
}