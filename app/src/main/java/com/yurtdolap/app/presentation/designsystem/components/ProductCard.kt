package com.yurtdolap.app.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.OutlineSoft
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun ProductCard(
    title: String,
    price: String,
    imageUrl: String?,
    tag: String,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = cardShape)
            .border(width = 1.dp, color = OutlineSoft.copy(alpha = 0.7f), shape = cardShape)
            .clickable { onClick() },
        color = SurfaceLight,
        shape = cardShape
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .background(OutlineSoft.copy(alpha = 0.45f))
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Product image: $title",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(152.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Text(
                        text = "Gorsel yok",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextDarkPurple.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                            )
                        )
                )

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PrimaryLilac,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = tag,
                        color = SurfaceLight,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onEditClick != null) {
                        RoundedActionButton(
                            onClick = onEditClick,
                            icon = Icons.Default.Edit,
                            contentDescription = "Duzenle",
                            tint = PrimaryLilac
                        )
                    }

                    if (onDeleteClick != null) {
                        RoundedActionButton(
                            onClick = onDeleteClick,
                            icon = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    } else if (onFavoriteClick != null) {
                        RoundedActionButton(
                            onClick = onFavoriteClick,
                            icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else TextDarkPurple.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextDarkPurple,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = OutlineSoft.copy(alpha = 0.8f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatPrice(price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CtaGreen
                )
            }
        }
    }
}

private fun formatPrice(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed
    if (trimmed.contains("₺")) return trimmed
    if (Regex("\\bTL\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
        return trimmed.replace(Regex("\\bTL\\b", RegexOption.IGNORE_CASE), "₺")
    }
    return "₺ $trimmed"
}

@Composable
private fun RoundedActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tint: Color
) {
    Surface(
        shape = CircleShape,
        color = SurfaceLight.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        modifier = Modifier.size(34.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
