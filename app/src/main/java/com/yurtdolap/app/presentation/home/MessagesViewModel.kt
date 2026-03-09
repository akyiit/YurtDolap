package com.yurtdolap.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIState<List<ChatRoom>>>(UIState.Loading)
    val uiState: StateFlow<UIState<List<ChatRoom>>> = _uiState.asStateFlow()

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        viewModelScope.launch {
            chatRepository.getUserChatRooms().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.value = UIState.Success(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Sohbetler yüklenemedi")
                    }
                    else -> {}
                }
            }
        }
    }
}
