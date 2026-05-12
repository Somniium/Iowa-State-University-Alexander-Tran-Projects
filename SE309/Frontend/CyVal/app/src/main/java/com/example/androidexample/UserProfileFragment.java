package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfileFragment extends Fragment {

    private TextView welcomeHeader;
    private TextView profileNameText;
    private TextView profileEmailText;
    private TextView profileSeparatorText;
    private TextView profileIdText;

    private Button editProfileBtn;
    private CardView reviewsCard, settingsCard, logoutCard, friendsCard, followingCard, professorCard, adminCard;
    private TextView reviewsText, settingsText, logoutText, booksText, albumsText, professorText, adminText;

    private int userId;

    //default value -1 signifies user is NOT a professor or admin, respectively
    private int professorId = -1;
    private int adminId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        //Get all necessary IDs, -1 if not of that type
        userId = getActivity().getIntent().getIntExtra("USER_ID", -1);
        fetchProfessorIdByUserID(userId);
        fetchAdminIdByUserID(userId);

        welcomeHeader = view.findViewById(R.id.welcome_header);
        profileNameText = view.findViewById(R.id.profile_name);
        profileEmailText = view.findViewById(R.id.profile_email);
        profileSeparatorText = view.findViewById(R.id.profile_separator);
        profileIdText = view.findViewById(R.id.profile_id);

        editProfileBtn = view.findViewById(R.id.btn_edit_profile);
        reviewsCard = view.findViewById(R.id.card_reviews);
        reviewsText = view.findViewById(R.id.text_reviews);
        settingsCard = view.findViewById(R.id.card_settings);
        settingsText = view.findViewById(R.id.text_settings);
        logoutCard = view.findViewById(R.id.card_logout);
        logoutText = view.findViewById(R.id.text_logout);
        friendsCard = view.findViewById(R.id.card_books);
        booksText = view.findViewById(R.id.text_books);
        followingCard = view.findViewById(R.id.card_albums);
        albumsText = view.findViewById(R.id.text_albums);

        //Declare special panel cards for higher permission users
        professorCard = view.findViewById(R.id.card_professor_panel);
        professorText = view.findViewById(R.id.text_professor_panel);
        adminCard = view.findViewById(R.id.card_admin_panel);
        adminText = view.findViewById(R.id.text_admin_panel);

        welcomeHeader.setText("Loading...");
        profileNameText.setText("Loading...");
        profileEmailText.setText("Loading...");
        profileSeparatorText.setText("  •  ");
        profileIdText.setText("Loading...");

        editProfileBtn.setText("View Profile");
        reviewsText.setText("⭐  My Reviews");
        settingsText.setText("⚙ Account Settings");
        logoutText.setText("➜]  Log Out");
        booksText.setText("Friends");
        albumsText.setText("Following");
        professorText.setText("Professor Panel");
        adminText.setText("Administrator Panel");

        editProfileBtn.setOnClickListener(v -> {
            if (userId != -1) {
                ((MainFeedActivity) requireActivity()).loadFragment(ViewProfileFragment.newInstance(userId));
            }
        });

        reviewsCard.setOnClickListener(v -> {
            // TODO: show user reviews
        });

        professorCard.setOnClickListener(v -> {
            if (userId != -1 && professorId != -1) {
                Intent intent = new Intent(getContext(), ProfessorPanelActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("PROFESSOR_ID", professorId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: Cannot open panel, User or Professor ID missing.", Toast.LENGTH_SHORT).show();
            }
        });

        adminCard.setOnClickListener(v -> {
            if (userId != -1 && adminId != -1) {
                Intent intent = new Intent(getContext(), AdminPanelActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("ADMIN_ID", adminId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: Cannot open panel, User or Professor ID missing.", Toast.LENGTH_SHORT).show();
            }
        });

        settingsCard.setOnClickListener(v -> {
            if (userId != -1) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: Cannot open settings, User ID missing.", Toast.LENGTH_SHORT).show();
            }
        });

        logoutCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
            getActivity().stopService(new Intent(getContext(), NotificationWebSocket.class));
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        friendsCard.setOnClickListener(v -> {
            ((MainFeedActivity) getActivity()).loadFragment(new FriendsFragment());
        });

        followingCard.setOnClickListener(v -> {
            ((MainFeedActivity) getActivity()).loadFragment(new FollowingFragment());
        });

        if (userId != -1) {
            fetchUserProfile(userId);
        } else {
            Toast.makeText(getContext(), "Error: User ID missing", Toast.LENGTH_SHORT).show();
            welcomeHeader.setText("Error loading profile");
        }

        return view;
    }

    private void fetchUserProfile(int userId) {
        ApiClient.get(getContext(), "/users/id/" + userId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String name = response.getString("name");
                    String email = response.getString("emailId");
                    int id = response.getInt("id");

                    welcomeHeader.setText(name + "'s Profile");
                    profileNameText.setText(name);
                    profileEmailText.setText(email);
                    profileIdText.setText("ID: " + id);

                    Log.d("ProfileFragment", "Profile loaded: " + name);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error parsing profile data", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e("ProfileFragment", "Profile load failed: " + message);
                welcomeHeader.setText("Profile Unavailable");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private void fetchProfessorIdByUserID(int userID) {
        ApiClient.get(UserProfileFragment.this.getContext(), "/professor/user/" + userID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                    try {
                        professorId = response.getInt("id");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                Log.d("Professor Check", "Is a professor, ID: " + professorId);

                if (professorId != -1) {
                    professorCard.setVisibility(View.VISIBLE);
                } else {
                    professorCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                Log.d("Professor Check", "Not a professor");
            }
        });
    }

    private void fetchAdminIdByUserID(int userID) {
        ApiClient.get(UserProfileFragment.this.getContext(), "/admin/user/" + userID, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    adminId = response.getInt("id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Log.d("Professor Check", "Is an admin, ID: " + adminId);

                if (adminId != -1) {
                    adminCard.setVisibility(View.VISIBLE);
                } else {
                    adminCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                Log.d("Admin Check", "Not an admin");
            }
        });
    }
}