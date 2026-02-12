package com.rohan.attendify_smart_attendance.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rohan.attendify_smart_attendance.api.RetrofitInstance
import com.rohan.attendify_smart_attendance.dto.ErrorResponse
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.entity.UserRole
import com.rohan.attendify_smart_attendance.repository.AuthRepository
import com.rohan.attendify_smart_attendance.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository(RetrofitInstance)

    private val _authState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val authState: StateFlow<Resource<LoginResponse>?> = _authState

    // LOGIN FUNCTION (With Validation)
    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Please enter email and password")
            return
        }
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            try {
                val response = repository.login(email, pass)
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // SIGNUP FUNCTION
    fun signupUser(
        role: String, name: String, email: String, pass: String) {

        //  Validation
        if (name.isBlank()) {
            _authState.value = Resource.Error("Name is required")
            return
        }
        if (email.isBlank()) {
            _authState.value = Resource.Error("Email is required")
            return
        }
        if (pass.length < 6 || pass.isBlank()) {
            _authState.value = Resource.Error("Password must be at least 6 chars")
            return
        }

        // connect to Server
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            try {
                val response = if (role.equals(UserRole.STUDENT.name, ignoreCase = true)) {
                    repository.signupStudent(name, email, pass)
                } else {
                    repository.signupTeacher(name, email, pass)
                }
                handleResponse(response)
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }


    private fun handleResponse(response: Response<LoginResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _authState.value = Resource.Success(response.body()!!)
        } else {
            val cleanErrorMessage = try {

                val errorBodyReader = response.errorBody()?.charStream()
                //  convert JSON -> ErrorResponse object
                val errorObj = Gson().fromJson(errorBodyReader, ErrorResponse::class.java)
                errorObj.message
            } catch (e: Exception) {
                "Error: ${response.message()}"
            }
            _authState.value = Resource.Error(cleanErrorMessage)
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is IOException -> _authState.value = Resource.Error("Network Error: Check Internet")
            else -> _authState.value = Resource.Error("Error: ${e.localizedMessage}")
        }
    }
}