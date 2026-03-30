package com.yurtdolap.app.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.presentation.designsystem.components.YurtPrimaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtSecondaryButton
import com.yurtdolap.app.presentation.designsystem.components.YurtTextField
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            onNavigateBack()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryLilac)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Profili Duzenle",
            style = MaterialTheme.typography.headlineSmall,
            color = TextDarkPurple,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hesap bilgilerini guncelleyebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDarkPurple.copy(alpha = 0.75f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Isim",
            style = MaterialTheme.typography.labelLarge,
            color = TextDarkPurple,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        YurtTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Ad Soyad"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Yurt",
            style = MaterialTheme.typography.labelLarge,
            color = TextDarkPurple,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        YurtTextField(
            value = state.dormitory,
            onValueChange = viewModel::onDormitoryChange,
            placeholder = "Yurt adi"
        )

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = state.errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        YurtPrimaryButton(
            text = if (state.isSaving) "Kaydediliyor..." else "Kaydet",
            onClick = { viewModel.saveProfile() },
            enabled = !state.isSaving
        )
        Spacer(modifier = Modifier.height(12.dp))
        YurtSecondaryButton(
            text = "Vazgec",
            onClick = onNavigateBack,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
