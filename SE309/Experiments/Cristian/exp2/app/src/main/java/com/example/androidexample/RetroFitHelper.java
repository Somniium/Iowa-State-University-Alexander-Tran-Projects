package com.example.androidexample;

import android.content.Context;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroFitHelper {

    public static final String BASE_URL = "https://api.chucknorris.io/";

    // Generic method to create any Retrofit service
    public static <S> S createRetrofitService(Class<S> serviceClass) {
        return buildRetrofit().create(serviceClass);
    }

    // Builder method
    public static Retrofit buildRetrofit() {
        return new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }
}