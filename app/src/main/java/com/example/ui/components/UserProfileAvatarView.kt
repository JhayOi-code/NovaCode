package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount

@Composable
fun UserProfileAvatarView(
    account: UserAccount?,
    size: Dp = 48.dp,
    borderColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    val customUri = account?.avatarCustomUri
    val preset = ProfileAvatarPresets.getById(account?.avatarId ?: "avatar_terminal")

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, borderColor, CircleShape)
            .background(Color(preset.secondaryColorHex)),
        contentAlignment = Alignment.Center
    ) {
        if (!customUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(customUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            val icon = when (preset.iconKey) {
                "terminal" -> Icons.Default.Terminal
                "security" -> Icons.Default.Security
                "cloud" -> Icons.Default.Cloud
                "palette" -> Icons.Default.Palette
                "phone_android" -> Icons.Default.PhoneAndroid
                "code" -> Icons.Default.Code
                "storage" -> Icons.Default.Storage
                "bug_report" -> Icons.Default.BugReport
                else -> Icons.Default.Person
            }
            Icon(
                imageVector = icon,
                contentDescription = preset.name,
                tint = Color(preset.primaryColorHex),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
