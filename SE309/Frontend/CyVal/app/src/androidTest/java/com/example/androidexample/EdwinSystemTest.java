package com.example.androidexample;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.assertion.ViewAssertions;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.core.content.res.TypedArrayUtils.getText;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;
import static org.hamcrest.CoreMatchers.anything;

import android.view.View;

import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class EdwinSystemTest {
    private int userId = 643;
    private int adminUserId = 638;
    private int adminId = 74;
    private int professorUserID = 644;
    private int professorId = 55;
    private int groupId = 5;
    private int postId = 172;

    private int bookId = 20;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);
//    @Rule
//    public ActivityScenarioRule<ChatActivity> chatRule
//            = new ActivityScenarioRule<>(ChatActivity.class);

    @Test
    public void testAdministratorTools() {

        //Log into static test admin account
        onView(withId(R.id.loginBtn))
                .perform(click());
        onView(withId(R.id.admin_button))
                .perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTestAdmin@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Navigate to profile
        onView(withId(R.id.nav_profile))
                .perform(click());
        // card_admin_panel starts GONE; fetchAdminIdByUserID() makes it VISIBLE after network response
        try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }

        //Navigate to Administrator Panel
        onView(withId(R.id.card_admin_panel))
                .perform(scrollTo(), click());
        try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }

        //Navigate to User Manager
        onView(withText("All Users")).perform(click());
        //Run experiments on static test user account
        onView(withId(R.id.userEdt)).perform(replaceText("edwinSystemTestUser"), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        // idEdt/passwordEdt are INVISIBLE until makeVisible() fires on search success
        try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.passwordEdt)).perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.updateBtn)).perform(click());
        try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
        // pressBack() pops to the EXISTING AdminPanelActivity (already rendered, "Professors" visible)
        // instead of creating a new instance via returnBtn that may not render on a deep back stack
        pressBack();
        // AdminPanelActivity reloads on resume -- 2 s gives it time to re-draw its buttons
        try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }

        //Navigate to Professor Manager
        onView(withText("Professors")).perform(click());
        //Run experiments on static test professor account
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.userEdt)).perform(replaceText(String.valueOf(professorId)), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        // classesAddEdt is invisible until professor data loads from server
        try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.classesAddEdt)).perform(replaceText("PHYS2320"), closeSoftKeyboard());
        onView(withId(R.id.addClassBtn)).perform(click());
        onView(withId(R.id.removeClassBtn)).perform(click());
        try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.returnBtn)).perform(click());

        //Navigate to Admins and leave (just for method coverage atp)
        onView(withText("Admins")).perform(click());
        try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.returnBtn)).perform(click());

        //Navigate to Reviews and search (don't delete tho)
        onView(withText("Reviews")).perform(click());
        onView(withId(R.id.findPostEdt)).perform(replaceText(String.valueOf(postId)), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.postScroller)).perform(swipeUp());
        onView(withId(R.id.returnBtn)).perform(click());

        //Navigate to Groups and search (don't delete tho)
        onView(withText("Groups")).perform(click());
        onView(withId(R.id.findGroupEdt)).perform(replaceText(String.valueOf(groupId)), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.returnBtn)).perform(click());

        //Navigate to Media (Books specifically) and search (don't delete tho)
        onView(withText("Books")).perform(click());
        onView(withText("Manage")).perform(click());
        onView(withId(R.id.searchEdt)).perform(replaceText(String.valueOf(bookId)), closeSoftKeyboard());
        onView(withId(R.id.searchBtn)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        onView(withId(R.id.authorEdt)).perform(clearText());
        onView(withId(R.id.authorEdt)).perform(replaceText("Typo"), closeSoftKeyboard());
        onView(withId(R.id.updateBtn)).perform(click());
        onView(withId(R.id.authorEdt)).perform(clearText());
        onView(withId(R.id.authorEdt)).perform(replaceText("Harper Lee"), closeSoftKeyboard());
        onView(withId(R.id.updateBtn)).perform(click());
        onView(withId(R.id.returnBtn)).perform(click());
    }

    @Test
    public void testProfessorTools() {

        //Log into static test professor account
        onView(withId(R.id.loginBtn))
                .perform(click());
        onView(withId(R.id.professor_button))
                .perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTestProfessor@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Navigate to profile
        onView(withId(R.id.nav_profile))
                .perform(click());
        // card_professor_panel starts GONE; wait for API call to make it visible
        try { Thread.sleep(2000); } catch (InterruptedException e) { throw new RuntimeException(e); }

        //Navigate to Professor Panel
        onView(withId(R.id.card_professor_panel))
                .perform(scrollTo(), click());

        //Create new group
        onView(withId(R.id.nameInput))
                .perform(replaceText("edwinSystemsDeleteGroup"), closeSoftKeyboard());
        onView(withId(R.id.classInput))
                .perform(replaceText("COMS309"), closeSoftKeyboard());
        onView(withId(R.id.createButton))
                .perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }

        //Enter FIRST group and run experiments
        onData(anything())
                .inAdapterView(withId(R.id.groupList))
                .atPosition(0)
                .perform(click());
        onView(withId(R.id.student_name_input))
                .perform(replaceText("edwinSystemTestUser"), closeSoftKeyboard());
        onView(withId(R.id.student_remove_button))
                .perform(click());
        onView(withId(R.id.student_add_button))
                .perform(click());
        onView(withId(R.id.group_name_input))
                .perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.update_group_button))
                .perform(click());
        onView(withId(R.id.group_name_input))
                .perform(clearText());
        onView(withId(R.id.group_name_input))
                .perform(replaceText("edwinSystemsTestGroup"), closeSoftKeyboard());
        onView(withId(R.id.update_group_button))
                .perform(click());

        //Return to dashboard and delete new group we made earlier
        onView(withId(R.id.return_button))
                .perform(click());
        try { Thread.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onData(anything())
                .inAdapterView(withId(R.id.groupList))
                .atPosition(1)
                .perform(click());
        try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
        onView(withId(R.id.delete_group_button))
                .perform(click());
    }

    @Test
    public void testGroupChat() throws InterruptedException {
        onView(withId(R.id.loginBtn))
                .perform(click());
        onView(withId(R.id.student_button))
                .perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTest@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());

        Thread.sleep(3000);

        onView(withId(R.id.nav_groups))
                .perform(click());
        Thread.sleep(2000); // wait for group list to load from server
        onData(anything())
                .inAdapterView(withId(R.id.group_list))
                .atPosition(1)
                .perform(click());
        Thread.sleep(2000); // wait for WebSocket connection to open
        //Should be inside ChatActivity now. Now we will send a message and check to see if it sent.

        //Generate a random message in the form of a string of numbers.
        Random rand = new Random();

        //get a random integer between 0 and 100000000, turn it into a string
        String message = Integer.toString(rand.nextInt(100000001));

        onView(withId(R.id.message_edit))
                .perform(replaceText(message), closeSoftKeyboard());
        onView(withId(R.id.send_button))
                .perform(click());
        onView(withId(R.id.return_button)).perform(click());

//        //Then, make sure the last message will be visible and check to see if the message is there.
//        onView(withId(R.id.message_display))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//                .check(ViewAssertions.matches(ViewMatchers.isDescendantOfA(ViewMatchers.withId(R.id.message_display))))
//                .check(ViewAssertions.matches(ViewMatchers.withText(message)));

    }

    @Test
    public void sendAndReceiveFriendRequest() throws InterruptedException {
        onView(withId(R.id.loginBtn))
                .perform(click());
        onView(withId(R.id.student_button))
                .perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTest@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());

        Thread.sleep(3000);

        //Navigate to profile
        onView(withId(R.id.nav_profile))
                .perform(click());
        Thread.sleep(1000);
        //Navigate to Friends
        onView(withId(R.id.card_books))
                .perform(click());
        onView(withId(R.id.requests_button))
                .perform(click());
        onView(withId(R.id.friend_id_edit))
                .perform(replaceText(String.valueOf(professorUserID)), closeSoftKeyboard());
        onView(withId(R.id.new_request_button))
                .perform(click());

        Thread.sleep(500);

        onData(anything())
                .inAdapterView(withId(R.id.outgoing_request_list))
                .atPosition(0)
                .perform(click());
        onView(withText("Unsend Request")).perform(click());
        onView(withId(R.id.new_request_button))
                .perform(click());
        onView(withId(R.id.return_button)).perform(click());
        onView(withId(R.id.return_button)).perform(click());

        onView(withId(R.id.wholeThing)).perform(swipeUp());
        onView(withId(R.id.card_logout)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.loginBtn))
                .perform(click());
        onView(withId(R.id.student_button))
                .perform(click());
        onView(withId(R.id.login_username_edit))
                .perform(replaceText("edwinSystemTestProfessor@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit))
                .perform(replaceText("Hello22!"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());

        Thread.sleep(3000);

        //Navigate to profile
        onView(withId(R.id.nav_profile))
                .perform(click());
        Thread.sleep(1000);
        //Navigate to Friends
        onView(withId(R.id.card_books))
                .perform(click());
        onView(withId(R.id.requests_button))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.incoming_request_list))
                .atPosition(0)
                .perform(click());
        onView(withText("Decline")).perform(click());

        Thread.sleep(500);

        onData(anything())
                .inAdapterView(withId(R.id.incoming_request_list))
                .atPosition(0)
                .perform(click());
        onView(withText("Accept")).perform(click());

        onView(withId(R.id.return_button)).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.friends_list))
                .atPosition(0)
                .perform(longClick());
        onView(withText("Unfriend")).perform(click());
    }
}