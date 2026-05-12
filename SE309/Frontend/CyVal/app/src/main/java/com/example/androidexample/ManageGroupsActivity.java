package com.example.androidexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Edwin Cepeda
 */
public class ManageGroupsActivity extends AppCompatActivity {

    // Declare button variables
    private Button deleteBtn, returnBtn;
    private ImageButton searchBtn;

    //Declare edit variables
    private EditText findPostEdt;

    //Declare textview variables
    private TextView managePostHeader, groupNameText, groupIdText, professorIdText, groupClassText;

    private JSONObject groupData;
    private int groupID = -1;
    private int adminID = -1;
    private int userID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups); // Set the UI layout for the activity

        managePostHeader = findViewById(R.id.manage_group_header);
        findPostEdt = findViewById(R.id.findGroupEdt);
        searchBtn = findViewById(R.id.searchBtn);
        groupNameText = findViewById(R.id.groupNameText);
        groupIdText = findViewById(R.id.groupIdText);
        professorIdText = findViewById(R.id.professorIdText);
        groupClassText = findViewById(R.id.groupClassText);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            userID = extras.getInt("USER_ID", -1);
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGroupInDB();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGroupInDB();
            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageGroupsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

    }

    private void searchGroupInDB() {

        ApiClient.get(ManageGroupsActivity.this, "/group/id/" + findPostEdt.getText().toString(), new Api_Interface() {

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("Response test", response.toString());
                    String mediaDataString = response.toString();
                    groupData = new JSONObject(mediaDataString);
                    groupID = response.getInt("groupId");
                    groupNameText.setText("Group Name: " + groupData.getString("name"));
                    groupIdText.setText("Group ID: " + groupData.getInt("groupId"));
                    professorIdText.setText("Professor ID: " + groupData.getJSONObject("professor").getInt("id"));
                    groupClassText.setText("Class: " + groupData.getString("className"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Log.e("search error", message);
                Toast.makeText(ManageGroupsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteGroupInDB() {

        ApiClient.deleteString(ManageGroupsActivity.this, "/delete-group/" + groupID, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ManageGroupsActivity.this, response, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageGroupsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageGroupsActivity.this, "Could not delete post/comment.", Toast.LENGTH_SHORT).show();
                Log.e("Delete error", message);
            }
        });

    }

}
