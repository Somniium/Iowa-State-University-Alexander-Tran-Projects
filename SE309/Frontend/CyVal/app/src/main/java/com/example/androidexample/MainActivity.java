package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare button variables
    private Button loginBtn, signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);

        /* Set click listeners for each button */
        loginBtn.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId(); // Get the ID of the clicked button
        String userType;
        Intent intent;
        // Check which button was clicked and start the corresponding activity
        if (id == R.id.loginBtn) {
            userType = "STUDENT";
            intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);
        } else if (id == R.id.signUpBtn) {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        }
    }
}
