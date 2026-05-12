package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable
    private TextView loginHeader;
    private LinearLayout studentID;
    private LinearLayout username;
    private LinearLayout password;
    private LinearLayout adminCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        loginHeader = findViewById(R.id.loginHeader);
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML
        studentID = findViewById(R.id.studentIdInput);
        username = findViewById(R.id.usernameInput);
        password = findViewById(R.id.passwordInput);
        adminCode = findViewById(R.id.adminCodeInput);

        /* extract data passed into this activity from another activity */
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            loginHeader.setText("Something is wrong!");
            username.setVisibility(View.INVISIBLE);             // set username text invisible initially
            studentID.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
            adminCode.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            signupButton.setVisibility(View.INVISIBLE);
        } else {
            if (extras.getString("USERTYPE").equals("admin")) {
                loginHeader.setText("Administrator Login");
                studentID.setVisibility(View.INVISIBLE);
            } else if (extras.getString("USERTYPE").equals("student")) {
                loginHeader.setText("Student Login");
                adminCode.setVisibility(View.INVISIBLE);
            } else if (extras.getString("USERTYPE").equals("guest")) {
                loginHeader.setText("Guest Login");
                username.setVisibility(View.INVISIBLE);             // set username text invisible initially
                studentID.setVisibility(View.INVISIBLE);
                password.setVisibility(View.INVISIBLE);
                adminCode.setVisibility(View.INVISIBLE);
            }
        }

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                if (extras != null) {
                    intent.putExtra("USERTYPE", extras.getString("USERTYPE"));
                }
                intent.putExtra("USERNAME", username);  // key-value to pass to the MainActivity
                intent.putExtra("PASSWORD", password);  // key-value to pass to the MainActivity
                startActivity(intent);  // go to MainActivity with the key-value data
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });
    }
}