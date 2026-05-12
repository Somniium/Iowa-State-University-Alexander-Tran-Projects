package com.example.androidexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyText, markAllRead;
    private NotificationAdapter adapter;
    private BroadcastReceiver notificationReceiver;
    private List<DTOmodels.Notification> notifications;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        if (getActivity() != null && getActivity().getIntent() != null) {
            currentUserId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        }

        recyclerView = view.findViewById(R.id.notification_recycler_view);
        emptyText = view.findViewById(R.id.notifications_empty);
        markAllRead = view.findViewById(R.id.mark_all_read);

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notifications, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onNotificationClick(DTOmodels.Notification notification) {
                if (!notification.read) {
                    markRead(notification);
                }
            }

            @Override
            public void onNotificationLongClick(DTOmodels.Notification notification) {
                if (getContext() == null) return;
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete notification?")
                        .setMessage(notification.message)
                        .setPositiveButton("Delete", (dialog, which) -> deleteNotification(notification))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        markAllRead.setOnClickListener(v -> markAllRead());

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Notifications", "New notification received, refreshing...");
                loadNotifications();
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            IntentFilter filter = new IntentFilter("com.example.androidexample.NEW_NOTIFICATION");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getActivity().registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                ContextCompat.registerReceiver(getActivity(), notificationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            }
        }
        loadNotifications();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && notificationReceiver != null) {
            getActivity().unregisterReceiver(notificationReceiver);
        }
    }

    private void loadNotifications() {
        if(currentUserId == -1) {
            showEmpty();
            return;
        }

        ApiClient.getArray(getContext(), "/users/" + currentUserId + "/notifications", new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    notifications.clear();
                    Gson gson = new Gson();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.getJSONObject(i);
                        DTOmodels.Notification notification = gson.fromJson(obj.toString(), DTOmodels.Notification.class);
                        notifications.add(notification);
                    }
                    adapter.notifyDataSetChanged();

                    if(notifications.isEmpty()) {
                        showEmpty();
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }
                    Log.d("notifications", "Loaded " + notifications.size() + " notifications");
                } catch (Exception e) {
                    Log.e("notifications", "Parse error" + e.getMessage());
                    showEmpty();
                }
            }
            @Override
            public void onError(String errorMessage) {
                Log.e("notifications", "Load failed " + errorMessage);
                showEmpty();
            }
        });
    }

    private void markRead(DTOmodels.Notification notification){
        ApiClient.putString(getContext(), "/notifications/" + notification.id + "/read", "", new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                notification.read = true;
                adapter.notifyDataSetChanged();
                Log.d("notifications", "Marked notification " + notification.id + " as read");
            }

            @Override
            public void onError(String message) {
                Log.e("notifications", "Mark read failed " + message);
            }
        });
    }

    private void markAllRead(){
        ApiClient.putString(getContext(), "/users/" + currentUserId + "/notifications/read-all", "", new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                for(DTOmodels.Notification n : notifications) {
                    n.read = true;
                }
                adapter.notifyDataSetChanged();
                if(getContext() != null){
                    Toast.makeText(getContext(), "All notifications marked as read", Toast.LENGTH_SHORT).show();
                }
                Log.d("notifications", "Marked all notifications as read");
            }

            @Override
            public void onError(String message) {
                Log.e("notifications", "Mark all read failed " + message);
            }
        });
    }

    private void deleteNotification(DTOmodels.Notification notification) {
        ApiClient.deleteString(getContext(), "/notifications/" + notification.id, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                notifications.remove(notification);
                adapter.notifyDataSetChanged();

                if (notifications.isEmpty()) {
                    showEmpty();
                }

                Log.d("Notifications", "Deleted notification " + notification.id);
                Toast.makeText(getContext(), "Notification deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Log.e("Notifications", "Delete failed: " + message);
            }
        });
    }

    private void showEmpty(){
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
    }
}
