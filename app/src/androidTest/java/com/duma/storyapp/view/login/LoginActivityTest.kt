package com.duma.storyapp.view.login

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
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duma.storyapp.JsonConverter
import com.duma.storyapp.R
import com.duma.storyapp.data.api.ApiConfig
import com.duma.storyapp.view.util.EspressoIdlingResource
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    private val mockWebServer = MockWebServer()

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.BASE_URL = "http://127.0.0.1:8080/"
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun loginSuccessTest() {
        val successResponse = JsonConverter.readStringFromFile("success_response.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(successResponse))

        onView(withId(R.id.ed_login_email)).perform(typeText("gom@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("gomgom12"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())
        onView(withText("Yeah!"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun loginFailureTest() {
        val errorResponse = JsonConverter.readStringFromFile("failure_response.json")
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setBody(errorResponse))

        onView(withId(R.id.ed_login_email)).perform(typeText("wrong@domain.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText("wrongpassword"), closeSoftKeyboard())
        onView(withId(R.id.loginButton)).perform(click())
        onView(withText("Opss!"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}