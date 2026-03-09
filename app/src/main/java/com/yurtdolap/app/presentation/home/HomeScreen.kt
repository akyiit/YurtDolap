package com.yurtdolap.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Category
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    UIStateWrapper(
        state = uiState,
        onRetry = { viewModel.loadHomeData() }
    ) { state ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            HomeHeroSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CategoriesSection(
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onCategoryClick = { viewModel.selectCategory(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FeaturedProductsSection(
                products = state.featuredProducts,
                favoriteIds = state.favoriteIds,
                onProductClick = onNavigateToDetail,
                onFavoriteClick = { productId -> viewModel.toggleFavorite(productId) }
            )
        }
    }
}

@Composable
fun HomeHeroSection() {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = PrimaryLilac.copy(alpha = 0.1f),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 48.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
            Text(
                text = "Yurdunda ne arıyorsun?",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(24.dp))
            YurtTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Mini buzdolabı, kitap, kettle..."
            )
        }
    }
}

@Composable
fun CategoriesSection(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategoryClick: (String?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Kategoriler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 24.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                val isSelected = category.id == selectedCategoryId
                val bgColor = if (isSelected) PrimaryLilac else SurfaceLight
                val textColor = if (isSelected) BackgroundWhite else PrimaryLilac

                Box(
                    modifier = Modifier
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .clickable {
                            if (isSelected) {
                                onCategoryClick(null)
                            } else {
                                onCategoryClick(category.id)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedProductsSection(
    products: List<Product>,
    favoriteIds: List<String>,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Öne Çıkan İlanlar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Key parameter is explicitly provided
            items(products, key = { it.id }) { product ->
                ProductCard(
                    title = product.title,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    tag = product.tag,
                    isFavorite = favoriteIds.contains(product.id),
                    onFavoriteClick = { onFavoriteClick(product.id) },
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}
