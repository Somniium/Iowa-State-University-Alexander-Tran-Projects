package com.example.androidexample;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class NotificationWebSocket extends Service {

    private static final String TAG = "NotificationWS";
    private static final String CHANNEL_ID = "CyVal_Notifications";

    private WebSocket ws;
    private Handler handler;
    private int userId = -1;
    private boolean shouldReconnect = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            userId = intent.getIntExtra("USER_ID", -1);
        }
        if(userId != -1) {
            shouldReconnect = true;
            connectWebSocket();
        }
        return START_STICKY;
    }

    private void connectWebSocket() {
        if(ws != null) {
            ws.close(1000, "Reconnecting");
        }

        Log.d(TAG, "Connecting WebSocket for user " + userId);

        ws = ApiClient.connectWebSocket("/ws/notifications/" + userId, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "WebSocket opened for user " + userId);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);

                handler.post(() -> {
                    try {
                        JSONObject obj = new JSONObject(text);
                        if("pong".equals(obj.optString("type"))) return;

                        String message = obj.optString("message", "New notification");
                        String type = obj.optString("type", "");
                        int notificationId = obj.optInt("id", (int)System.currentTimeMillis());

                        showNotification(notificationId, type, message);
                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse error, showing plain text: " + text);
                        showNotification((int) System.currentTimeMillis(), "INFO", text);
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closing: " + reason);
                ws.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
                scheduleReconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
                scheduleReconnect();
            }
        });
    }

    private void scheduleReconnect() {
        if(!shouldReconnect) return;
        Log.d(TAG, "Scheduling reconnect in 5 seconds");
        handler.postDelayed(() -> {
            if(shouldReconnect && userId != -1){
                connectWebSocket();
            }
        }, 5000);
    }

    private void showNotification(int notificationId, String type, String message) {
        Intent intent = new Intent(this, MainFeedActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("openNotifs", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT |PendingIntent.FLAG_IMMUTABLE);

        String title;
        switch(type){
            case "COMMENT":
                title = "New comment";
                break;
            case "LIKE":
                title = "New like";
                break;
            case "REVIEW":
                title = "REVIEW POSTED";
                break;
            default:
                title = "CyVal";
                break;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cyval_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
        
        Intent broadcastIntent = new Intent("com.example.androidexample.NEW_NOTIFICATION");
        broadcastIntent.setPackage(getPackageName()); // Ensure it only goes to our app
        sendBroadcast(broadcastIntent);
        Log.d(TAG, "Notification shown: " + title + " - " + message + ". Broadcast sent.");
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "CyVal Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for comments, likes, and reviews");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shouldReconnect = false;
        if(ws != null) {
            ws.close(1000, "Service destroyed");
        }
        handler.removeCallbacksAndMessages(null);
        Log.d(TAG, "WebSocket service Stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
