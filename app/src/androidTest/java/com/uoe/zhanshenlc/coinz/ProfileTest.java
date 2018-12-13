package com.uoe.zhanshenlc.coinz;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ProfileTest {

    @BeforeClass
    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>(SplashActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule .grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void splash() {
        mActivityTestRule.launchActivity(new Intent());

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.loginLink_register)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.inputEmail_login)).perform(replaceText("2856483627@qq.com"));
        onView(withId(R.id.inputPassword_login)).perform(replaceText("123456"));
        onView(withId(R.id.btn_login)).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.sidebar_map)).perform(click());
        onView(withText("My Profile")).perform(click());

        onView(withId(R.id.name_profile)).check(matches(withText("Hachikuji")));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.inputName_profile)).perform(replaceText("Senjougahara"));
        onView(withId(R.id.nameBtn_profile)).perform(click());

        onView(withId(R.id.name_profile)).check(matches(withText("Senjougahara")));

        Map<String, Object> result = new HashMap<>();
        result.put("name", "Hachikuji");
        FirebaseFirestore.getInstance().collection("users")
                .document("2856483627@qq.com").update(result);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();

        onView(withId(R.id.sidebar_map)).perform(click());
        onView(withText("Sign Out")).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
