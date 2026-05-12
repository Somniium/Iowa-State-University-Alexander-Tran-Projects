package com.example.androidexample;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import com.android.volley.toolbox.StringRequest;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @author Cristian Alvarez
 * @version 1.2
 * * A dedicated API Client that handles all Volley requests between
 * Android studio in the Frontend and the Spring boot in the Backend.
 * * This class uses Volley for network communication and Gson for
 * robust data conversion of Data Transfer Objects (DTOs) to JSON.
 * This class handles all 4 standard CRUD operations(POST, GET, PUT, and DELETE)
 * in a single method.
 */
public class ApiClient {

    /**
     * URL for the remote dataBase
     */
    private static final String Server_URL = "http://coms-3090-017.class.las.iastate.edu:8080";
//    private static final String Server_URL = "http://10.0.2.2:3001";

    /**
     * sends a POST request to the server with the given endpoint and DTO
     * to create new data on the remote database.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to. (ex: /users/login)
     * @param dto      The data transfer object that is being converted to JSON via Gson.
     * @param result   an interface that handles the response or error from the server.
     */
    public static void post(Context context, String endpoint, Object dto, Api_Interface result) {
        sendRequest(context, Request.Method.POST, endpoint, dto, result);
    }

    public static void postString(Context context, String endpoint, Api_String_Interface result) {
        String url = Server_URL + endpoint;
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * sends a GET request to the server with the given endpoint.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to. (ex: /users/id/1)
     * @param result   an interface that handles the response or error from the server.
     */
    public static void get(Context context, String endpoint, Api_Interface result) {
        sendRequest(context, Request.Method.GET, endpoint, null, result);
    }

    /**
     * sends a PUT request to the server with the given endpoint and DTO
     * to update data on the remote database.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to. (ex: /users)
     * @param dto      The data transfer object that is being converted to JSON via Gson.
     * @param result   an interface that handles the response or error from the server.
     */
    public static void put(Context context, String endpoint, Object dto, Api_Interface result) {
        sendRequest(context, Request.Method.PUT, endpoint, dto, result);
    }

    /**
     * Sends a PUT request to the server with the given endpoint and a String body
     * to update data on the remote database. This method expects a String/Array response.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to.
     * @param Body     The raw string body to include in the PUT request.
     * @param result   An interface that handles the response or error from the server.
     */
    public static void putString(Context context, String endpoint, String Body, Api_String_Interface result) {
        String url = Server_URL + endpoint;

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    public static void postString(Context context, String endpoint, String Body, Api_String_Interface result) {
        String url = Server_URL + endpoint;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * sends a DELETE request to the server with the given endpoint to
     * delete data in the remote database.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to. (ex: /users/email/test@iastate.edu)
     * @param result   an interface that handles the response or error from the server.
     */
    public static void delete(Context context, String endpoint, Api_Interface result) {
        sendRequest(context, Request.Method.DELETE, endpoint, null, result);
    }

    /**
     * Sends a DELETE request to the server with the given endpoint to
     * delete data in the remote database, handling a String/Array response.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to.
     * @param result   An interface that handles the response or error from the server.
     */
    public static void deleteString(Context context, String endpoint, Api_String_Interface result) {
        String url = Server_URL + endpoint;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Private helper method that packages the request and adds it to the volley queue.
     */
    private static void sendRequest(Context context, int method, String endpoint, Object dto, Api_Interface result) {
        String url = Server_URL + endpoint;
        JSONObject body = null;

        //converts the DTO to JSON
        if (dto != null) {
            try {
                String dtoString = new Gson().toJson(dto);
                body = new JSONObject(dtoString);
            } catch (Exception e) {
                Log.e("CrudApi", "Error converting DTO to JSON" + e.getMessage());
            }
        }

        JsonObjectRequest request = new JsonObjectRequest(method, url, body,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Gets the error message sent from the remote server.
     *
     * @param error the VolleyError that was sent from the server.
     * @return A string containing the error message.
     */
    private static String getError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            return new String(error.networkResponse.data, StandardCharsets.UTF_8);
        }
        return "Network Error: Could not reach server";
    }

    /**
     * Fetches an image from the provided URL and loads it into the given ImageView
     * using Volley's ImageLoader. It handles displaying a placeholder while loading
     * and a default error image if the URL is invalid or the network request fails.
     *
     * @param context   The application/Activity that this method is called from.
     * @param url       The network URL string pointing to the image file.
     * @param imageView The target ImageView widget where the image will be displayed.
     */
    public static void loadImage(Context context, String url, android.widget.ImageView imageView) {
        if (url == null || url.isEmpty()) {
            imageView.setImageResource(R.drawable.error_image);
            return;
        }

        VolleySingleton.getInstance(context).getImageLoader().get(url,
                com.android.volley.toolbox.ImageLoader.getImageListener(
                        imageView,
                        R.drawable.cyval_logo,
                        R.drawable.error_image
                )
        );

    }

    /**
     * Sends a GET request to the server with the given endpoint specifically
     * expecting a JSON Array in the response body.
     *
     * @param context  The application/Activity that this method is called from.
     * @param endpoint The endpoint to send the request to. (ex: /users/all)
     * @param result   An interface that handles the JSON Array response (converted to a String) or an error.
     */
    public static void getArray(Context context, String endpoint, Api_Array_Interface result) {
        String url = Server_URL + endpoint;

        JsonArrayRequest request;
        request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> result.onSuccess(response),
                error -> result.onError(getError(error))
        );
        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    private static OkHttpClient wsClient = new OkHttpClient();

    public static WebSocket connectWebSocket(String endPoint, WebSocketListener listener) {
        String url = Server_URL.replace("http://", "ws://") + endPoint;
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        return wsClient.newWebSocket(request, listener);
    }
}
