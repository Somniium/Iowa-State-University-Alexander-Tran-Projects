package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Edwin Cepeda
 */
public class SignupActivity extends AppCompatActivity {

    private EditText usernameEdit;  // define username edittext variable
    private EditText passwordEdit;  // define password edittext variable
    private EditText emailEdit; //define email edittext variable
    //    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable
    private Button returnButton;


//    private LinearLayout idInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);            // link to Login activity XML

        /* initialize UI elements */
        usernameEdit = findViewById(R.id.username_edt);
        passwordEdit = findViewById(R.id.password_edt);
        emailEdit = findViewById(R.id.email_edt);
//        loginButton = findViewById(R.id.login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.signup_btn);  // link to signup button in the Login activity XML
        returnButton = findViewById(R.id.return_btn);
//        idInput = findViewById(R.id.id_input);
//        passwordInput = findViewById(R.id.password_input);

        /* click listener on login button pressed */
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                /* grab strings from user inputs */
//                String username = usernameEdit.getText().toString();
//                String password = passwordEdit.getText().toString();
//
//                /* when login button is pressed, use intent to switch to Login Activity */
//                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//                startActivity(intent);  // go to MainActivity with the key-value data
//            }
//        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Grab strings from inputs
                String username = usernameEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                String email = emailEdit.getText().toString();


                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (username.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "Usernames cannot have spaces.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!passwordValidation(password)) {
                    return;
                }
                String newUserString = String.format("{\"name\": \"%s\", \"emailId\": \"%s\", \"password\": \"%s\"}", username, email, password);
                JSONObject userObject;
                try {
                    userObject = new JSONObject(newUserString);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

//                String url = "http://10.0.2.2:8080/users";
                String url = "http://coms-3090-017.class.las.iastate.edu:8080/users";

                postRequest(userObject, url);


                /* when signup button is pressed, use intent to switch to Signup Activity */
//                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
//                startActivity(intent);  // go to SignupActivity

            }
        });

        /* click listener on signup button pressed */
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* on click, return to main activity */
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void postRequest(JSONObject userObject, String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, userObject,
                response -> {
                    // SUCCESS (HTTP 200 OK)
                    Log.d("Post Request: ", "response: " + response.toString());
                    String message;
                    try {
                        message = response.get("message").toString();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    if (message.equals("success")) {
                        Toast.makeText(getApplicationContext(), "Welcome to CyVal!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.putExtra("USERTYPE", "STUDENT");
                        startActivity(intent);  // go to LoginActivity if signup successful
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                }, error -> {
            // ERROR (HTTP 4xx or 5xx)
            Log.e("PostRequest", "Error response received", error);
//            final int errorCode = error.networkResponse.statusCode;
            if (error.networkResponse != null && error.networkResponse.data != null) {
                String serverError = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                // This will display "Invalid username", "Invalid password", etc.
                Toast.makeText(SignupActivity.this, serverError, Toast.LENGTH_LONG).show();
                Log.e("PostRequest", serverError, error);
            } else {
                // Fallback if the server is offline or unreachable
                Toast.makeText(SignupActivity.this, "Network Error: Could not reach server", Toast.LENGTH_LONG).show();
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Set content type
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean passwordValidation(String password) {
        //This string will check each pattern.
        String currentPattern;

        //null check
        if (password == null) {
            Toast.makeText(getApplicationContext(), "Error: Password not received.", Toast.LENGTH_LONG).show();
            return false;
        }

        //length check
        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Password must be 8 or more characters.", Toast.LENGTH_LONG).show();
            return false;
        }

        //lowercase letter check
        currentPattern = ".*[a-z].*";
        if (!(password.matches(currentPattern))) {
            Toast.makeText(getApplicationContext(), "Password must contain a lowercase letter.", Toast.LENGTH_LONG).show();
            return false;
        }

        //uppercase letter check
        currentPattern = ".*[A-Z].*";
        if (!(password.matches(currentPattern))) {
            Toast.makeText(getApplicationContext(), "Password must contain an uppercase letter.", Toast.LENGTH_LONG).show();
            return false;
        }

        //number check
        currentPattern = ".*[0-9].*";
        if (!(password.matches(currentPattern))) {
            Toast.makeText(getApplicationContext(), "Password must contain a number.", Toast.LENGTH_LONG).show();
            return false;
        }

        //special char check
        currentPattern = ".*[!?@#$%^&+=].*";
        if (!(password.matches(currentPattern))) {
            Toast.makeText(getApplicationContext(), "Password must contain a special character (!?@#$%^&+=).", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

}
