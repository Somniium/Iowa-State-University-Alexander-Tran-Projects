package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfileActivity extends AppCompatActivity {

    // Profile Data variables
    private TextView welcomeHeader;
    private TextView profileNameText;
    private TextView profileEmailText;
    private TextView profileSeparatorText;
    private TextView profileIdText;
    private TextView profileMajorText;
    private TextView profileBioText;

    private int currentProfileId = -1;

    // Interactive UI variables
    private Button editProfileBtn;
    private CardView settingsCard;
    private TextView settingsText;
    private CardView logoutCard;
    private TextView logoutText;

    private CardView booksCard;
    private TextView booksText;
    private CardView albumsCard;
    private TextView albumsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Profile Data Text
        welcomeHeader = findViewById(R.id.welcome_header);
        profileNameText = findViewById(R.id.profile_name);
        profileEmailText = findViewById(R.id.profile_email);
        profileSeparatorText = findViewById(R.id.profile_separator);
        profileIdText = findViewById(R.id.profile_id);
        profileMajorText = findViewById(R.id.profile_major);
        profileBioText = findViewById(R.id.profile_bio);

        // Initialize Interactive UI Elements
        editProfileBtn = findViewById(R.id.btn_edit_profile);

        settingsCard = findViewById(R.id.card_settings);
        settingsText = findViewById(R.id.text_settings);

        logoutCard = findViewById(R.id.card_logout);
        logoutText = findViewById(R.id.text_logout);

        booksCard = findViewById(R.id.card_books);
        booksText = findViewById(R.id.text_books);

        albumsCard = findViewById(R.id.card_albums);
        albumsText = findViewById(R.id.text_albums);

        // Set all static UI text here in Java
        welcomeHeader.setText("Loading...");
        profileNameText.setText("Loading...");
        profileEmailText.setText("Loading...");
        profileSeparatorText.setText("  •  ");
        profileIdText.setText("Loading...");

        editProfileBtn.setText("View Profile");
        settingsText.setText("⚙ Account Settings");
        logoutText.setText("➜]  Log Out");

        booksText.setText("Friends");
        albumsText.setText("Following");

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getIntent().getIntExtra("USER_ID", -1);
                if (userId != -1) {
                    Intent intent = new Intent(UserProfileActivity.this, MainFeedActivity.class);
                    intent.putExtra("USER_ID", userId);
                    intent.putExtra("openViewProfile", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }
        });

        settingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Grab the current user's ID
                int userId = getIntent().getIntExtra("USER_ID", -1);

                if (userId != -1) {
                    // Route to the SettingsActivity and pass the ID along
                    Intent intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(UserProfileActivity.this, "Error: Cannot open settings, User ID missing.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserProfileActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                // Route to MainActivity
                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);

                // Clear the activity stack so the user can't press "Back" to return to the profile
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish();
            }
        });

        booksCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getIntent().getIntExtra("USER_ID", -1);
                Intent intent = new Intent(UserProfileActivity.this, MainFeedActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("openFriends", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        albumsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getIntent().getIntExtra("USER_ID", -1);
                Intent intent = new Intent(UserProfileActivity.this, MainFeedActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("openFollowing", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        // Get the USER_ID passed from LoginActivity
        int userId = getIntent().getIntExtra("USER_ID", -1);

        if (userId != -1) {
            fetchUserProfile(userId);
            fetchProfileDetails(userId);
        } else {
            Toast.makeText(this, "Error: User ID missing", Toast.LENGTH_SHORT).show();
            welcomeHeader.setText("Error loading profile");
        }
    }

    private void fetchProfileDetails(int userId) {
        ApiClient.get(this, "/profile/userId/" + userId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    currentProfileId = response.getInt("profileId");

                    String major = response.optString("major", "");
                    String bio = response.optString("bio", "");

                    if (!major.isEmpty()) {
                        profileMajorText.setText(major);
                        profileMajorText.setVisibility(View.VISIBLE);
                    }
                    if (!bio.isEmpty()) {
                        profileBioText.setText(bio);
                        profileBioText.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void fetchUserProfile(int userId) {
        ApiClient.get(this, "/users/id/" + userId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String name = response.getString("name");
                    String email = response.getString("emailId");
                    int id = response.getInt("id");

                    welcomeHeader.setText(name + "'s Profile");
                    profileNameText.setText(name);
                    profileEmailText.setText(email);
                    profileIdText.setText("ID: " + id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(UserProfileActivity.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UserProfileActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                welcomeHeader.setText("Profile Unavailable");
            }
        });
    }
}