package com.yurtdolap.app.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Category
import com.yurtdolap.app.domain.model.Product
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SecondaryLavender
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    UIStateWrapper(
        state = uiState,
        onRetry = { viewModel.loadHomeData() }
    ) { state ->
        val filteredProducts = remember(state.featuredProducts, searchQuery) {
            if (searchQuery.isBlank()) {
                state.featuredProducts
            } else {
                state.featuredProducts.filter {
                    it.title.contains(searchQuery.trim(), ignoreCase = true)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                HomeHeroSection(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    totalCount = state.featuredProducts.size,
                    filteredCount = filteredProducts.size
                )
            }

            items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                CategoriesSection(
                    categories = state.categories,
                    selectedCategoryId = state.selectedCategoryId,
                    onCategoryClick = { viewModel.selectCategory(it) }
                )
            }

            items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                FeaturedProductsHeader(productCount = filteredProducts.size)
            }

            if (filteredProducts.isEmpty()) {
                items(count = 1, span = { GridItemSpan(maxLineSpan) }) {
                    EmptyProductsState()
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        title = product.title,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        tag = product.tag,
                        isFavorite = state.favoriteIds.contains(product.id),
                        onFavoriteClick = { viewModel.toggleFavorite(product.id) },
                        onClick = { onNavigateToDetail(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeroSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    totalCount: Int,
    filteredCount: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryLilac, SecondaryLavender)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceLight.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SurfaceLight
                        )
                    }
                    Text(
                        text = "Yurtta ihtiyacin olan ne varsa burada.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hizli ara, kategoriyi sec ve en uygun ilana ulas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SurfaceLight.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                YurtTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = "Orn: mini buzdolabi, kitap, kettle"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(label = "Toplam", value = totalCount.toString())
                    InfoPill(label = "Gosterilen", value = filteredCount.toString())
                }
            }
        }
    }
}

@Composable
private fun InfoPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SurfaceLight.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SurfaceLight.copy(alpha = 0.9f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = SurfaceLight,
                fontWeight = FontWeight.Bold
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
    Column {
        Text(
            text = "Kategoriler",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDarkPurple
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            item {
                CategoryChip(
                    label = "Tum",
                    isSelected = selectedCategoryId == null,
                    onClick = { onCategoryClick(null) }
                )
            }

            items(categories, key = { it.id }) { category ->
                CategoryChip(
                    label = category.name,
                    isSelected = category.id == selectedCategoryId,
                    onClick = { onCategoryClick(category.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryLilac else SurfaceLight
    val textColor = if (isSelected) SurfaceLight else TextDarkPurple
    val border = if (isSelected) null else BorderStroke(1.dp, OutlineSoft)

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FeaturedProductsHeader(productCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "One Cikan Ilanlar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextDarkPurple
        )

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = PrimaryLilac.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$productCount ilan",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryLilac,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyProductsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceLight,
        border = BorderStroke(1.dp, OutlineSoft)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sonuc bulunamadi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Farkli bir arama veya kategori deneyebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDarkPurple.copy(alpha = 0.75f)
            )
        }
    }
}
