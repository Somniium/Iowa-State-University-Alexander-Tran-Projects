package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Edwin Cepeda
 */
public class AdminPanelActivity extends AppCompatActivity implements View.OnClickListener {

    // Comment to test pipeline

    //Declare adminID and userID variables
    int adminID = -1;
    int userID = -1;
    // Declare button variables
    private Button userBtn, professorBtn, adminBtn, postBtn, commentsRepliesBtn, groupBtn, albumBtn, artistBtn, bookBtn, gameBtn, movieBtn, returnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        userBtn = findViewById(R.id.btnUserManage);
        professorBtn = findViewById(R.id.btnProfessorManage);
        adminBtn = findViewById(R.id.btnAdminManage);
        postBtn = findViewById(R.id.btnReviewManage);
        commentsRepliesBtn = findViewById(R.id.btnCommentsRepliesManage);
        groupBtn = findViewById(R.id.btnGroupsManage);
        albumBtn = findViewById(R.id.btnAlbumManage);
        artistBtn = findViewById(R.id.btnArtistManage);
        bookBtn = findViewById(R.id.btnBookManage);
        gameBtn = findViewById(R.id.btnGameManage);
        movieBtn = findViewById(R.id.btnMovieManage);
        returnBtn = findViewById(R.id.btnReturn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            userID = extras.getInt("USER_ID", -1);
        }

        if (adminID == -1) {
            Toast.makeText(AdminPanelActivity.this, "MISSING ADMIN ID", Toast.LENGTH_LONG).show();
        }
        if (userID == -1) {
            Toast.makeText(AdminPanelActivity.this, "MISSING USER ID", Toast.LENGTH_LONG).show();
        }

        userBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, UserManageActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        professorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageProfessorsActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        adminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adminID == -1) {
                    Toast.makeText(AdminPanelActivity.this, "Cannot process admin information.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiClient.get(AdminPanelActivity.this, "/admin/id/" + adminID, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        boolean isMaster;
                        try {
                            isMaster = response.getBoolean("master");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        if (isMaster) {
                            Intent intent = new Intent(AdminPanelActivity.this, ManageAdminsActivity.class);
                            intent.putExtra("ADMIN_ID", adminID);
                            intent.putExtra("USER_ID", userID);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AdminPanelActivity.this, "You need master-level permissions to manage Admins.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(AdminPanelActivity.this, "You aren't even an admin... how are you here?", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManagePostsActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("POST_TYPE", "POSTS");
                startActivity(intent);
            }
        });

        commentsRepliesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManagePostsActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("POST_TYPE", "COMMENTS");
                startActivity(intent);
            }
        });

        groupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageGroupsActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageMediaActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("MEDIA_TYPE", "games");
                startActivity(intent);
            }
        });

        albumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageMediaActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("MEDIA_TYPE", "albums");
                startActivity(intent);
            }
        });

        artistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageMediaActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("MEDIA_TYPE", "artists");
                startActivity(intent);
            }
        });

        bookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageMediaActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("MEDIA_TYPE", "books");
                startActivity(intent);
            }
        });

        movieBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, ManageMediaActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                intent.putExtra("MEDIA_TYPE", "moviesOrShows");
                startActivity(intent);
            }
        });


        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPanelActivity.this, MainFeedActivity.class);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId(); // Get the ID of the clicked button
        String userType;
        Intent intent;
        // Check which button was clicked and start the corresponding activity
        if (id == R.id.btnUserManage) {
            intent = new Intent(AdminPanelActivity.this, UserManageActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.btnJsonObjRequest) {
//            userType = "ADMIN";
//            intent = new Intent(AdminPanelActivity.this, LoginActivity.class);
//            intent.putExtra("USERTYPE", userType);
//            startActivity(intent);
      /*  } else if (id == R.id.btnJsonArrRequest) {
            userType = "GUEST";
            intent = new Intent(MainActivity.this, AlbumActivity.class);
            intent.putExtra("USERTYPE", userType);
            startActivity(intent);

       */
//        } else if (id == R.id.btnImageRequest) {
//            startActivity(new Intent(AdminPanelActivity.this, SignupActivity.class));
//        }
    }
}
