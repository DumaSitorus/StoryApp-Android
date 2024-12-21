package com.duma.storyapp.view.liststory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.duma.storyapp.data.api.response.ErrorResponse
import com.duma.storyapp.data.api.response.ListStoryItem
import com.duma.storyapp.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ListStoryViewModel(private val repository: UserRepository) : ViewModel() {
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _listStory = MutableLiveData<List<ListStoryItem>>()

    val stories: LiveData<PagingData<ListStoryItem>> =
        repository.getPaginatedStories().cachedIn(viewModelScope)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getStories() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getStories()
                _listStory.value = response.listStory
                _message.value = response.message
            } catch (e: Exception) {
                if (e is HttpException) {
                    _listStory.value = emptyList()
                    val jsonInString = e.response()?.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    val errorMessage = errorBody.message
                    _message.value = errorMessage
                } else {
                    _listStory.value = emptyList()
                    _message.value = "error. Try again!"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}