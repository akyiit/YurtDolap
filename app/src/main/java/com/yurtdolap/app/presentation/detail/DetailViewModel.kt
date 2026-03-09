package com.yurtdolap.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.domain.repository.ChatRepository
import com.yurtdolap.app.domain.repository.ProductRepository
import com.yurtdolap.app.domain.util.Resource
import com.yurtdolap.app.presentation.designsystem.components.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val productId: String? = savedStateHandle["productId"]

    private val _uiState = MutableStateFlow<UIState<Product>>(UIState.Idle)
    val uiState: StateFlow<UIState<Product>> = _uiState.asStateFlow()

    private val _navigateToChatEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val navigateToChatEvent = _navigateToChatEvent.asSharedFlow()

    init {
        loadProduct()
    }

    fun loadProduct() {
        if (productId == null) {
            _uiState.value = UIState.Error("Ürün bulunamadı")
            return
        }

        viewModelScope.launch {
            productRepository.getProductById(productId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = UIState.Loading
                    }
                    is Resource.Success -> {
                        if (resource.data != null) {
                            _uiState.value = UIState.Success(resource.data)
                        } else {
                            _uiState.value = UIState.Error("Ürün verisi boş.")
                        }
                    }
                    is Resource.Error -> {
                        _uiState.value = UIState.Error(resource.message ?: "Bilinmeyen bir hata oluştu")
                    }
                }
            }
        }
    }

    fun onMessageSellerClicked() {
        val currentProduct = (uiState.value as? UIState.Success)?.data ?: return

        viewModelScope.launch {
            val result = chatRepository.createOrGetChatRoom(
                otherUserId = currentProduct.sellerId,
                productId = currentProduct.id,
                productTitle = currentProduct.title,
                productImageUrl = currentProduct.imageUrl ?: ""
            )
            if (result is Resource.Success && result.data != null) {
                _navigateToChatEvent.emit(result.data)
            } else {
                // handle error via a UI state if needed, or Toast
            }
        }
    }
}
