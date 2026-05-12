package com.example.androidexample;

import retrofit2.Call;

import retrofit2.http.GET;

public interface ChuckNorrisApi {
    @GET("jokes/random")
    Call<JokeResponse> getRandomJoke();
}
