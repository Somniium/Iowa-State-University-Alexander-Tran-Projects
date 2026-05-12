package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class ProfessorPanelActivity extends AppCompatActivity {

    private TextView usernameText, professorIdText, userIdText, classesText;
    private EditText nameEdit, classEdit;
    private Button createButton, returnButton;
    private ListView groupList;
    private JSONObject professorData;
    private ArrayList<JSONObject> professorGroups = new ArrayList<JSONObject>();
    String[] groupNames = {};

    private int userID;
    private int professorID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_panel);

        //Initalize all elements
        usernameText = findViewById(R.id.username);
        professorIdText = findViewById(R.id.professor_id);
        userIdText = findViewById(R.id.user_id);
        classesText = findViewById(R.id.classes_text);
        nameEdit = findViewById(R.id.nameInput);
        classEdit = findViewById(R.id.classInput);
        createButton = findViewById(R.id.createButton);
        returnButton = findViewById(R.id.returnButton);
        groupList = findViewById(R.id.groupList);

        //Unpackage bundle
        Bundle extras = getIntent().getExtras();
        professorID = -1;
        if (extras != null) {
            professorID = extras.getInt("PROFESSOR_ID");
            userID = extras.getInt("USER_ID");
        }

        //Get professor information and update UI
        ApiClient.get(ProfessorPanelActivity.this, "/professor/id/" + professorID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String stringedResponse = response.toString();
                    professorData = new JSONObject(stringedResponse);
                    professorIdText.setText("Professor ID: " + professorData.getInt("id"));
                    String classesString = professorData.getJSONArray("classes").toString();
                    classesText.setText("Classes: " + classesString);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorPanelActivity.this, "Could not process Professor.", Toast.LENGTH_SHORT).show();
            }
        });

        //Get user information and update UI
        ApiClient.get(ProfessorPanelActivity.this, "/users/id/" + userID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    userID = response.getInt("id");
                    usernameText.setText(response.getString("name"));
                    userIdText.setText("User ID: " + userID);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorPanelActivity.this, "Could not process User.", Toast.LENGTH_SHORT).show();
            }
        });

        updateGroupsList();

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String group = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(ProfessorPanelActivity.this, "Going to " + group, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfessorPanelActivity.this, ProfessorGroupManageActivity.class);
                try {
                    intent.putExtra("PROFESSOR_ID", professorData.getInt("id"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                intent.putExtra("USER_ID", userID);
                intent.putExtra("GROUP_NAME", group);
                startActivity(intent);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String endpoint;
                try {
                    endpoint = String.format("/group/name/%s/professor/%d/class/%s", nameEdit.getText().toString(), professorData.getInt("id"), classEdit.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ApiClient.post(ProfessorPanelActivity.this, endpoint, null, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        updateGroupsList();
                        Toast.makeText(ProfessorPanelActivity.this, "Group made.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProfessorPanelActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorPanelActivity.this, MainFeedActivity.class);
                intent.putExtra("USER_ID", userID);
                startActivity(intent);
            }
        });

    }

    private void updateGroupsList() {
        JsonArrayRequest jsonArrReq = new JsonArrayRequest(
                Request.Method.GET, // HTTP method (GET request)
                "http://coms-3090-017.class.las.iastate.edu:8080/get-professor-groups/" + professorID,
                null, // Request body (null for GET request)
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        groupNames = new String[response.length()];
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                String singleString = response.getJSONObject(i).toString();
                                JSONObject singleGroup = new JSONObject(singleString);
                                professorGroups.add(singleGroup);
                                groupNames[i] = professorGroups.get(i).getString("name");
                                Log.d("Test Strings", groupNames[i]);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfessorPanelActivity.this, android.R.layout.simple_list_item_1, groupNames);
                            groupList.setAdapter(adapter);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ProfessorPanelActivity.this, "Could not get groups.", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Headers for the request (if needed)
                Map<String, String> headers = new HashMap<>();
                // Example headers (uncomment if needed)
                // headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN");
                // headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                // Parameters for the request (if needed)
                Map<String, String> params = new HashMap<>();
                // Example parameters (uncomment if needed)
                // params.put("param1", "value1");
                // params.put("param2", "value2");
                return params;
            }
        };

        // Adding request to the Volley request queue
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrReq);
    }
}