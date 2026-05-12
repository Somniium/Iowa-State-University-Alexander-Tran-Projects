package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
 * @author Edwin Cepeda
 */
public class GroupsFragment extends Fragment {
    int groupId;
    int userId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        userId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        ListView groupList = view.findViewById(R.id.group_list);
        ArrayList<String> studentGroups = new ArrayList<String>();
        ApiClient.getArray(GroupsFragment.this.getContext(), "/get-user-groups/" + userId, new Api_Array_Interface() {
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    String stringedResponse = response.toString();
                    JSONArray groupData = new JSONArray(stringedResponse);
                    studentGroups.add("CyVal AI");
                    for (int i = 0; i < groupData.length(); i++) {
                        try {
                            String singleGroupName = groupData.getJSONObject(i).getString("name");
                            studentGroups.add(singleGroupName);
                            Log.d("test: ", studentGroups.get(i));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    String[] studentGroupsArray = new String[studentGroups.size()];
                    for (int i = 0; i < studentGroups.size(); i++) {
                        studentGroupsArray[i] = studentGroups.get(i);
                        Log.d("test2: ", studentGroupsArray[i]);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, studentGroupsArray);
                    groupList.setAdapter(adapter);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(String message) {
            }
        });

        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String group = adapterView.getItemAtPosition(i).toString();
                if ("CyVal AI".equals(group)) {
                    Intent intent = new Intent(getContext(), AiChatActivity.class);
                    intent.putExtra("USER_ID", userId);
                    startActivity(intent);
                    return;
                }
                ApiClient.get(getContext(), "/group/name/" + group, new Api_Interface() {

                    @Override
                    public void onSuccess(JSONObject response) {
                        try {
                            groupId = response.getInt("groupId");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        finishChatConnect(group);
                    }

                    @Override
                    public void onError(String message) {
                    }
                });

            }
        });

        return view;
    }

    private void finishChatConnect(String group) {
        Intent intent = new Intent(getContext(), ChatActivity.class);

        intent.putExtra("GROUP_ID", groupId);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("GROUP_NAME", group);
        startActivity(intent);
    }
}