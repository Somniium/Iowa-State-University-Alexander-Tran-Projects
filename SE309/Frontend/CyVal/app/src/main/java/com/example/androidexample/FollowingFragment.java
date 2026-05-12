package com.example.androidexample;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * FollowingFragment — lists users the current user follows.
 *
 * <ul>
 *   <li>Short tap  → opens {@link ViewProfileFragment} for that user</li>
 *   <li>Long press → confirmation dialog then unfollow</li>
 * </ul>
 *
 * User IDs are stored alongside display names during the initial fetch so that
 * profile navigation and unfollow calls never need a secondary name→ID lookup.
 */
public class FollowingFragment extends Fragment {

    private static final String TAG             = "FollowingFragment";
    private static final String NO_FOLLOWING_MSG = "Not following anyone yet.";

    private int userId;
    private ListView listView;
    private Button   returnButton;

    /** Parallel lists — index i in followingNames matches index i in followingUserIds. */
    private final ArrayList<String>  followingNames   = new ArrayList<>();
    private final ArrayList<Integer> followingUserIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        userId       = requireActivity().getIntent().getIntExtra("USER_ID", -1);
        listView     = view.findViewById(R.id.following_list);
        returnButton = view.findViewById(R.id.return_button);

        loadFollowing();

        // Short tap → view that user's profile (mirrors FriendsFragment behaviour)
        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            if (!name.equals(NO_FOLLOWING_MSG)) {
                int targetId = followingUserIds.get(position);
                ((MainFeedActivity) requireActivity())
                        .loadFragment(ViewProfileFragment.newInstance(targetId));
            }
        });

        // Long press → confirm unfollow
        listView.setOnItemLongClickListener((parent, itemView, position, id) -> {
            String name = (String) parent.getItemAtPosition(position);
            if (!name.equals(NO_FOLLOWING_MSG)) {
                int targetId = followingUserIds.get(position);
                new AlertDialog.Builder(requireActivity())
                        .setTitle("Unfollow")
                        .setMessage("Are you sure you want to unfollow " + name + "?")
                        .setPositiveButton("Unfollow", (dialog, which) -> unfollowUser(targetId))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .show();
            }
            return true; // consume the long-press
        });

        returnButton.setOnClickListener(v ->
                ((MainFeedActivity) requireActivity()).loadFragment(new UserProfileFragment()));

        return view;
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadFollowing() {
        followingNames.clear();
        followingUserIds.clear();

        ApiClient.getArray(getContext(), "/follower/get-following/" + userId,
                new Api_Array_Interface() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject user = response.getJSONObject(i);
                                followingNames.add(user.getString("name"));
                                followingUserIds.add(user.getInt("id"));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Parse error: " + e.getMessage());
                        }
                        updateListUI();
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Load following error: " + message);
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Could not load following list.",
                                    Toast.LENGTH_SHORT).show();
                        updateListUI();
                    }
                });
    }

    private void unfollowUser(int targetUserId) {
        String endpoint = String.format(
                "/follower/remove-follower/currUser/%d/followUser/%d", userId, targetUserId);

        ApiClient.deleteString(getContext(), endpoint, new Api_String_Interface() {
            @Override
            public void onSuccess(String response) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Unfollowed.", Toast.LENGTH_SHORT).show();
                loadFollowing(); // refresh the list
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Unfollow error: " + message);
                if (getContext() != null)
                    Toast.makeText(getContext(), "Could not unfollow user.",
                            Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================================
    // UI update
    // =========================================================================

    private void updateListUI() {
        String[] display = followingNames.isEmpty()
                ? new String[]{NO_FOLLOWING_MSG}
                : followingNames.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, display);
        listView.setAdapter(adapter);
    }
}
