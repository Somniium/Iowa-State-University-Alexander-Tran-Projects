package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class UserManageActivity extends AppCompatActivity {

    // Declare button variables
    private Button updateBtn, deleteBtn, returnBtn;
    private ImageButton searchBtn;

    // Declare layout variables
    private LinearLayout passwordInput, idInput, usernameInput, emailInput;

    //Declare edit variables
    private EditText searchEdt, passwordEdt, idEdt, usernameEdt, emailEdt;

    //Declare variables for holding user information
    private String username, password, email;
    private int id;
    private boolean active;
    private int userID = -1;
    private int adminID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        searchBtn = findViewById(R.id.searchBtn);
        updateBtn = findViewById(R.id.updateBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);

        passwordInput = findViewById(R.id.password_input);
        idInput = findViewById(R.id.id_input);
        usernameInput = findViewById(R.id.username_input);
        emailInput = findViewById(R.id.email_input);

        searchEdt = findViewById(R.id.userEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        idEdt = findViewById(R.id.idEdt);
        usernameEdt = findViewById(R.id.usernameEdt);
        emailEdt = findViewById(R.id.emailEdt);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            userID = extras.getInt("USER_ID", -1);
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiClient.get(UserManageActivity.this, "/users/name/" + searchEdt.getText(), new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            username = response.getString("name");
                            email = response.getString("emailId");
                            id = response.getInt("id");
//                            password = response.getString("password");
                            active = response.getBoolean("active");
                            usernameEdt.setText(username);
                            emailEdt.setText(email);
                            idEdt.setText(String.valueOf(id));
                            passwordEdt.setText(password);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        makeVisible();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(UserManageActivity.this, "Could not find user.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiClient.delete(UserManageActivity.this, "/users/id/" + id, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(UserManageActivity.this, "User '" + username + "' terminated.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserManageActivity.this, AdminPanelActivity.class);
                        intent.putExtra("ADMIN_ID", adminID);
                        intent.putExtra("USER_ID", userID);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(UserManageActivity.this, "Could not find user.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserManageActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                JSONObject updatedUserData = new JSONObject();
                try {
                    updatedUserData.put("id", Integer.parseInt(idEdt.getText().toString()));
                    updatedUserData.put("name", usernameEdt.getText().toString());
                    updatedUserData.put("emailId", emailEdt.getText().toString());
                    if (!passwordEdt.getText().toString().isEmpty()) {
                        updatedUserData.put("password", passwordEdt.getText().toString());
                    } else {
                        updatedUserData.put("password", null);
                    }

                    updatedUserData.put("active", active);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Sending...", updatedUserData.toString());
//                ApiClient.put(UserManageActivity.this, "/users/" + idEdt.getText().toString(), updatedUserData, new Api_Interface() {
//                    @Override
//                    public void onSuccess(JSONObject response) {
//                        try {
//                            String currentName = response.getString("name");
//                            usernameEdt.setText(currentName);
//                            String currentEmail = response.getString("emailId");
//                            emailEdt.setText(currentEmail);
//                            int currentID = response.getInt("id");
//                            idEdt.setText(String.valueOf(currentID));
//                            String currentPassword = response.getString("password");
//                            passwordEdt.setText(currentPassword);
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                        makeVisible();
//                    }
//
//                    @Override
//                    public void onError(String message) {
//                        Toast.makeText(UserManageActivity.this, "Could not process data update.", Toast.LENGTH_SHORT).show();
//                    }
//                });
                String url = String.format("http://coms-3090-017.class.las.iastate.edu:8080/users/%s", idEdt.getText().toString());
                JsonObjectRequest update = new JsonObjectRequest(Request.Method.PUT,
                        url,
                        updatedUserData, // the updated book to updated
                        response -> {
                            try {
                                String currentName = response.getString("name");
                                usernameEdt.setText(currentName);
                                String currentEmail = response.getString("emailId");
                                emailEdt.setText(currentEmail);
                                int currentID = response.getInt("id");
                                idEdt.setText(String.valueOf(currentID));
//                                String currentPassword = response.getString("password");
//                                passwordEdt.setText(currentPassword);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            Toast.makeText(UserManageActivity.this, "User Data Updated.", Toast.LENGTH_SHORT).show();
                        },


                        error -> {
                            Toast.makeText(UserManageActivity.this, "Could not process data update.", Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        // Define headers if needed
                        HashMap<String, String> headers = new HashMap<>();
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        // Define parameters if needed
                        Map<String, String> params = new HashMap<>();
                        // Example parameters (uncomment if needed)
                        // params.put("param1", "value1");
                        // params.put("param2", "value2");
                        return params;
                    }
                };

                // Adding request to the Volley request queue
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(update);
            }
        });

    }

    protected void makeVisible() {
        passwordInput.setVisibility(View.VISIBLE);
        idInput.setVisibility(View.VISIBLE);
        usernameInput.setVisibility(View.VISIBLE);
        emailInput.setVisibility(View.VISIBLE);
    }
}
