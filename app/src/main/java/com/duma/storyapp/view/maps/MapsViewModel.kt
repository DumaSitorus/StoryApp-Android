package com.duma.storyapp.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duma.storyapp.data.api.response.ListStoryItem
import com.duma.storyapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _listStory = MutableLiveData<List<ListStoryItem>>()
    val listStory: LiveData<List<ListStoryItem>> get() = _listStory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchStoriesWithLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getStoriesWithLocation()
                if (!response.error) {
                    _listStory.value = response.listStory
                } else {
                    _message.value = response.message
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Something went wrong"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _message.value = null
    }
}