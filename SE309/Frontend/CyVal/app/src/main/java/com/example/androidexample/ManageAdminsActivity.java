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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class ManageAdminsActivity extends AppCompatActivity {

    // Declare button variables
    private Button queryAddBtn, queryManageBtn, addBtn, promoteBtn, deleteBtn, returnBtn;
    private ImageButton searchBtn;

    // Declare layout variables
    private LinearLayout queryInterface, addInterface, manageInterface;

    //Declare edit variables
    private EditText searchEdt, addEdt;

    //Declare textview variables
    private TextView adminIdText, userIdText, masterText;
    private int adminID = -1;
    private int userID = -1;
    private boolean isMaster;
    private int yourUserID = -1;
    private int yourAdminID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_admins); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        queryAddBtn = findViewById(R.id.queryAddBtn);
        queryManageBtn = findViewById(R.id.queryManageBtn);
        addBtn = findViewById(R.id.addBtn);
        searchBtn = findViewById(R.id.searchBtn);
        promoteBtn = findViewById(R.id.promoteBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        returnBtn = findViewById(R.id.returnBtn);

        queryInterface = findViewById(R.id.queryInterface);
        addInterface = findViewById(R.id.addInterface);
        manageInterface = findViewById(R.id.manageInterface);

        searchEdt = findViewById(R.id.userEdt);
        addEdt = findViewById(R.id.userAddEdt);

        adminIdText = findViewById(R.id.adminIdText);
        userIdText = findViewById(R.id.userIdText);
        masterText = findViewById(R.id.masterText);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            yourAdminID = extras.getInt("ADMIN_ID", -1);
            yourUserID = extras.getInt("USER_ID", -1);
        }

        queryAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                addInterface.setVisibility(View.VISIBLE);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = Integer.parseInt(addEdt.getText().toString());
                ApiClient.get(ManageAdminsActivity.this, "/admin/user/" + userID, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(ManageAdminsActivity.this, "Admin already exists for this user.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        addAdmin(userID);
                    }
                });
            }
        });

        queryManageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryInterface.setVisibility(View.GONE);
                manageInterface.setVisibility(View.VISIBLE);
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApiClient.get(ManageAdminsActivity.this, "/admin/id/" + Integer.parseInt(searchEdt.getText().toString()), new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            adminID = response.getInt("id");
                            isMaster = response.getBoolean("master");
                            getUserIdAndUpdateUI(adminID);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ManageAdminsActivity.this, "Not a valid admin.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMaster) {
                    Toast.makeText(ManageAdminsActivity.this, "You cannot delete another master admin.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiClient.delete(ManageAdminsActivity.this, "/remove-admin/id/" + adminID, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Toast.makeText(ManageAdminsActivity.this, "Admin (ID: " + adminID + ") deleted.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ManageAdminsActivity.this, AdminPanelActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        Log.d("oh my god just work for the love of god", String.valueOf(adminID));
                        Toast.makeText(ManageAdminsActivity.this, "Could not find admin.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageAdminsActivity.this, AdminPanelActivity.class);
                intent.putExtra("ADMIN_ID", yourAdminID);
                intent.putExtra("USER_ID", yourUserID);
                startActivity(intent);
            }
        });

        promoteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                JSONObject updatedAdmin = new JSONObject();
//                try {
//                    updatedAdmin.put("id", adminID);
//                    updatedAdmin.put("is_master", false);
//                    updatedAdmin.put("user_id", userID);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                String url = String.format("http://coms-3090-017.class.las.iastate.edu:8080/admin-make-master/%d", adminID);
                JsonObjectRequest update = new JsonObjectRequest(Request.Method.PUT,
                        url,
                        null,
                        response -> {
                            Toast.makeText(ManageAdminsActivity.this, "Admin (ID: " + adminID + ") promoted to master.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ManageAdminsActivity.this, AdminPanelActivity.class);
                            intent.putExtra("ADMIN_ID", yourAdminID);
                            intent.putExtra("USER_ID", yourUserID);
                            startActivity(intent);
                        },

                        error -> {
                            Toast.makeText(ManageAdminsActivity.this, "Could not promote Admin.", Toast.LENGTH_SHORT).show();
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

    protected void addAdmin(int userID) {
        ApiClient.post(ManageAdminsActivity.this, "/admin/" + userID, null, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(ManageAdminsActivity.this, "Admin created for User ID " + userID + ".", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                addAdmin(userID);
            }
        });
    }

    protected void getUserIdAndUpdateUI(int adminsID) {
        ApiClient.get(ManageAdminsActivity.this, "/admin/get-user/" + adminsID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    userID = response.getInt("id");
                    adminIdText.setText("Admin ID: " + adminID);
                    userIdText.setText("User ID: " + userID);
                    if (isMaster) {
                        masterText.setText("Master: true");
                        promoteBtn.setVisibility(View.INVISIBLE);
                    } else {
                        masterText.setText("Master: false");
                        promoteBtn.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
//                addAdmin(userID);
            }
        });
    }
}
