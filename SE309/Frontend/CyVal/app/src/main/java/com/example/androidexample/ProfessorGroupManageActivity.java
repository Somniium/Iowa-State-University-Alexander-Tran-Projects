package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Edwin Cepeda
 */
public class ProfessorGroupManageActivity extends AppCompatActivity {

    private TextView groupName, groupId, professorId;
    private Button accessButton, studentAddButton, studentRemoveButton, updateGroupButton, deleteGroupButton, returnButton;
    private EditText groupNameInput, groupClassInput, studentNameInput;
    private ListView studentsList;

    private int professorIdValue;
    private int userIdValue, queuedUserId;
    private String groupNameValue;
    private JSONObject groupData;

    private ArrayList<JSONObject> groupStudents = new ArrayList<JSONObject>();
    String[] groupStudentNames = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_group_manage); // Set the UI layout for the activity

        groupName = findViewById(R.id.group_name);
        groupId = findViewById(R.id.group_id);
        professorId = findViewById(R.id.professor_id);

        accessButton = findViewById(R.id.access_button);
        studentAddButton = findViewById(R.id.student_add_button);
        studentRemoveButton = findViewById(R.id.student_remove_button);
        updateGroupButton = findViewById(R.id.update_group_button);
        deleteGroupButton = findViewById(R.id.delete_group_button);
        returnButton = findViewById(R.id.return_button);

        groupNameInput = findViewById(R.id.group_name_input);
        groupClassInput = findViewById(R.id.group_class_input);
        studentNameInput = findViewById(R.id.student_name_input);

        studentsList = findViewById(R.id.students_list);

        //Get transferred data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            professorIdValue = extras.getInt("PROFESSOR_ID");
            userIdValue = extras.getInt("USER_ID");
            groupNameValue = extras.getString("GROUP_NAME");
        }

        //Get bundle information and update UI
        professorId.setText("Prof ID: " + professorIdValue);
        groupName.setText(groupNameValue);

        //Get group information and update UI
        ApiClient.get(ProfessorGroupManageActivity.this, "/group/name/" + groupNameValue, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String stringedResponse = response.toString();
                    groupData = new JSONObject(stringedResponse);
                    groupId.setText("Group ID: " + groupData.getInt("groupId"));
                    groupNameInput.setText(groupData.getString("name"));
                    groupClassInput.setText(groupData.getString("className"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorGroupManageActivity.this, "Could not get Group Data.", Toast.LENGTH_SHORT).show();
            }
        });

        updateStudentList();

        studentAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get user ID by username
                String endpoint = String.format("/users/name/%s", studentNameInput.getText().toString());
                ApiClient.get(ProfessorGroupManageActivity.this, endpoint, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            Log.d("Test:", String.valueOf(response.getInt("id")));
                            queuedUserId = response.getInt("id");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        finishAdd();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProfessorGroupManageActivity.this, "Could not add student.", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        studentRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get user ID by username
                String endpoint = String.format("/users/name/%s", studentNameInput.getText().toString());
                ApiClient.get(ProfessorGroupManageActivity.this, endpoint, new Api_Interface() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            Log.d("Test:", String.valueOf(response.getInt("id")));
                            queuedUserId = response.getInt("id");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        finishRemove();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProfessorGroupManageActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        updateGroupButton.setOnClickListener(new View.OnClickListener() {
            //TODO: WAITING ON BACKEND FIX FOR THIS ENDPOINT
            @Override
            public void onClick(View v) {
                try {
                    groupData.put("name", groupNameInput.getText().toString());
                    groupData.put("className", groupClassInput.getText().toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                String endpoint;
                try {
                    endpoint = "http://coms-3090-017.class.las.iastate.edu:8080/update-group/" + groupData.getInt("groupId");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.d("test", groupData.toString());

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.PUT,
                        endpoint, // API URL
                        groupData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(ProfessorGroupManageActivity.this, "Updated group.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Log the error details
                                Log.e("Volley Error", error.toString());
                                Toast.makeText(ProfessorGroupManageActivity.this, "Could Not Update.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                };

                // Adding request to the Volley request queue
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

//                ApiClient.put(ProfessorGroupManageActivity.this, endpoint, groupData, new Api_Interface() {
//                    @Override
//                    public void onSuccess(JSONObject response) {
//                        Toast.makeText(ProfessorGroupManageActivity.this, "Group Info Updated", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onError(String message) {
//                        Toast.makeText(ProfessorGroupManageActivity.this, message, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });

        deleteGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (groupData == null) {
                    Toast.makeText(ProfessorGroupManageActivity.this, "Group data not loaded yet.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String endpoint;
                try {
                    endpoint = "/delete-group/" + String.valueOf(groupData.getInt("groupId"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ApiClient.deleteString(ProfessorGroupManageActivity.this, endpoint, new Api_String_Interface() {
                    @Override
                    public void onSuccess(String response) {
                        Toast.makeText(ProfessorGroupManageActivity.this, "Group deleted.", Toast.LENGTH_SHORT).show();
                        returnToAdminDash();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ProfessorGroupManageActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToAdminDash();
            }
        });

        accessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfessorGroupManageActivity.this, ChatActivity.class);
                try {
                    intent.putExtra("GROUP_ID", groupData.getInt("groupId"));
                    intent.putExtra("USER_ID", userIdValue);
                    intent.putExtra("GROUP_NAME", groupData.getString("name"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                startActivity(intent);
            }
        });

    }

    protected void finishAdd() {
        //add user to group
        String endpoint;
        try {
            endpoint = String.format("/add-user-to-group/group/%d/user/%d", groupData.getInt("groupId"), queuedUserId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ApiClient.postString(ProfessorGroupManageActivity.this, endpoint, null, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ProfessorGroupManageActivity.this, "Student added.", Toast.LENGTH_SHORT);
                updateStudentList();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorGroupManageActivity.this, message, Toast.LENGTH_SHORT).show();
                updateStudentList();
            }
        });
    }

    protected void finishRemove() {
        //add user to group
        String endpoint;
        try {
            endpoint = String.format("/remove-user-from-group/group/%d/user/%d", groupData.getInt("groupId"), queuedUserId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ApiClient.deleteString(ProfessorGroupManageActivity.this, endpoint, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(ProfessorGroupManageActivity.this, "Student removed.", Toast.LENGTH_SHORT).show();
                updateStudentList();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorGroupManageActivity.this, message, Toast.LENGTH_SHORT).show();
                updateStudentList();
            }
        });
    }
    protected void updateStudentList() {

        //Reobtain groupData to refresh students
        ApiClient.get(ProfessorGroupManageActivity.this, "/group/name/" + groupNameValue, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String stringedResponse = response.toString();
                    groupData = new JSONObject(stringedResponse);
                    groupStudentNames = new String[groupData.getJSONArray("members").length()];
                    for (int i = 0; i < groupStudentNames.length; i++) {
                        try {
                            String singleString = groupData.getJSONArray("members").getJSONObject(i).toString();
                            JSONObject singleStudent = new JSONObject(singleString);
                            groupStudentNames[i] = singleStudent.getString("name");
                            Log.d("Test Strings", groupStudentNames[i]);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfessorGroupManageActivity.this, android.R.layout.simple_list_item_1, groupStudentNames);
                    studentsList.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProfessorGroupManageActivity.this, "Could not get Group Data.", Toast.LENGTH_SHORT).show();
            }
        });



//        JsonArrayRequest jsonArrReq = new JsonArrayRequest(
//                Request.Method.GET, // HTTP method (GET request)
//                "http://coms-3090-017.class.las.iastate.edu:8080/get-professor-groups/" + professorID,
//                null, // Request body (null for GET request)
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(ProfessorPanelActivity.this, "Could not get groups.", Toast.LENGTH_SHORT).show();
//                    }
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                // Headers for the request (if needed)
//                Map<String, String> headers = new HashMap<>();
//                // Example headers (uncomment if needed)
//                // headers.put("Authorization", "Bearer YOUR_ACCESS_TOKEN");
//                // headers.put("Content-Type", "application/json");
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() {
//                // Parameters for the request (if needed)
//                Map<String, String> params = new HashMap<>();
//                // Example parameters (uncomment if needed)
//                // params.put("param1", "value1");
//                // params.put("param2", "value2");
//                return params;
//            }
//        };
//
//        // Adding request to the Volley request queue
//        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrReq);
    }

    protected void returnToAdminDash() {
        Intent intent = new Intent(ProfessorGroupManageActivity.this, ProfessorPanelActivity.class);
        intent.putExtra("PROFESSOR_ID", professorIdValue);
        intent.putExtra("USER_ID", userIdValue);
        startActivity(intent);
    }

}
