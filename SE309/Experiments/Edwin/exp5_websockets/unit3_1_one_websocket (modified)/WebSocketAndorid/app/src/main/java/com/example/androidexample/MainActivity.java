package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.java_websocket.handshake.ServerHandshake;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements WebSocketListener{

    private Button connectBtn;
    private EditText serverEtx, usernameEtx;
    private Spinner userTypeSelect;

    //Variable to hold the user type
    String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialize UI elements */
        connectBtn = findViewById(R.id.connectBtn);
        serverEtx = findViewById(R.id.serverEdt);
        usernameEtx = findViewById(R.id.unameEdt);
        Spinner userTypeSelect = findViewById(R.id.usertypeSelector);

        //Create a list of options for userType
        ArrayList<String> userOptions = new ArrayList<>();
        userOptions.add("Student");
        userOptions.add("Professor");
        userOptions.add("Administrator");
        //Turn all items in userOptions into spinner items using an adapter and set it as userTypeSelector's adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userOptions);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        userTypeSelect.setAdapter(adapter);

        userTypeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                /*This method uses the user's selection in mediaSelector to change the url for the request.*/

                //Get the user's selection
                mode = adapterView.getItemAtPosition(position).toString().toUpperCase();
                Log.d("userTypeSelect", mode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mode = null; //Ensures a safe failure at the null check in connectBtn's onClick if nothing is selected.
            }
        });

        /* connect button listener */
        connectBtn.setOnClickListener(view -> {
            if (mode == null) {
                return;
            }
            String serverUrl = "";
            if (mode.equals("STUDENT")) {
                serverUrl = serverEtx.getText().toString() + usernameEtx.getText().toString();
            } else {
                serverUrl = serverEtx.getText().toString() + mode + "_" + usernameEtx.getText().toString();
            }
            // Establish WebSocket connection and set listener
            WebSocketManager.getInstance().connectWebSocket(serverUrl);
            WebSocketManager.getInstance().setWebSocketListener(MainActivity.this);

            // got to chat activity
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });
    }


    @Override
    public void onWebSocketMessage(String message) {}

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {}

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {}

    @Override
    public void onWebSocketError(Exception ex) {}
}