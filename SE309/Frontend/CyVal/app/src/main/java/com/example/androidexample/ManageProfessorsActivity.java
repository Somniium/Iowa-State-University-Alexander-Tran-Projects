package com.example.androidexample;

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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class ManageProfessorsActivity extends AppCompatActivity {

    // Declare button variables
    private Button queryAddBtn, queryManageBtn, addBtn, addClassBtn, removeClassBtn, deleteBtn, returnBtn;
    private ImageButton searchBtn;

    // Declare layout variables
    private LinearLayout queryInterface, addInterface, manageInterface;

    //Declare edit variables
    private EditText searchEdt, addEdt, addClassEdt;

    //Declare textview variables
    private TextView professorIdText, userIdText, classesListText;
    private int professorID = -1;
    private int userID = -1;

    private int adminID = -1;
    private int adminUserID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_professors); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        queryAddBtn = findViewById(R.id.queryAddBtn);
        queryManageBtn = findViewById(R.id.queryManageBtn);
        addBtn = findViewById(R.id.addBtn);
        searchBtn = findViewById(R.id.searchBtn);
        addClassBtn = findViewById(R.id.addClassBtn);
        removeClassBtn = findViewById(R.id.removeClassBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);

        queryInterface = findViewById(R.id.queryInterface);
        addInterface = findViewById(R.id.addInterface);
        manageInterface = findViewById(R.id.manageInterface);

        searchEdt = findViewById(R.id.userEdt);
        addEdt = findViewById(R.id.userAddEdt);
        addClassEdt = findViewById(R.id.classesAddEdt);

        professorIdText = findViewById(R.id.professorIdText);
        userIdText = findViewById(R.id.userIdText);
        classesListText = findViewById(R.id.classesText);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            adminID = extras.getInt("ADMIN_ID", -1);
            adminUserID = extras.getInt("USER_ID", -1);
        }

        queryAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                addInterface.setVisibility(View.VISIBLE);
            }
        });

        queryManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                manageInterface.setVisibility(View.VISIBLE);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = Integer.parseInt(addEdt.getText().toString());
                ApiClient.get(ManageProfessorsActivity.this, "/professor/user/" + userID, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(ManageProfessorsActivity.this, "Professor already exists for this user.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        addProfessor(userID);
                    }
                });
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiClient.get(ManageProfessorsActivity.this, "/professor/id/" + Integer.parseInt(searchEdt.getText().toString()), new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            professorID = response.getInt("id");
                            getUserID(professorID);
                            professorIdText.setText("Professor ID: " + professorID);
                            if (response.getJSONArray("classes") != null) {
                                classesListText.setText("Classes: " + response.getJSONArray("classes"));
                            } else {
                                classesListText.setText("Classes: None");
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ManageProfessorsActivity.this, "Not a valid professor.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest stringRequest = new StringRequest(
                        Request.Method.DELETE, // HTTP method (GET request)
                        String.format("http://coms-3090-017.class.las.iastate.edu:8080/professor/remove-professor/id/%d", professorID), // API URL
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(ManageProfessorsActivity.this, "Professor (ID: " + professorID + ") deleted.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ManageProfessorsActivity.this, AdminPanelActivity.class);
                                intent.putExtra("ADMIN_ID", adminID);
                                intent.putExtra("USER_ID", adminUserID);
                                startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(ManageProfessorsActivity.this, "Could not find professor.", Toast.LENGTH_SHORT).show();
                            }
                        }
                ) {
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
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
//                ApiClient.delete(ManageProfessorsActivity.this, "/professor/remove-professor/id/" + professorID, new Api_Interface() {
//                    @Override
//                    public void onSuccess(JSONObject response) {
//                        Toast.makeText(ManageProfessorsActivity.this, "Professor (ID: " + professorID + ") deleted.", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(ManageProfessorsActivity.this, AdminPanelActivity.class);
//                        startActivity(intent);
//                    }
//
//                    @Override
//                    public void onError(String message) {
//                        Log.d("oh my god just work for the love of god", String.valueOf(professorID));
//                        Toast.makeText(ManageProfessorsActivity.this, "Could not find professor.", Toast.LENGTH_SHORT).show();
//                    }
//                });

            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageProfessorsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", adminID);
                intent.putExtra("USER_ID", adminUserID);
                startActivity(intent);
            }
        });

        addClassBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = String.format("http://coms-3090-017.class.las.iastate.edu:8080/professor/add-class/%d/%s", professorID, addClassEdt.getText().toString());
                JsonObjectRequest update = new JsonObjectRequest(Request.Method.PUT,
                        url,
                        null,
                        response -> {
                            updateClassesUI();
                            Toast.makeText(ManageProfessorsActivity.this, "Class " + addClassEdt.getText().toString() + "added for Professor " + professorID + ".", Toast.LENGTH_SHORT).show();
                        },

                        error -> {
                            Toast.makeText(ManageProfessorsActivity.this, "Could not add class.", Toast.LENGTH_SHORT).show();
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

        removeClassBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = String.format("http://coms-3090-017.class.las.iastate.edu:8080/professor/remove-class/%d/%s", professorID, addClassEdt.getText().toString());
                JsonObjectRequest update = new JsonObjectRequest(Request.Method.PUT,
                        url,
                        null,
                        response -> {
                            updateClassesUI();
                            Toast.makeText(ManageProfessorsActivity.this, "Class " + addClassEdt.getText().toString() + "removed for Professor " + professorID + ".", Toast.LENGTH_SHORT).show();
                        },

                        error -> {
                            Toast.makeText(ManageProfessorsActivity.this, "Could not find class " + addClassEdt.getText().toString() + ".", Toast.LENGTH_SHORT).show();
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

    protected void addProfessor(int userID) {
        ApiClient.post(ManageProfessorsActivity.this, "/professor/" + userID, null, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(ManageProfessorsActivity.this, "Professor created for User ID " + userID + ".", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageProfessorsActivity.this, "Professor not created.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void getUserID(int professorID) {
        ApiClient.get(ManageProfessorsActivity.this, "/professor/get-user/" + professorID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    userID = response.getInt("id");
                    userIdText.setText("User ID: " + userID);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageProfessorsActivity.this, "Couldn't get ID.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void updateClassesUI() {
        ApiClient.get(ManageProfessorsActivity.this, "/professor/id/" + Integer.parseInt(searchEdt.getText().toString()), new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getJSONArray("classes") != null) {
                        classesListText.setText("Classes: " + response.getJSONArray("classes"));
                    } else {
                        classesListText.setText("Classes: None");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ManageProfessorsActivity.this, "Not a valid professor.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
