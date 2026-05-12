package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class SettingsActivity extends AppCompatActivity {

    private TextView settingsHeader;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private Button changePasswordBtn;
    private Button deleteAccountBtn;
    private TextView backToProfileText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get the USER_ID passed from UserProfileActivity
        int userId = getIntent().getIntExtra("USER_ID", -1);

        // Initialize UI Elements
        settingsHeader = findViewById(R.id.settings_header);
        oldPasswordEditText = findViewById(R.id.settings_old_password_edit);
        newPasswordEditText = findViewById(R.id.settings_new_password_edit);
        changePasswordBtn = findViewById(R.id.btn_change_password);
        deleteAccountBtn = findViewById(R.id.btn_delete_account);
        backToProfileText = findViewById(R.id.back_to_profile_text);

        // Set Text Programmatically
        settingsHeader.setText("Change Password");
        oldPasswordEditText.setHint("Old Password");
        newPasswordEditText.setHint("New Password");
        changePasswordBtn.setText("Update Password");
        backToProfileText.setText("Back to Profile");

        // Back Button logic
        backToProfileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Closes this screen to go back to the profile
            }
        });

        // Delete Button logic
        deleteAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DeleteAccountActivity.class);
                startActivity(intent);
            }
        });

        // Change Password Logic
        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId == -1) {
                    Toast.makeText(SettingsActivity.this, "Error: Invalid User ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                String oldPasswordInput = oldPasswordEditText.getText().toString();
                String newPasswordInput = newPasswordEditText.getText().toString();

                if (oldPasswordInput.isEmpty() || newPasswordInput.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Please fill out both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Fetch current user data from the existing backend GET route
                String getUrl = "http://coms-3090-017.class.las.iastate.edu:8080/users/id/" + userId;

                JsonObjectRequest getRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        getUrl,
                        null,
                        response -> {
                            try {
                                // Extract current data
                                String currentName = response.getString("name");
                                String currentEmail = response.getString("emailId");
                                boolean currentActive = response.getBoolean("active");
                                String currentDbPassword = response.getString("password");

                                //Password Verification
                                if (!currentDbPassword.equals(oldPasswordInput)) {
                                    Toast.makeText(SettingsActivity.this, "Incorrect old password!", Toast.LENGTH_SHORT).show();
                                    return; // Stop the process here
                                }

                                //If it matches, package the old data with the NEW password
                                updatePasswordInDatabase(userId, currentName, currentEmail, currentActive, newPasswordInput);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SettingsActivity.this, "Error reading user data", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Toast.makeText(SettingsActivity.this, "Network Error: Could not fetch user", Toast.LENGTH_LONG).show()
                );

                VolleySingleton.getInstance(SettingsActivity.this).addToRequestQueue(getRequest);
            }
        });
    }

    //handle the PUT request using the generic update route
    private void updatePasswordInDatabase(int userId, String name, String emailId, boolean active, String newPassword) {
        String putUrl = "http://coms-3090-017.class.las.iastate.edu:8080/users/" + userId;

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", userId);
            requestBody.put("name", name);
            requestBody.put("emailId", emailId);
            requestBody.put("active", active);
            requestBody.put("password", newPassword); // Overwriting with the new password
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                putUrl,
                requestBody,
                response -> {
                    // SUCCESS: Route to Login and clear the stack
                    Toast.makeText(SettingsActivity.this, "Password Updated! Please log in again.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    // ERROR
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String serverError = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        Toast.makeText(SettingsActivity.this, "Error: " + serverError, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Update Error: Could not reach server", Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }
}