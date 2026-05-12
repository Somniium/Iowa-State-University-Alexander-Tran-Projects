package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Make sure the class name matches your filename exactly!
public class ChuckNorrisJokes extends AppCompatActivity {

    // 1. Declare variables inside the class, but outside methods
    private TextView jokeTextView;
    private Button fetchButton;

    private Button backBtn;

    private String joke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chuck_norris);

        // 2. Initialize UI elements inside onCreate
        jokeTextView = findViewById(R.id.jokeTextView);
        fetchButton = findViewById(R.id.fetchButton);
        backBtn = findViewById(R.id.counter_back_btn);

        // 3. Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.chucknorris.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ChuckNorrisApi api = retrofit.create(ChuckNorrisApi.class);

        // 4. Set the listener
        fetchButton.setOnClickListener(v -> {
            api.getRandomJoke().enqueue(new Callback<JokeResponse>() {
                @Override
                public void onResponse(Call<JokeResponse> call, Response<JokeResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        joke = response.body().getJokeText();

                        jokeTextView.setText(response.body().getJokeText());
                    }
                }

                @Override
                public void onFailure(Call<JokeResponse> call, Throwable t) {
                    jokeTextView.setText("Error: " + t.getMessage());
                }
            });
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChuckNorrisJokes.this, MainActivity.class);
                intent.putExtra("ChuckJoke", joke);  // key-value to pass to the MainActivity
                startActivity(intent);
            }
        });
    }
}