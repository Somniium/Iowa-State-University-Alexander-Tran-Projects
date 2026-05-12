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
    private Button returnButton;
    private TextView loginHeader;

    private LinearLayout idInput, passwordInput, adminInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML
        returnButton = findViewById(R.id.return_btn);
        loginHeader = findViewById(R.id.login_header);
        idInput = findViewById(R.id.id_input);
        passwordInput = findViewById(R.id.password_input);
        adminInput = findViewById(R.id.admin_input);

        Bundle extras = getIntent().getExtras();

        if (extras.getString("USERTYPE").equals("STUDENT")) {
            loginHeader.setText("Student Login");
            adminInput.setVisibility(View.INVISIBLE);
        } else if (extras.getString("USERTYPE").equals("ADMIN")) {
            loginHeader.setText("Admin Login");
        } else if (extras.getString("USERTYPE").equals("GUEST")) {
            loginHeader.setText("Guest Login");
            idInput.setVisibility(View.INVISIBLE);
            passwordInput.setVisibility(View.INVISIBLE);
            adminInput.setVisibility(View.INVISIBLE);
        } else {
            loginHeader.setText("Something is wrong. Close the app.");
        }

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* when login button is pressed, use intent to switch to Login Activity */
//                Intent intent = new Intent(LoginActivity.this, FakeFeedActivity.class);
//                intent.putExtra("USERNAME", username);  // key-value to pass to the MainActivity
//                intent.putExtra("PASSWORD", password);  // key-value to pass to the MainActivity
//                startActivity(intent);  // go to MainActivity with the key-value data
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

        /* click listener on signup button pressed */
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* on click, return to main activity */
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
