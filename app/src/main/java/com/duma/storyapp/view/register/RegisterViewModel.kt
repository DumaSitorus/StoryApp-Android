package com.duma.storyapp.view.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duma.storyapp.data.api.response.ErrorResponse
import com.duma.storyapp.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _isError = MutableLiveData<Boolean?>()
    val isError: LiveData<Boolean?> get() = _isError

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = userRepository.registerUser(name, email, password)
                _isError.value = response.error
                _message.value = response.message
            } catch (e: Exception) {
                if (e is HttpException) {
                    _isError.value = true
                    val jsonInString = e.response()?.errorBody()?.string()
                    val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                    val errorMessage = errorBody.message
                    _message.value = errorMessage
                } else {
                    _isError.value = true
                    _message.value = "error. Try again!"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}