package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeleteAccountActivity extends AppCompatActivity {


    private EditText passwordEdit;  // define password edittext variable
    private EditText emailEdit; //define email edittext variable
    private Button deleteButton;        // define signup button variable
    private Button returnButton;


//    private LinearLayout idInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);            // link to Login activity XML

        /* initialize UI elements */
        passwordEdit = findViewById(R.id.password_edt);
        emailEdit = findViewById(R.id.email_edt);
        deleteButton = findViewById(R.id.delete_btn);  // link to signup button in the Login activity XML
        returnButton = findViewById(R.id.cancel_btn);
//        idInput = findViewById(R.id.id_input);
//        passwordInput = findViewById(R.id.password_input);

        /* click listener on signup button pressed */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Grab strings from inputs
                String password = passwordEdit.getText().toString();
                String email = emailEdit.getText().toString();

                String url = "http://coms-3090-017.class.las.iastate.edu:8080/users/email/" + email;

                delRequest(url);


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
                Intent intent = new Intent(DeleteAccountActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void delRequest(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                response -> {
                    Log.d("Post Request: ", "response: " + response.toString());
                }, error -> {
            Log.e("PostRequest", "Error response received", error);
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json"); // Set content type
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
