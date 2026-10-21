package com.example.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount

@Composable
fun UserProfileAvatarView(
    account: UserAccount?,
    size: Dp = 48.dp,
    borderColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    val preset = ProfileAvatarPresets.getById(account?.avatarId ?: "avatar_terminal")
    val initial = account?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "L"

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, borderColor, CircleShape)
            .background(Color(preset.secondaryColorHex)),
        contentAlignment = Alignment.Center
    ) {
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

        if (account?.isSignedIn == true && account.authProvider == "Google") {
            Text(
                text = initial,
                color = Color(preset.primaryColorHex),
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.45f).sp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = preset.name,
                tint = Color(preset.primaryColorHex),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
