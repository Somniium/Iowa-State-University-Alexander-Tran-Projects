package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class UserTypeActivity extends AppCompatActivity {

    private Button adminButton;         // define login button variable
    private Button studentButton;        // define signup button variable
    private Button guestButton;        // define signup button variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usertype);            // link to Login activity XML

        /* initialize UI elements */
        adminButton = findViewById(R.id.adminButton);
        studentButton = findViewById(R.id.studentButton);
        guestButton = findViewById(R.id.guestButton);    // link to login button in the Login activity XML


        /* click listener on login button pressed */
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when admin button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(UserTypeActivity.this, LoginActivity.class);
                intent.putExtra("USERTYPE", "admin");  // key-value to pass to the LoginActivity
                startActivity(intent);  // go to MainActivity with the key-value data
            }
        });

        /* click listener on student button pressed */
        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when student button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(UserTypeActivity.this, LoginActivity.class);
                intent.putExtra("USERTYPE", "student");  // key-value to pass to the LoginActivity
                startActivity(intent);  // go to SignupActivity
            }
        });

        /* click listener on student button pressed */
        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when student button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(UserTypeActivity.this, LoginActivity.class);
                intent.putExtra("USERTYPE", "guest");  // key-value to pass to the LoginActivity
                startActivity(intent);  // go to SignupActivity
            }
        });
    }
}