package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class FakeFeedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_feed);

        Button logoutButton = findViewById(R.id.logout_button);
        TextView feedHeader = findViewById(R.id.feed_header);

        Bundle extras = getIntent().getExtras();


        if (extras.getString("USERNAME").equals("")) {
            feedHeader.setText("Guest's Feed");
        } else {
            feedHeader.setText(extras.getString("USERNAME") + "'s Feed");
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(FakeFeedActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}