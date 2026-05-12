package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare button variables
    private Button strBtn, jsonObjBtn, jsonArrBtn, imgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        strBtn = findViewById(R.id.btnStringRequest);
        jsonObjBtn = findViewById(R.id.btnJsonObjRequest);
        jsonArrBtn = findViewById(R.id.btnJsonArrRequest);
        imgBtn = findViewById(R.id.btnImageRequest);

        /* Set click listeners for each button */
        strBtn.setOnClickListener(this);
        jsonObjBtn.setOnClickListener(this);
        jsonArrBtn.setOnClickListener(this);
        imgBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId(); // Get the ID of the clicked button
        String userType;
        Intent intent;
        // Check which button was clicked and start the corresponding activity
        if (id == R.id.btnStringRequest) {
            userType = "STUDENT";
            intent = new Intent(MainActivity.this, JsonObjReqActivity.class);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        } else if (id == R.id.btnJsonObjRequest) {
            userType = "ADMIN";
            intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        } else if (id == R.id.btnJsonArrRequest) {
            userType = "GUEST";
            intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        } else if (id == R.id.btnImageRequest) {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        }
    }
}
