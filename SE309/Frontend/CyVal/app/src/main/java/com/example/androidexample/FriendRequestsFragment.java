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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendRequestsFragment extends Fragment {
    int groupId;
    int userId;
    ListView incomingList;
    ListView outgoingList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        userId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        incomingList = view.findViewById(R.id.incoming_request_list);
        outgoingList = view.findViewById(R.id.outgoing_request_list);
        Button newRequestButton = view.findViewById(R.id.new_request_button);
        EditText friendIdEdit = view.findViewById(R.id.friend_id_edit);
        Button backButton = view.findViewById(R.id.return_button);

        // Get all incoming friend requests and display them
        updateRequestList(incomingList, "/friends/received-by/" + userId);
        updateRequestList(outgoingList, "/friends/sent-by/" + userId);

//        ArrayList<String> incomingRequestStrings = new ArrayList<String>();
//        ApiClient.getArray(FriendRequestsFragment.this.getContext(), "/friends/received-by/" + userId, new Api_Array_Interface() {
//            @Override
//            public void onSuccess(JSONArray response) {
//                try {
//                    String stringedResponse = response.toString();
//                    JSONArray incomingRequestData = new JSONArray(stringedResponse);
//                    for (int i = 0; i < incomingRequestData.length(); i++) {
//                        try {
//                            String incomingNameToken = incomingRequestData.getJSONObject(i).getString("name");
//                            String statusToken = incomingRequestData.getJSONObject(i).getString("friendStatus");
//                            if (!statusToken.equals("ACCEPTED")) { //ACCEPTED friend requests show up only in Friends page, not as requests
//                                String fullRequestString = "[" + statusToken + "] " + incomingNameToken;
//                                incomingRequestStrings.add(incomingNameToken);
//                                Log.d("test: ", incomingRequestStrings.get(i));
//                            }
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    String[] incomingRequestsArray = new String[incomingRequestStrings.size()];
//                    for (int i = 0; i < incomingRequestStrings.size(); i++) {
//                        incomingRequestsArray[i] = incomingRequestStrings.get(i);
//                        Log.d("test2: ", incomingRequestsArray[i]);
//                    }
//
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, incomingRequestsArray);
//                    incomingList.setAdapter(adapter);
//
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//
//            @Override
//            public void onError(String message) {
//            }
//        });
//
//        // Get all outgoing friend requests and display them
//        ArrayList<String> outgoingRequestStrings = new ArrayList<String>();
//        ApiClient.getArray(FriendRequestsFragment.this.getContext(), "/friends/sent-by/" + userId, new Api_Array_Interface() {
//            @Override
//            public void onSuccess(JSONArray response) {
//                try {
//                    String stringedResponse = response.toString();
//                    JSONArray outgoingRequestData = new JSONArray(stringedResponse);
//                    for (int i = 0; i < outgoingRequestData.length(); i++) {
//                        try {
//                            String outgoingNameToken = outgoingRequestData.getJSONObject(i).getString("name");
//                            String statusToken = outgoingRequestData.getJSONObject(i).getString("friendStatus");
//                            if (!statusToken.equals("ACCEPTED")) { //ACCEPTED friend requests show up only in Friends page, not as requests
//                                String fullRequestString = "[" + statusToken + "] " + outgoingNameToken;
//                                outgoingRequestStrings.add(outgoingNameToken);
//                                Log.d("test: ", outgoingRequestStrings.get(i));
//                            }
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    String[] outgoingRequestsArray = new String[outgoingRequestStrings.size()];
//                    for (int i = 0; i < outgoingRequestStrings.size(); i++) {
//                        outgoingRequestsArray[i] = outgoingRequestStrings.get(i);
//                        Log.d("test2: ", outgoingRequestsArray[i]);
//                    }
//
//                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, outgoingRequestsArray);
//                    outgoingList.setAdapter(adapter);
//
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }
//
//            @Override
//            public void onError(String message) {
//            }
//        });

        newRequestButton.setOnClickListener(v -> {
            int friendId = Integer.parseInt(friendIdEdit.getText().toString());
            String endpoint = String.format("/friend/send-request/currUser/%d/friend/%d", userId, friendId);
            ApiClient.post(getContext(), endpoint, null, new Api_Interface() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(getContext(), "Friend request sent to user" + friendId + "!", Toast.LENGTH_SHORT).show();
                    updateRequestList(outgoingList, "/friends/sent-by/" + userId);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("Send Request Error", message);
                }
            });
        });

        backButton.setOnClickListener(v -> {
            ((MainFeedActivity) getActivity()).loadFragment(new FriendsFragment());
        });



        incomingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String requestString = adapterView.getItemAtPosition(i).toString();
                //Split requestString into tokens. 0 is status, 1 is target's name, 2 is unneeded, 3 is requestID
                String[] requestTokens = requestString.split(" ");
                new AlertDialog.Builder(getActivity())
                        .setTitle("This request is " + requestTokens[0])
                        .setMessage(requestTokens[1] + " has send you a friend request.\nAvaiable options:")
                        .setNegativeButton("Decline", (dialog, which) -> {
                            rejectRequest(Integer.parseInt(requestTokens[3]), incomingList);
                        })
                        .setNeutralButton("Cancel", null)
                        .setPositiveButton("Accept", (dialog, which) -> {
                            acceptRequest(Integer.parseInt(requestTokens[3]), incomingList);
                        })
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .show();
            }
        });

        outgoingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String requestString = adapterView.getItemAtPosition(i).toString();
                //Split requestString into tokens. 0 is status, 1 is target's name, 2 is unneeded, 3 is requestID
                String[] requestTokens = requestString.split(" ");

                if (requestTokens[0].equals("[PENDING]")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Your request is pending.")
                            .setMessage(requestTokens[1] + " has not responded yet.\nAvailable options:")
                            .setNegativeButton("Unsend Request", (dialog, which) -> {
                                deleteRequest(Integer.parseInt(requestTokens[3]), outgoingList);
                            })
                            .setNeutralButton("Cancel", null)
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .show();
                } else if (requestTokens[0].equals("[DECLINED]")) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Your request has been declined.")
                            .setMessage(requestTokens[1] + " has declined your friend request.")
                            .setNegativeButton("Close Request", (dialog, which) -> {
                                deleteRequest(Integer.parseInt(requestTokens[3]), outgoingList);
                            })
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .show();
                }

            }
        });

        return view;
    }

    private void rejectRequest(int requestId, ListView requestList) {
        String endpoint = "/friend/decline-request/" + requestId;
        ApiClient.put(FriendRequestsFragment.this.getContext(), endpoint, null, new Api_Interface () {

            @Override
            public void onSuccess(JSONObject response) {
                updateRequestList(requestList, "/friends/received-by/" + userId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendRequestsFragment.this.getContext(), "Could not reject.", Toast.LENGTH_SHORT).show();
                Log.e("Reject error", message);
            }
        });
    }

    private void acceptRequest(int requestId, ListView requestList) {
        String endpoint = "/friend/accept-request/" + requestId;
        ApiClient.put(FriendRequestsFragment.this.getContext(), endpoint, null, new Api_Interface () {

            @Override
            public void onSuccess(JSONObject response) {
                updateRequestList(requestList, "/friends/received-by/" + userId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendRequestsFragment.this.getContext(), "Could not reject.", Toast.LENGTH_SHORT).show();
                Log.e("Reject error", message);
            }
        });
    }

    private void deleteRequest(int requestId, ListView requestList) {
        String endpoint = "/friend/remove-friend-or-cancel-request/" + requestId;
        ApiClient.deleteString(FriendRequestsFragment.this.getContext(), endpoint, new Api_String_Interface () {

            @Override
            public void onSuccess(String response) {
                updateRequestList(requestList, "/friends/sent-by/" + userId);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendRequestsFragment.this.getContext(), "Could not delete.", Toast.LENGTH_SHORT).show();
                Log.e("Delete error", message);
            }
        });
    }

    private void updateRequestList(ListView requestList, String endpoint) {
        ArrayList<String> RequestStrings = new ArrayList<String>();
        ApiClient.getArray(FriendRequestsFragment.this.getContext(), endpoint, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    String stringedResponse = response.toString();
                    JSONArray RequestData = new JSONArray(stringedResponse);
                    for (int i = 0; i < RequestData.length(); i++) {
                        try {
                            JSONObject userObject = null;
                            if (requestList.equals(incomingList)) {
                                userObject = RequestData.getJSONObject(i).getJSONObject("user");
                            } else {
                                userObject = RequestData.getJSONObject(i).getJSONObject("friend");
                            }

                            String nameToken = userObject.getString("name");
                            String statusToken = RequestData.getJSONObject(i).getString("friendStatus");
                            int requestIdToken = RequestData.getJSONObject(i).getInt("id");
                            if (!statusToken.equals("ACCEPTED")) { //ACCEPTED friend requests show up only in Friends page, not as requests
                                String fullRequestString = "[" + statusToken + "] " + nameToken + " RequestID: " + requestIdToken;
                                RequestStrings.add(fullRequestString);
                                Log.d("test: ", fullRequestString);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    String[] requestsArray = new String[RequestStrings.size()];
                    for (int i = 0; i < RequestStrings.size(); i++) {
                        requestsArray[i] = RequestStrings.get(i);
                        Log.d("test2: ", requestsArray[i]);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, requestsArray);
                    requestList.setAdapter(adapter);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(String message) {
                Toast.makeText(FriendRequestsFragment.this.getContext(), "Could not pull friend data.", Toast.LENGTH_SHORT).show();
                Log.e("Pull friend data error", message);
            }
        });
    }
}