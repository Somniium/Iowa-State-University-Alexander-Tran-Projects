package com.example.androidexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Edwin Cepeda
 */
public class ManagePostsActivity extends AppCompatActivity {

    // Declare button variables
    private Button deleteBtn, returnBtn;
    private ImageButton searchBtn;

    //Declare edit variables
    private EditText findPostEdt;

    //Declare textview variables
    private TextView managePostHeader, authorNameText, authorIdText, postMediaTitleText, postTitleText, postRatingText, postReplyingToText, postBodyText;

    private JSONObject postData;
    private int postID = -1;
    private int adminID = -1;
    private int userID = -1;
    private String postType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_posts); // Set the UI layout for the activity

        managePostHeader = findViewById(R.id.manage_post_header);
        findPostEdt = findViewById(R.id.findPostEdt);
        searchBtn = findViewById(R.id.searchBtn);
        authorNameText = findViewById(R.id.authorNameText);
        authorIdText = findViewById(R.id.authorIdText);
        postMediaTitleText = findViewById(R.id.postMediaTitleText);
        postTitleText = findViewById(R.id.postTitleText);
        postRatingText = findViewById(R.id.postRatingText);
        postReplyingToText = findViewById(R.id.postReplyingToText);
        postBodyText = findViewById(R.id.postBodyText);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);
        returnBtn.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            userID = extras.getInt("USER_ID", -1);
            postType = extras.getString("POST_TYPE", null);
        }

        updateElements();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPostInDB();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePostInDB();
            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagePostsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

    }

    private void updateElements() {
        switch (postType){
            case "POSTS":
                postReplyingToText.setVisibility(View.GONE);
                break;
            case "COMMENTS":
                findPostEdt.setHint("Get Comment by Comment ID");
                managePostHeader.setText("Manage Comments");
                postTitleText.setVisibility(View.GONE);
                postMediaTitleText.setVisibility(View.GONE);
                postRatingText.setVisibility(View.GONE);
                break;
            default:
                Toast.makeText(ManagePostsActivity.this, "ERROR: NO POST TYPE", Toast.LENGTH_LONG).show();
        }


    }

    private void searchPostInDB() {
        String searchEndpoint = "";
        switch (postType){
            case "POSTS":
                searchEndpoint = "/posts/" + findPostEdt.getText().toString();
                break;
            case "COMMENTS":
                searchEndpoint = "/comments/" + findPostEdt.getText().toString();
                break;
            default:
                Toast.makeText(ManagePostsActivity.this, "ERROR: NO POST TYPE", Toast.LENGTH_LONG).show();
        }
        Log.d("test endpoint", searchEndpoint);

        ApiClient.get(ManagePostsActivity.this, searchEndpoint, new Api_Interface() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("Response test", response.toString());
                    String mediaDataString = response.toString();
                    postData = new JSONObject(mediaDataString);
                    switch (postType){
                        case "POSTS":
                            postID = response.getInt("id");
                            authorNameText.setText("Author Name: " + postData.getString("authorName"));
                            authorIdText.setText("Author ID: " + postData.getInt("authorId"));
                            postMediaTitleText.setText("Media Title: " + postData.getString("mediaTitle"));
                            postTitleText.setText("Post Title: " + "(" + postData.getString("mediaType") + ")" + postData.getString("caption"));
                            postRatingText.setText("Post Rating: " + postData.getInt("rating"));
                            getPostBodyText(postData.getInt("reviewId"));
                            break;
                        case "COMMENTS":
                            postID = response.getInt("id");
                            authorNameText.setText("Author Name: " + postData.getString("authorName"));
                            authorIdText.setText("Author ID: " + postData.getString("authorId"));
                            postReplyingToText.setText("Replying To: " + "(POST ID: " + postData.getInt("postId") + ")");
                            postBodyText.setText(postData.getString("body"));
                            break;
                        default:
                            Toast.makeText(ManagePostsActivity.this, "ERROR: NO MEDIA TYPE", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Log.e("search error", message);
                Toast.makeText(ManagePostsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getPostBodyText(int reviewId) {
        String getBodyEndpoint = "/reviews/" + reviewId;

        ApiClient.get(ManagePostsActivity.this, getBodyEndpoint, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String body = response.getString("body");
                    postBodyText.setText(body);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManagePostsActivity.this, "Could not get post body", Toast.LENGTH_LONG).show();
            }

            ;

        });
    }

    private void deletePostInDB() {
        String deleteEndpoint = "";
        switch (postType){
            case "POSTS":
                deleteEndpoint = "/posts/" + postID + "?requesterId=" + userID;
                break;
            case "COMMENTS":
                deleteEndpoint = "/comments/" + postID +  "?requesterId=" + userID;
                break;
            default:
                Toast.makeText(ManagePostsActivity.this, "ERROR: NO POST TYPE", Toast.LENGTH_LONG).show();
        }

        ApiClient.deleteString(ManagePostsActivity.this, deleteEndpoint, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ManagePostsActivity.this, response, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManagePostsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManagePostsActivity.this, "Could not delete post/comment.", Toast.LENGTH_SHORT).show();
                Log.e("Delete error", message);
            }
        });

    }

}
