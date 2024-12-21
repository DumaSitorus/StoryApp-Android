package com.duma.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.duma.storyapp.data.api.ApiService
import com.duma.storyapp.data.api.response.AddStoryResponse
import com.duma.storyapp.data.api.response.ListStoryItem
import com.duma.storyapp.data.api.response.LoginResponse
import com.duma.storyapp.data.api.response.RegisterResponse
import com.duma.storyapp.data.api.response.StoriesResponse
import com.duma.storyapp.data.api.response.StoryDetailResponse
import com.duma.storyapp.data.database.StoryDatabase
import com.duma.storyapp.data.paging.RemoteMediator
import com.duma.storyapp.data.paging.StoryPagingSource
import com.duma.storyapp.data.pref.UserModel
import com.duma.storyapp.data.pref.UserPreference
import com.duma.storyapp.view.util.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UserRepository private constructor(
    private val storyDatabase: StoryDatabase,
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    private suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        wrapEspressoIdlingResource {
            userPreference.logout()
        }
    }

    suspend fun registerUser(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(userId: String, password: String): LoginResponse {
        val response = apiService.login(userId, password)
        wrapEspressoIdlingResource {
            if (response.error == false) {
                val user = UserModel(
                    userId = response.loginResult?.userId ?: "",
                    name = response.loginResult?.name ?: "",
                    token = response.loginResult?.token ?: "token_story_dicoding",
                    isLogin = true
                )
                saveSession(user)
            } else {
                throw Exception("Login failed: ${response.message}")
            }
            return response
        }
    }

    suspend fun getStories(): StoriesResponse {
        val response = apiService.getStories()
        return response
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getPaginatedStories(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            remoteMediator = RemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = { StoryPagingSource(apiService) }
        ).liveData
    }

    suspend fun getDetailStory(id: String): StoryDetailResponse {
        val response = apiService.getStoryDetail(id)
        return response
    }

    suspend fun getStoriesWithLocation(): StoriesResponse {
        return apiService.getStoriesWithLocation(location = 1)
    }

    suspend fun addNewStory(photo: File, description: String, latitude: Double?, longitude: Double?): AddStoryResponse {
        val requestImageFile = photo.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData(
            "photo",
            photo.name,
            requestImageFile
        )
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val latBody = latitude?.toString()?.toRequestBody("text/plain".toMediaType())
        val lonBody = longitude?.toString()?.toRequestBody("text/plain".toMediaType())

        return apiService.addNewStory(multipartBody, requestBody, latBody, lonBody)
    }

    companion object {
        fun getInstance(database: StoryDatabase, preference: UserPreference,  apiService: ApiService) =
            UserRepository(database, preference, apiService)
    }
}