package com.rohan.attendify_smart_attendance.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rohan.attendify_smart_attendance.dto.ErrorResponse
import com.rohan.attendify_smart_attendance.dto.LoginResponse
import com.rohan.attendify_smart_attendance.enums.UserRole
import com.rohan.attendify_smart_attendance.repository.AuthRepository
import com.rohan.attendify_smart_attendance.security.TokenManager
import com.rohan.attendify_smart_attendance.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {


    private val _authState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val authState: StateFlow<Resource<LoginResponse>?> = _authState

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        checkSession()
    }

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

                if (handleResponse(response)) {
                    response.body()?.let {
                        Log.i("bleuuid",it.bleUuid)
                        tokenManager.saveAccessToken(it.accessToken)
                        tokenManager.saveUserDetails(it.name?:"User",it.role,it.userId,it.bleUuid,it.refreshToken)
                    }
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }



    fun logout() {
        viewModelScope.launch {
            try {

               repository.performLogout()

            } catch (e: Exception) {

                Log.e("auth", "Non-fatal error clearing vault during logout", e)
            } finally {
                _sessionState.value = SessionState.Unauthenticated
            }
        }
    }


    private fun checkSession() {
        viewModelScope.launch {
            val token = tokenManager.getAccessTokenSync()
            val role = tokenManager.getUserRole()

            if (token != null && role != null) {
                val name = tokenManager.getUserName() ?: "User"
                val userId = tokenManager.getUserId() ?: ""
                _sessionState.value = SessionState.Authenticated(name, role, userId)
            } else {

                _sessionState.value = SessionState.Unauthenticated
            }
        }
    }



    private fun handleResponse(response: Response<LoginResponse>): Boolean {
        if (response.isSuccessful && response.body() != null) {
            _authState.value = Resource.Success(response.body()!!)
            return true
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
            return false
        }
    }
    private fun handleException(e: Exception) {
        when (e) {
            is IOException -> _authState.value = Resource.Error("Network Error: Check Internet")
            else -> _authState.value = Resource.Error("Error: ${e.localizedMessage}")
        }
    }
}


sealed class SessionState {
    object Loading : SessionState()
    data class Authenticated(val name: String, val role: String, val userId: String) : SessionState()
    object Unauthenticated : SessionState()
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // THE FIX: You must pass both the repository AND the tokenManager here!
            return AuthViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}