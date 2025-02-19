package com.duma.storyapp.view.main

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duma.storyapp.R
import com.duma.storyapp.view.login.LoginActivity
import com.duma.storyapp.view.util.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun testLogoutAndCheckLoginRegisterButtons() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.loginButton)).perform(click())
        onView(withId(R.id.ed_login_email)).perform(typeText("gom@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("gomgom12"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())
        onView(withText("Yeah!")).inRoot(isDialog()).perform(click())

        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.logoutButton)).perform(click())
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()))
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()))
    }
}