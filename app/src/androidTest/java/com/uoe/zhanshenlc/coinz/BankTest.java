package com.uoe.zhanshenlc.coinz;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;


import com.google.firebase.auth.FirebaseAuth;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BankTest {

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

        // Sign in finish

        onView(withId(R.id.sidebar_map)).perform(click());
        onView(withText("Bank")).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.quid_bank)).check(matches(withText("QUID to GOLD")));
        onView(withId(R.id.quidRate_bank)).check(matches(not(withText(""))));

        onView(withId(R.id.quidBtn_bank)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.toGoldSwitch_bank)).perform(click());
        onView(withId(R.id.fromGoldSwitch_bank)).check(matches(isChecked()));
        onView(withId(R.id.balance_bank)).check(matches(not(withText(""))));

        onView(withId(R.id.rateHistory_bank)).perform(click());
        onView(withId(R.id.inputYear_rateHistory)).perform(replaceText("2018"));
        onView(withId(R.id.inputMonth_rateHistory)).perform(replaceText("12"));
        onView(withId(R.id.inputDay_rateHistory)).perform(replaceText("13"));
        onView(withId(R.id.searchBtn_rateHistory)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.quidRate_rateHistory)).check(matches(withText("33.707551225910905")));

        pressBack();

        pressBack();

        // Sign out

        onView(withId(R.id.sidebar_map)).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("Sign Out")).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
