package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public interface Api_Interface {
    void onSuccess(JSONObject response);

    void onError(String message);
}
