package com.example.androidexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainFeedActivity extends AppCompatActivity {

    private ImageView feedIcon, groupsIcon, postIcon, notifsIcon, profileIcon;
    private TextView feedText, groupsText, postText, notifsText, profileText;
    private LinearLayout nav_groups;
    private View notifDot;
    private BroadcastReceiver mainNotificationReceiver;

    private String activeTab = "feed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);

        feedIcon = findViewById(R.id.nav_feed_icon);
        groupsIcon = findViewById(R.id.nav_groups_icon);
        postIcon = findViewById(R.id.nav_post_icon);
        notifsIcon = findViewById(R.id.nav_notifs_icon);
        profileIcon = findViewById(R.id.nav_profile_icon);

        feedText = findViewById(R.id.nav_feed_text);
        groupsText = findViewById(R.id.nav_groups_text);
        postText = findViewById(R.id.nav_post_text);
        notifsText = findViewById(R.id.nav_notifs_text);
        profileText = findViewById(R.id.nav_profile_text);
        notifDot = findViewById(R.id.notif_dot);
        nav_groups = findViewById(R.id.nav_groups);

        mainNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // If we're not currently on the notifications tab, show the dot
                if (notifDot != null && !"notifs".equals(activeTab)) {
                    notifDot.setVisibility(View.VISIBLE);
                }
            }
        };

        if (savedInstanceState == null) {
            loadFragment(new FeedFragment());
            setActiveTab("feed");
        }

        findViewById(R.id.nav_feed).setOnClickListener(v -> {
            loadFragment(new FeedFragment());
            setActiveTab("feed");
        });

        findViewById(R.id.nav_groups).setOnClickListener(v -> {
            loadFragment(new GroupsFragment());
            setActiveTab("groups");
        });

        findViewById(R.id.nav_post).setOnClickListener(v -> {
            setActiveTab("post");
            CreatePostDialogFragment dialog = new CreatePostDialogFragment();
            dialog.setOnPostPublishedListener(() -> {
                loadFragment(new FeedFragment());
                setActiveTab("feed");
            });
            dialog.setOnDismissListener(() -> {
                loadFragment(new FeedFragment());
                setActiveTab("feed");
            });
            dialog.show(getSupportFragmentManager(), "create_post");
        });

        findViewById(R.id.nav_notifs).setOnClickListener(v -> {
            loadFragment(new NotificationsFragment());
            setActiveTab("notifs");
            if (notifDot != null) {
                notifDot.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            loadFragment(new UserProfileFragment());
            setActiveTab("profile");
        });

        Intent notifService = new Intent(this, NotificationWebSocket.class);
        notifService.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1));
        startService(notifService);

        if (getIntent().getBooleanExtra("openNotifs", false)) {
            loadFragment(new NotificationsFragment());
            setActiveTab("notifs");
        } else if (getIntent().getBooleanExtra("openFriends", false)) {
            loadFragment(new FriendsFragment());
            setActiveTab("profile");
        } else if (getIntent().getBooleanExtra("openFollowing", false)) {
            loadFragment(new FollowingFragment());
            setActiveTab("profile");
        } else if (getIntent().getIntExtra("openViewProfile", -1) != -1) {
            int uid = getIntent().getIntExtra("openViewProfile", -1);
            loadFragment(ViewProfileFragment.newInstance(uid));
            setActiveTab("profile");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.androidexample.NEW_NOTIFICATION");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mainNotificationReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mainNotificationReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mainNotificationReceiver != null) {
            unregisterReceiver(mainNotificationReceiver);
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        // Preserve USER_ID from the original intent — other fragments (e.g. CreatePost)
        // read it from getActivity().getIntent() and must never see -1.
        if (!intent.hasExtra("USER_ID")) {
            intent.putExtra("USER_ID", getIntent().getIntExtra("USER_ID", -1));
        }
        setIntent(intent);

        if (intent.getBooleanExtra("openFriends", false)) {
            loadFragment(new FriendsFragment());
            setActiveTab("profile");
        } else if (intent.getBooleanExtra("openFollowing", false)) {
            loadFragment(new FollowingFragment());
            setActiveTab("profile");
        } else if (intent.getIntExtra("openViewProfile", -1) != -1) {
            int uid = intent.getIntExtra("openViewProfile", -1);
            loadFragment(ViewProfileFragment.newInstance(uid));
            setActiveTab("profile");
        }
    }

    protected void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void setActiveTab(String tab) {
        int inactive = android.graphics.Color.parseColor("#000000");
        int active = android.graphics.Color.parseColor("#E53935");

        feedIcon.setColorFilter(inactive);
        groupsIcon.setColorFilter(inactive);
        postIcon.setColorFilter(inactive);
        notifsIcon.setColorFilter(inactive);
        profileIcon.setColorFilter(inactive);

        feedText.setTextColor(inactive);
        groupsText.setTextColor(inactive);
        postText.setTextColor(inactive);
        notifsText.setTextColor(inactive);
        profileText.setTextColor(inactive);

        feedText.setTypeface(null, android.graphics.Typeface.NORMAL);
        groupsText.setTypeface(null, android.graphics.Typeface.NORMAL);
        postText.setTypeface(null, android.graphics.Typeface.NORMAL);
        notifsText.setTypeface(null, android.graphics.Typeface.NORMAL);
        profileText.setTypeface(null, android.graphics.Typeface.NORMAL);

        switch (tab) {
            case "feed":
                activeTab = "feed";
                feedIcon.setColorFilter(active);
                feedText.setTextColor(active);
                feedText.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "groups":
                activeTab = "groups";
                groupsIcon.setColorFilter(active);
                groupsText.setTextColor(active);
                groupsText.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "post":
                activeTab = "post";
                postIcon.setColorFilter(active);
                postText.setTextColor(active);
                postText.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "notifs":
                activeTab = "notifs";
                notifsIcon.setColorFilter(active);
                notifsText.setTextColor(active);
                notifsText.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case "profile":
                activeTab = "profile";
                profileIcon.setColorFilter(active);
                profileText.setTextColor(active);
                profileText.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }
}