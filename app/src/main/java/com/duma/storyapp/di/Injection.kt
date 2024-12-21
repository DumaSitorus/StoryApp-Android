package com.duma.storyapp.di

import android.content.Context
import com.duma.storyapp.data.api.ApiConfig
import com.duma.storyapp.data.database.StoryDatabase
import com.duma.storyapp.data.repository.UserRepository
import com.duma.storyapp.data.pref.UserPreference
import com.duma.storyapp.data.pref.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val db = StoryDatabase.getDatabase(context)
        return UserRepository.getInstance(db, pref, apiService)
    }
}
