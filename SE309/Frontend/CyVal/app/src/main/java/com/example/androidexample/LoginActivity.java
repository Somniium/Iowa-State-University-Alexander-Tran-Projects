package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// Volley and JSON imports
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Edwin Cepeda and Cristian Alvarez
 */
public class LoginActivity extends AppCompatActivity {

    // Define UI variables
    private EditText usernameEditText, passwordEditText;
    private Button studentButton, professorButton, adminButton, loginButton, backButton;

    // Define TextView variables
    private TextView loginHeader, signupText,adminLoginText, forgotPasswordText , idLabel, passwordLabel;

    //Define LinearLayout variables;
    private LinearLayout usertypeQuery, loginDetails;

    //Define variable containing the type of login being attempted
    private String loginType;

    //Define variable containing the name of user
    private String verifiedUserName;

    //Define variable containing the user's ID. -1 by default until user credentials are verified.
    private int userID = -1;
    private Boolean validLogin = true;

    //Higher permission IDs. -1 by default, signifying there is no professor or admin object for the user.
    private int professorID = -1;
    private int adminID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        usertypeQuery = findViewById(R.id.usertype_query);
        loginDetails = findViewById(R.id.login_details);
        usernameEditText = findViewById(R.id.login_username_edit);
        passwordEditText = findViewById(R.id.login_password_edit);
        studentButton = findViewById(R.id.student_button);
        professorButton = findViewById(R.id.professor_button);
        adminButton = findViewById(R.id.admin_button);
        loginButton = findViewById(R.id.login_login_btn);
        backButton = findViewById(R.id.cancel_login_btn);

        loginHeader = findViewById(R.id.login_header);
        signupText = findViewById(R.id.login_signup);
        adminLoginText = findViewById(R.id.admin_login);
        forgotPasswordText = findViewById(R.id.forgot_password);
        idLabel = findViewById(R.id.id_label);
        passwordLabel = findViewById(R.id.password_label);

        // Set static text for the UI elements
        loginHeader.setText("Login");
        loginButton.setText("Login");
        signupText.setText("Don't have an account? Sign Up");
        adminLoginText.setText("Admin Login");
        forgotPasswordText.setText("Forgot Password?");
        if (idLabel != null) idLabel.setText("ISU ID");
        if (passwordLabel != null) passwordLabel.setText("Password");



        /* Click Listeners */

        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "student";
                usertypeQuery.setVisibility(View.GONE);
                loginDetails.setVisibility(View.VISIBLE);
            }
        });

        professorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "professor";
                usertypeQuery.setVisibility(View.GONE);
                loginDetails.setVisibility(View.VISIBLE);
            }
        });

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginType = "admin";
                usertypeQuery.setVisibility(View.GONE);
                loginDetails.setVisibility(View.VISIBLE);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                /* If the user's credentials check out, this will make verifiedUserName and userID
                no longer be null (as they are by default). A null check gates off the rest of this method
                from running. */
                verifyCredentials(username, password);

                //Null check and ID check. If verifyCredentials failed, nothing else will run.
                if (verifiedUserName == null || userID == -1) {
                    return;
                }

                /*Verification step for all user types. There are additional checks to ensure the account
                is of the type they are claiming to be.*/
                if (loginType.equals("admin")) {
                        checkAdmin();
                } else if (loginType.equals("professor")) {
                        checkProfessor();
                } else {
                    // TODO: If the user is claiming to simply be a student, we still need to check that this is not an elevated account.
                    checkAdmin();
                    checkProfessor();
                    if (validLogin) {
                        finishLogin();
                    }
                }



//               ApiClient.post(LoginActivity.this, "/users/login", loginRequest, new Api_Interface() {
//                   @Override
//                   public void onSuccess(JSONObject response) {
//                       DTOmodels.LoginResponse loginResponse = new Gson().fromJson(response.toString(), DTOmodels.LoginResponse.class);
//
//                        Toast.makeText(LoginActivity.this, "Welcome, " + loginResponse.name + "!", Toast.LENGTH_SHORT).show();
//
//                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
//                        intent.putExtra("USER_ID", loginResponse.id);
//                        startActivity(intent);
//                        finish();
//                   }
//                   @Override
//                   public void onError(String message) {
//                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
//                   }
//               });
            }
        });


        signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        adminLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Route to an AdminLoginActivity or handle admin flow
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Route to ForgotPasswordActivity
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * This method checks with the database to verify that the user's username and password line up.
     * @param username The username of the user to be verified.
     * @param password The password that the user provided, used to check login.
     */
    protected void verifyCredentials(String username, String password) {
        DTOmodels.LoginRequest loginRequest = new DTOmodels.LoginRequest(username, password);

        ApiClient.post(LoginActivity.this, "/users/login", loginRequest, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                DTOmodels.LoginResponse loginResponse = new Gson().fromJson(response.toString(), DTOmodels.LoginResponse.class);
                userID = loginResponse.id;
                verifiedUserName = loginResponse.name;
                /*Verification step for all user types. There are additional checks to ensure the account
                is of the type they are claiming to be.*/
                if (loginType.equals("admin")) {
                    checkAdmin();
                } else if (loginType.equals("professor")) {
                    checkProfessor();
                } else {
                    //Higher authority users can still log in as students.
                    finishLogin();
                }
            }
            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void checkAdmin() {
        ApiClient.get(LoginActivity.this, "/admin/user/" + userID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                if (loginType.equals("admin")) {
                    int adminID;
                    try {
                        adminID = response.getInt("id");
                        Log.d("Check: ", String.valueOf(adminID));
                        finishAdminLogin(adminID);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    finishLogin();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid user type.", Toast.LENGTH_SHORT).show();
                    validLogin = false;
                }
            }

            @Override
            public void onError(String message) {
                if (loginType.equals("admin")) {
                    Toast.makeText(LoginActivity.this, "Not a valid admin.", Toast.LENGTH_SHORT).show();
                } else if (loginType.equals("student")) {
                    validLogin = true;
                }
            }
        });
    }

    protected void checkProfessor() {
        ApiClient.get(LoginActivity.this, "/professor/user/" + userID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                if (loginType.equals("professor")) {
                    int professorID = -1;
                    try {
                        professorID = response.getInt("id");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    finishLogin();
//                    Intent intent = new Intent(LoginActivity.this, ProfessorPanelActivity.class);
//                    intent.putExtra("USER_ID", userID);
//                    intent.putExtra("PROFESSOR_ID", professorID);
//                    startActivity(intent);
//                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid user type.", Toast.LENGTH_SHORT).show();
                    validLogin = false;
                }
            }

            @Override
            public void onError(String message) {
                Log.d("Tag", loginType);
                if (loginType.equals("professor")) {
                    Toast.makeText(LoginActivity.this, "Not a valid professor.", Toast.LENGTH_SHORT).show();
                } else if (loginType.equals("student")) {
                    validLogin = true;
                }
            }
        });
    }

    /**
     * This simple helper method switches activities. Only here to avoid code duplication.
     */
    protected void finishLogin() {
        Toast.makeText(LoginActivity.this, "Welcome, " + verifiedUserName + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainFeedActivity.class);
        intent.putExtra("USER_ID", userID);
        intent.putExtra("ADMIN_ID", adminID);
        intent.putExtra("PROFESSOR_ID", professorID);
        startActivity(intent);
        finish();
    }

    /**
     * This simple helper method switches activities. Only here to avoid code duplication.
     */
    protected void finishAdminLogin(int adminID) {
        Toast.makeText(LoginActivity.this, "Welcome, " + verifiedUserName + "!", Toast.LENGTH_SHORT).show();

        if (loginType.equals("admin")) {
            Intent intent = new Intent(LoginActivity.this, AdminPanelActivity.class);
            intent.putExtra("USER_ID", userID);
            intent.putExtra("ADMIN_ID", adminID);
            startActivity(intent);
            finish();
        }
    }
}
