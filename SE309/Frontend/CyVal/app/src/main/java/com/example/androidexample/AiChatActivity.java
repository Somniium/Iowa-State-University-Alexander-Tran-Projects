package com.example.androidexample;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AiChatActivity extends AppCompatActivity {

    private RecyclerView messageDisplay;
    private EditText messageEdit;
    private Button sendButton;
    private ArrayList<String> messages = new ArrayList<>();
    private MessageAdapter adapter;

    private ImageView backButton;

    private long sessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        messageDisplay = findViewById(R.id.ai_message_display);
        messageEdit = findViewById(R.id.ai_message_edit);
        sendButton = findViewById(R.id.ai_send_button);
        backButton = findViewById(R.id.back_button);

        adapter = new MessageAdapter(messages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        messageDisplay.setLayoutManager(lm);
        messageDisplay.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String text = messageEdit.getText().toString().trim();
            if (text.isEmpty()) return;
            messageEdit.setText("");
            messages.add("You: " + text);
            adapter.notifyDataSetChanged();
            messageDisplay.smoothScrollToPosition(messages.size() - 1);
            sendButton.setEnabled(false);

            DTOmodels.MessageRequest req = new DTOmodels.MessageRequest(
                    (long) getIntent().getIntExtra("USER_ID", -1),
                    sessionId > 0 ? sessionId : null,
                    text);

            ApiClient.post(this, "/api/chat/message", req, new Api_Interface() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (sessionId <= 0) sessionId = response.getLong("sessionId");
                        messages.add("CyVal AI: " + response.getString("content"));
                        adapter.notifyDataSetChanged();
                        messageDisplay.smoothScrollToPosition(messages.size() - 1);
                    } catch (JSONException e) {
                        Toast.makeText(AiChatActivity.this, "Error reading response", Toast.LENGTH_SHORT).show();
                    }
                    sendButton.setEnabled(true);
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(AiChatActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                }
            });
        });

    }
}
