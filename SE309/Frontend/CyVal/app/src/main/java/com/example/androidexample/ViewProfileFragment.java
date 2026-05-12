package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileFragment extends Fragment {

    private static final String TAG = "ViewProfileFragment";

    private TextView headerText, nameText, majorText, emailText, bioText;
    private TextView hobbiesText, gradDateText, linkedInText, emptyText;
    private EditText editMajor, editBio, editHobbies, editGradDate, editLinkedIn;
    private CardView hobbiesCard, gradDateCard, linkedInCard;
    private Button editBtn;
    private RecyclerView postsRecycler;
    private TextView postsEmptyText;

    private int userId;
    private int currentUserId;
    private int profileId = -1;
    private boolean isEditing = false;

    // Keep current values so EditTexts are pre-filled correctly
    private String currentMajor = "", currentBio = "", currentHobbies = "",
                   currentGradDate = "", currentLinkedIn = "";

    public static ViewProfileFragment newInstance(int userId) {
        ViewProfileFragment fragment = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putInt("USER_ID", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);

        userId        = getArguments() != null ? getArguments().getInt("USER_ID", -1) : -1;
        currentUserId = requireActivity().getIntent().getIntExtra("USER_ID", -1);

        headerText    = view.findViewById(R.id.view_profile_header);
        nameText      = view.findViewById(R.id.view_profile_name);
        majorText     = view.findViewById(R.id.view_profile_major);
        emailText     = view.findViewById(R.id.view_profile_email);
        bioText       = view.findViewById(R.id.view_profile_bio);
        hobbiesCard   = view.findViewById(R.id.card_hobbies);
        hobbiesText   = view.findViewById(R.id.view_profile_hobbies);
        gradDateCard  = view.findViewById(R.id.card_grad_date);
        gradDateText  = view.findViewById(R.id.view_profile_grad_date);
        linkedInCard  = view.findViewById(R.id.card_linkedin);
        linkedInText  = view.findViewById(R.id.view_profile_linkedin);
        emptyText     = view.findViewById(R.id.view_profile_empty);
        editMajor     = view.findViewById(R.id.edit_major);
        editBio       = view.findViewById(R.id.edit_bio);
        editHobbies   = view.findViewById(R.id.edit_hobbies);
        editGradDate  = view.findViewById(R.id.edit_grad_date);
        editLinkedIn  = view.findViewById(R.id.edit_linkedin);
        editBtn       = view.findViewById(R.id.btn_edit_profile);
        postsRecycler = view.findViewById(R.id.profile_posts_recycler);
        postsEmptyText = view.findViewById(R.id.profile_posts_empty);

        headerText.setText("Profile");

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                ((MainFeedActivity) requireActivity()).loadFragment(new UserProfileFragment())
        );

        // Show edit button only when viewing your own profile
        if (userId == currentUserId) {
            editBtn.setVisibility(View.VISIBLE);
        }

        editBtn.setOnClickListener(v -> {
            if (isEditing) {
                saveProfile();
            } else {
                enterEditMode();
            }
        });

        if (userId != -1) {
            fetchUserInfo();
            fetchProfileDetails();
            loadUserPosts();
        }

        return view;
    }

    private void enterEditMode() {
        isEditing = true;
        editBtn.setText("Save");

        // Swap TextViews for EditTexts
        majorText.setVisibility(View.GONE);
        editMajor.setText(currentMajor);
        editMajor.setVisibility(View.VISIBLE);

        bioText.setVisibility(View.GONE);
        editBio.setText(currentBio);
        editBio.setVisibility(View.VISIBLE);

        // Always show detail cards in edit mode so empty fields can be filled in
        hobbiesCard.setVisibility(View.VISIBLE);
        hobbiesText.setVisibility(View.GONE);
        editHobbies.setText(currentHobbies);
        editHobbies.setVisibility(View.VISIBLE);

        gradDateCard.setVisibility(View.VISIBLE);
        gradDateText.setVisibility(View.GONE);
        editGradDate.setText(currentGradDate);
        editGradDate.setVisibility(View.VISIBLE);

        linkedInCard.setVisibility(View.VISIBLE);
        linkedInText.setVisibility(View.GONE);
        editLinkedIn.setText(currentLinkedIn);
        editLinkedIn.setVisibility(View.VISIBLE);

        emptyText.setVisibility(View.GONE);
    }

    private void exitEditMode() {
        isEditing = false;
        editBtn.setText("Edit Profile");

        editMajor.setVisibility(View.GONE);
        editBio.setVisibility(View.GONE);
        editHobbies.setVisibility(View.GONE);
        editGradDate.setVisibility(View.GONE);
        editLinkedIn.setVisibility(View.GONE);

        // Refresh displayed values
        if (!currentMajor.isEmpty()) {
            majorText.setText(currentMajor);
            majorText.setVisibility(View.VISIBLE);
        }
        if (!currentBio.isEmpty()) {
            bioText.setText(currentBio);
            bioText.setVisibility(View.VISIBLE);
        }
        if (!currentHobbies.isEmpty()) {
            hobbiesText.setText("Hobbies:  " + currentHobbies);
            hobbiesText.setVisibility(View.VISIBLE);
            hobbiesCard.setVisibility(View.VISIBLE);
        } else {
            hobbiesCard.setVisibility(View.GONE);
        }
        if (!currentGradDate.isEmpty()) {
            gradDateText.setText("Graduating:  " + currentGradDate);
            gradDateText.setVisibility(View.VISIBLE);
            gradDateCard.setVisibility(View.VISIBLE);
        } else {
            gradDateCard.setVisibility(View.GONE);
        }
        if (!currentLinkedIn.isEmpty()) {
            linkedInText.setText(currentLinkedIn);
            linkedInText.setVisibility(View.VISIBLE);
            linkedInCard.setVisibility(View.VISIBLE);
        } else {
            linkedInCard.setVisibility(View.GONE);
        }
    }

    private void saveProfile() {
        currentBio      = editBio.getText().toString().trim();
        currentMajor    = editMajor.getText().toString().trim();
        currentHobbies  = editHobbies.getText().toString().trim();
        currentGradDate = editGradDate.getText().toString().trim();
        currentLinkedIn = editLinkedIn.getText().toString().trim();

        DTOmodels.ProfileRequest request = new DTOmodels.ProfileRequest(
                currentBio, currentMajor, currentHobbies, currentGradDate, currentLinkedIn);

        editBtn.setEnabled(false);

        Api_Interface callback = new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try { profileId = response.getInt("profileId"); } catch (JSONException ignored) {}
                editBtn.setEnabled(true);
                exitEditMode();
                Toast.makeText(getContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                editBtn.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            }
        };

        if (profileId == -1) {
            ApiClient.post(getContext(), "/profile/userId/" + userId, request, callback);
        } else {
            ApiClient.put(getContext(), "/update-profile/" + profileId, request, callback);
        }
    }

    private void fetchUserInfo() {
        ApiClient.get(getContext(), "/users/id/" + userId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    String name  = response.getString("name");
                    String email = response.getString("emailId");
                    headerText.setText(name + "'s Profile");
                    nameText.setText(name);
                    emailText.setText(email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProfileDetails() {
        ApiClient.get(getContext(), "/profile/userId/" + userId, new Api_Interface() {
            @Override
            public void onSuccess(JSONObject response) {
                try { profileId = response.getInt("profileId"); } catch (JSONException ignored) {}

                currentBio      = response.optString("bio", "");
                currentMajor    = response.optString("major", "");
                currentHobbies  = response.optString("hobbies", "");
                currentGradDate = response.optString("gradDate", "");
                currentLinkedIn = response.optString("linkedInURL", "");

                boolean hasAny = false;

                if (!currentMajor.isEmpty()) {
                    majorText.setText(currentMajor);
                    majorText.setVisibility(View.VISIBLE);
                    hasAny = true;
                }
                if (!currentBio.isEmpty()) {
                    bioText.setText(currentBio);
                    bioText.setVisibility(View.VISIBLE);
                    hasAny = true;
                }
                if (!currentHobbies.isEmpty()) {
                    hobbiesText.setText("Hobbies:  " + currentHobbies);
                    hobbiesCard.setVisibility(View.VISIBLE);
                    hasAny = true;
                }
                if (!currentGradDate.isEmpty()) {
                    gradDateText.setText("Graduating:  " + currentGradDate);
                    gradDateCard.setVisibility(View.VISIBLE);
                    hasAny = true;
                }
                if (!currentLinkedIn.isEmpty()) {
                    linkedInText.setText(currentLinkedIn);
                    linkedInCard.setVisibility(View.VISIBLE);
                    hasAny = true;
                }

                if (!hasAny) emptyText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String message) {
                // No profile yet — empty state, edit mode will POST
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    // =========================================================================
    // User posts
    // =========================================================================

    /**
     * Fetches all published posts by {@link #userId} from {@code GET /users/{id}/posts}
     * and renders them using {@link FeedAdapter} in the posts RecyclerView.
     * Tapping a post card opens {@link PostViewActivity} with the full post data.
     */
    private void loadUserPosts() {
        ApiClient.getArray(getContext(), "/users/" + userId + "/posts",
                new Api_Array_Interface() {
                    @Override
                    public void onSuccess(JSONArray response) {
                        Gson gson = new Gson();
                        List<DTOmodels.FeedItem> items = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                DTOmodels.FeedPost post = gson.fromJson(
                                        response.getJSONObject(i).toString(),
                                        DTOmodels.FeedPost.class);
                                items.add(DTOmodels.FeedItem.fromPost(post));
                            } catch (JSONException e) {
                                Log.e(TAG, "Parse error in post list: " + e.getMessage());
                            }
                        }

                        if (items.isEmpty()) {
                            postsEmptyText.setVisibility(View.VISIBLE);
                        } else {
                            // Non-scrolling layout manager: the outer NestedScrollView
                        // owns the scroll gesture, so RecyclerView expands to
                        // its full height and every post is rendered.
                        postsRecycler.setLayoutManager(
                                new LinearLayoutManager(getContext()) {
                                    @Override
                                    public boolean canScrollVertically() { return false; }
                                });
                            postsRecycler.setAdapter(new FeedAdapter(getContext(), items,
                                    new FeedAdapter.OnItemClickListener() {
                                        @Override
                                        public void onPostClick(DTOmodels.FeedPost post) {
                                            Intent intent = new Intent(
                                                    requireActivity(), PostViewActivity.class);
                                            intent.putExtra("postId",       post.id);
                                            intent.putExtra("reviewId",     post.reviewId);
                                            intent.putExtra("authorId",     post.authorId);
                                            intent.putExtra("authorName",   post.authorName);
                                            intent.putExtra("caption",      post.caption);
                                            intent.putExtra("mediaTitle",   post.mediaTitle);
                                            intent.putExtra("mediaType",    post.mediaType);
                                            intent.putExtra("rating",       post.rating);
                                            intent.putExtra("likeCount",    post.likeCount);
                                            intent.putExtra("commentCount", post.commentCount);
                                            intent.putExtra("publishedAt",  post.publishedAt);
                                            intent.putExtra("USER_ID",      currentUserId);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onMediaClick(DTOmodels.FeedMedia media) {}
                                    }));
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "Could not load posts: " + message);
                        if (getContext() != null)
                            postsEmptyText.setVisibility(View.VISIBLE);
                    }
                });
    }
}
