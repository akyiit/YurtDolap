package com.yurtdolap.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthFormState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val city: String = "",
    val dormitory: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<Unit>>(UIState.Idle)
    val uiState: StateFlow<UIState<Unit>> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    fun onEmailChange(email: String) {
        _formState.value = _formState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _formState.value = _formState.value.copy(password = password)
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun onCityChange(city: String) {
        _formState.value = _formState.value.copy(city = city)
    }

    fun onDormitoryChange(dorm: String) {
        _formState.value = _formState.value.copy(dormitory = dorm)
    }

    fun login() {
        val email = _formState.value.email
        val password = _formState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UIState.Error("E-posta ve şifre boş olamaz.")
            return
        }

        _uiState.value = UIState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmailAndPassword(email, password)
            if (result is Resource.Success) {
                _uiState.value = UIState.Success(Unit)
            } else {
                _uiState.value = UIState.Error(result.message ?: "Giriş başarısız oldu.")
            }
        }
    }

    fun register() {
        val state = _formState.value
        
        if (state.email.isBlank() || state.password.isBlank() || state.name.isBlank() || state.city.isBlank() || state.dormitory.isBlank()) {
            _uiState.value = UIState.Error("Tüm alanları doldurunuz.")
            return
        }
        
        val emailLower = state.email.trim().lowercase()
        if (!(emailLower.endsWith(".edu.tr") || emailLower.endsWith(".edu"))) {
            _uiState.value = UIState.Error("Sadece .edu.tr veya .edu uzantılı öğrenci e-posta adresleriyle kayıt olabilirsiniz.")
            return
        }
        
        if (state.password.length < 6) {
             _uiState.value = UIState.Error("Şifre en az 6 karakter olmalıdır.")
             return
        }

        _uiState.value = UIState.Loading
        viewModelScope.launch {
            val result = authRepository.createUserWithEmailAndPassword(
                email = state.email.trim(),
                password = state.password,
                name = state.name,
                city = state.city,
                dormitory = state.dormitory
            )
            if (result is Resource.Success) {
                _uiState.value = UIState.Success(Unit)
            } else {
                _uiState.value = UIState.Error(result.message ?: "Kayıt işlemi başarısız oldu.")
            }
        }
    }

    fun resetState() {
        _uiState.value = UIState.Idle
    }
}
