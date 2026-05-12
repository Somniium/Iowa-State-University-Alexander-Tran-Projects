package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author Edwin Cepeda and Cristian Alvarez
 */
public class FriendsFragment extends Fragment {
    int groupId;
    int userId;

    int unfriendId;
    ListView friendsListViewer;
    ArrayList<String> friendsList;
    ArrayList<Integer> friendshipIDs = new ArrayList<Integer>();
    ArrayList<Integer> friendUserIDs = new ArrayList<Integer>();
    ArrayList<String> newFollowedAccounts = new ArrayList<String>();
    String noFriendsString = "No friends yet. Add some!";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        userId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        friendsListViewer = view.findViewById(R.id.friends_list);
        Button friendRequestsButton = view.findViewById(R.id.requests_button);
        friendsList = new ArrayList<String>();
        Button backButton = view.findViewById(R.id.return_button);

        //This method kicks off a chain of methods that updates the UI
        updateGetSentFriends();

        friendRequestsButton.setOnClickListener(v -> {
            ((MainFeedActivity) getActivity()).loadFragment(new FriendRequestsFragment());
        });

        backButton.setOnClickListener(v -> {
            ((MainFeedActivity) getActivity()).loadFragment(new UserProfileFragment());
        });

        // Tap → open that friend's profile
        friendsListViewer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String friend = adapterView.getItemAtPosition(i).toString();
                if (!friend.equals(noFriendsString)) {
                    int friendUserId = friendUserIDs.get(i);
                    ((MainFeedActivity) requireActivity()).loadFragment(
                            ViewProfileFragment.newInstance(friendUserId));
                }
            }
        });

        // Long press → confirm unfriend
        friendsListViewer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String friend = adapterView.getItemAtPosition(i).toString();
                if (!friend.equals(noFriendsString)) {
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Un-friend")
                            .setMessage("Are you sure you want to unfriend " + friend + "?")
                            .setPositiveButton("Unfriend", (dialog, which) -> {
                                int friendshipId = friendshipIDs.get(i);
                                unfriendUser(friendshipId);
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .show();
                }
                return true; // consume the long-press event
            }
        });

        return view;
    }

    private void getUserIdByNameAndDelete(String name) {
        ApiClient.get(getContext(), "/users/name/" + name, new Api_Interface() {

            @Override
            public void onSuccess(JSONObject response) {
                try {
                    unfriendId = response.getInt("id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                unfriendUser(unfriendId);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void unfriendUser(int friendId) {
        String endpoint = String.format("/friend/remove-friend-or-cancel-request/%d", friendId);
        ApiClient.deleteString(getContext(), endpoint, new Api_String_Interface() {

            @Override
            public void onSuccess(String response) {
                updateGetSentFriends();
                Toast.makeText(getContext(), "Unfollowed.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Log.e("Unfollow Error", message);
                Toast.makeText(getContext(), "Could not unfollow user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGetSentFriends() {
        friendsList.clear();
        friendshipIDs.clear();
        friendUserIDs.clear();
        ApiClient.getArray(FriendsFragment.this.getContext(), "/friends/sent-by/" + userId, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    String stringedResponse = response.toString();
                    JSONArray friendData = new JSONArray(stringedResponse);
                    for (int i = 0; i < friendData.length(); i++) {
                        try {
                            //Check if the friend object's status is accepted
                            String status = friendData.getJSONObject(i).getString("friendStatus");
                            if (status.equals("ACCEPTED")) { //ACCEPTED friends only
                                JSONObject friendObject = friendData.getJSONObject(i).getJSONObject("friend");
                                friendshipIDs.add(friendData.getJSONObject(i).getInt("id"));
                                String nameToken = friendObject.getString("name");
                                int friendId = friendObject.getInt("id");
                                friendUserIDs.add(friendId);
                                String fullFriendString = nameToken + " ID: " + friendId;
                                friendsList.add(fullFriendString);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    updateGetReceivedFriends();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendsFragment.this.getContext(), "Could not pull friend data.", Toast.LENGTH_SHORT).show();
                Log.e("Pull friend data error", message);
            }
        });
    }

    private void updateGetReceivedFriends() {
        ApiClient.getArray(FriendsFragment.this.getContext(), "/friends/received-by/" + userId, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    String stringedResponse = response.toString();
                    JSONArray friendData = new JSONArray(stringedResponse);
                    for (int i = 0; i < friendData.length(); i++) {
                        try {
                            //Check if the friend object's status is accepted
                            String status = friendData.getJSONObject(i).getString("friendStatus");
                            if (status.equals("ACCEPTED")) { //ACCEPTED friends only
                                JSONObject friendObject = friendData.getJSONObject(i).getJSONObject("user");
                                friendshipIDs.add(friendData.getJSONObject(i).getInt("id"));
                                String nameToken = friendObject.getString("name");
                                int friendId = friendObject.getInt("id");
                                friendUserIDs.add(friendId);
                                String fullFriendString = nameToken + " ID: " + friendId;
                                friendsList.add(fullFriendString);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    updateFriendsListUI();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendsFragment.this.getContext(), "Could not pull friend data.", Toast.LENGTH_SHORT).show();
                Log.e("Pull friend data error", message);
            }
        });
    }

    private void updateFriendsListUI() {
        if (!isAdded() || getContext() == null) return;
        String[] friendsArray = new String[1];
        if (!friendsList.isEmpty()) {
            friendsArray = new String[friendsList.size()];
            for (int i = 0; i < friendsList.size(); i++) {
                friendsArray[i] = friendsList.get(i);
                Log.d("test2: ", friendsArray[i]);
            }
        } else {
            friendsArray[0] = noFriendsString;
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FriendsFragment.this.getContext(), android.R.layout.simple_list_item_1, friendsArray);
        friendsListViewer.setAdapter(adapter);
    }
}