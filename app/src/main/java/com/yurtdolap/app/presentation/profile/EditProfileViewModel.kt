package com.yurtdolap.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.repository.UserRepository
import com.yurtdolap.app.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileState(
    val name: String = "",
    val dormitory: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state: StateFlow<EditProfileState> = _state.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = userRepository.getUserProfile()) {
                is Resource.Success -> {
                    val profile = result.data
                    _state.value = _state.value.copy(
                        name = profile?.name.orEmpty(),
                        dormitory = profile?.dormitory.orEmpty(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Profil bilgileri yuklenemedi"
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, errorMessage = null)
    }

    fun onDormitoryChange(value: String) {
        _state.value = _state.value.copy(dormitory = value, errorMessage = null)
    }

    fun saveProfile() {
        val current = _state.value
        if (current.name.isBlank() || current.dormitory.isBlank()) {
            _state.value = current.copy(errorMessage = "Isim ve yurt bilgisi zorunlu")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isSaving = true, errorMessage = null)
            when (val result = userRepository.updateUserProfile(current.name, current.dormitory)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isSaving = false, errorMessage = null)
                    _saveSuccess.emit(Unit)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        errorMessage = result.message ?: "Profil guncellenemedi"
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(isSaving = true)
                }
            }
        }
    }
}
