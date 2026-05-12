package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * ChatActivity handles the chat interface where users can send and receive messages
 * using a WebSocket connection.
 * @author Edwin Cepeda
 */
public class ChatActivity extends AppCompatActivity /*implements WebSocketListener*/ {

    private Button sendButton, returnButton;
    private EditText messageEdit;
    private RecyclerView messageDisplay;
    private WebSocketClient client;
    private TextView groupId, userId, groupName;
    private int groupIdValue, userIdValue;
    private String groupNameValue;
    private ArrayList<String> sessionMessages = new ArrayList<String>();
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_interface);


        /* initialize UI elements */
        sendButton = (Button) findViewById(R.id.send_button);
        messageEdit = (EditText) findViewById(R.id.message_edit);
        messageDisplay = (RecyclerView) findViewById(R.id.message_display);
        returnButton = findViewById(R.id.return_button);
        groupId = findViewById(R.id.group_id);
        userId = findViewById(R.id.user_id);
        groupName = findViewById(R.id.group_name);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupIdValue = extras.getInt("GROUP_ID");
            userIdValue = extras.getInt("USER_ID");
            groupNameValue = extras.getString("GROUP_NAME");
            groupId.setText("Group ID: " + groupIdValue);
            userId.setText("User ID: " + userIdValue);
            groupName.setText(groupNameValue);
        }

        String serverUrl = String.format("http://coms-3090-017.class.las.iastate.edu:8080/chat/group/%d/user/%d", groupIdValue, userIdValue);
        connectWebSocket(serverUrl);

        adapter = new MessageAdapter(sessionMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        messageDisplay.setLayoutManager(layoutManager);
        messageDisplay.setAdapter(adapter);

        /* send button listener */
        sendButton.setOnClickListener(v -> {
            try {
                // send message
                Log.d("Sending: ", messageEdit.getText().toString());
//                WebSocketManager.getInstance().sendMessage(msgEtx.getText().toString());
//                //clear text box once the msg is sent
//                msgEtx.setText("");
                String message = messageEdit.getText().toString();
                if (message != null && message.length() > 0){
                   client.send(message);
                }
                messageEdit.setText("");
            } catch (Exception e) {
                Log.e("Chat Exception", String.valueOf(e));
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                client.close();
                checkProfessor();
            }
        });
    }

    private void connectWebSocket(String serverUrl) {
        URI uri;
        try {
            uri = new URI(serverUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Log.d("Status:", "got this far.");
        client = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
//                Toast.makeText(ChatActivity.this, "You're connected.", Toast.LENGTH_SHORT).show();
                Log.d("Status:", "Connected.");
                messageDisplay.smoothScrollToPosition(sessionMessages.size()-1);
            }

            @Override
            public void onMessage(String message) {
                runOnUiThread(() -> {
                    Log.d("Server response: ", message);
                    sessionMessages.add(message);
                    adapter.notifyDataSetChanged();
                    messageDisplay.smoothScrollToPosition(sessionMessages.size()-1);
                });
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("Websocket", "Closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.d("Status:", "Force disconnect. " + ex.getMessage());
            }
        };

        if (!client.isOpen()) {
            client.connect();
        }

    }

    protected void checkProfessor() {
        ApiClient.get(ChatActivity.this, "/professor/user/" + userIdValue, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                Intent intent = new Intent(ChatActivity.this, ProfessorGroupManageActivity.class);
                intent.putExtra("GROUP_NAME", groupNameValue);
                try {
                    intent.putExtra("PROFESSOR_ID", response.getInt("id"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                intent.putExtra("USER_ID", userIdValue);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                //If not a professor, reroute to student exit
                studentLeave();
            }
        });
    }

    protected void studentLeave() {
        Intent intent = new Intent(ChatActivity.this, MainFeedActivity.class);
        intent.putExtra("USER_ID", userIdValue);
        startActivity(intent);
    }

//    /**
//     * Called when a message is received from the WebSocket.
//     * This method ensures that UI updates happen on the main thread.
//     */
//    @Override
//    public void onWebSocketMessage(String message) {
//        /**
//         * In Android, all UI-related operations must be performed on the main UI thread
//         * to ensure smooth and responsive user interfaces. The 'runOnUiThread' method
//         * is used to post a runnable to the UI thread's message queue, allowing UI updates
//         * to occur safely from a background or non-UI thread.
//         */
//        runOnUiThread(() -> {
//            Log.d("Server Response: ", message);
//            String s = msgTv.getText().toString();
//            msgTv.setText(s + "\n"+message);
//        });
//    }
//
//    /**
//     * Called when the WebSocket connection is closed.
//     * Displays the closure reason in the TextView.
//     *
//     * @param code   The status code of the closure
//     * @param reason The reason provided for closure
//     */
//    @Override
//    public void onWebSocketClose(int code, String reason, boolean remote) {
//        String closedBy = remote ? "server" : "local";
//        runOnUiThread(() -> {
////            String s = msgTv.getText().toString();
////            msgTv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
//        });
//    }
//
//    @Override
//    public void onWebSocketOpen(ServerHandshake handshakedata) {}
//
//
//    @Override
//    public void onWebSocketError(Exception ex) {}
}