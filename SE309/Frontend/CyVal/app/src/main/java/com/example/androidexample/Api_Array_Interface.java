package com.example.androidexample;

import org.json.JSONArray;

public interface Api_Array_Interface {
    void onSuccess(JSONArray response);

    void onError(String message);
}