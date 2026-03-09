package com.yurtdolap.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.presentation.designsystem.components.ProductCard
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun ProfileScreen(
    onNavigateToEdit: (String) -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var productToDelete by remember { mutableStateOf<String?>(null) }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(text = "Silmeyi Onayla", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = { Text("Bu ilanı gerçekten kalıcı olarak silmek istiyor musunuz?") },
            confirmButton = {
                TextButton(onClick = {
                    productToDelete?.let { viewModel.deleteProduct(it) }
                    productToDelete = null
                }) {
                    Text("Sil", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("İptal", color = PrimaryLilac)
                }
            }
        )
    }

    UIStateWrapper(
        state = uiState,
        onRetry = { viewModel.loadProfile() }
    ) { profile ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            ProfileHeaderBlock(
                name = profile.name, 
                dormitory = profile.dormitory,
                onSignOutClick = {
                    viewModel.signOut()
                    onSignOut()
                }
            )
            
            Divider(color = OutlineSoft, modifier = Modifier.padding(vertical = 16.dp))
            
            Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize()) {
                Text(
                    text = "Aktif İlanlarım",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = TextDarkPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(profile.activeListings, key = { it.id }) { product ->
                        ProductCard(
                            title = product.title,
                            price = product.price,
                            imageUrl = product.imageUrl,
                            tag = product.tag,
                            onClick = { /* Navigate to detail */ },
                            onDeleteClick = { productToDelete = product.id },
                            onEditClick = { onNavigateToEdit(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderBlock(
    name: String, 
    dormitory: String,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryLilac.copy(alpha = 0.05f))
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryLilac.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = PrimaryLilac
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.displayLarge,
            color = TextDarkPurple,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = dormitory,
            style = MaterialTheme.typography.bodyLarge,
            color = TextDarkPurple.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        YurtSecondaryButton(text = "Profili Düzenle", onClick = { /* TODO */ })
        Spacer(modifier = Modifier.height(16.dp))
        YurtSecondaryButton(
            text = "Çıkış Yap", 
            onClick = onSignOutClick
        )
    }
}
