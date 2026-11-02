package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProfileAvatarPresets
import com.example.model.UserAccount
import java.io.File

@Composable
fun UserProfileAvatarView(
    account: UserAccount?,
    size: Dp = 48.dp,
    borderColor: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    val customUri = account?.avatarCustomUri
    val customBitmap = remember(customUri) {
        if (!customUri.isNullOrBlank()) {
            val f = File(customUri)
            if (f.exists() && f.length() > 0) {
                try {
                    BitmapFactory.decodeFile(f.absolutePath)
                } catch (e: Exception) {
                    null
                }
            } else null
        } else null
    }

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
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap.asImageBitmap(),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (account?.isSignedIn == true) {
            Text(
                text = initial,
                color = Color(preset.primaryColorHex),
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.45f).sp
            )
        } else if (preset.iconKey == "terminal" || account?.isSignedIn != true) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_linq_user_logo),
                contentDescription = "Linq Studio Logo",
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(preset.primaryColorHex)),
                modifier = Modifier
                    .size(size * 0.58f)
                    .padding(2.dp)
            )
        } else {
            val icon = when (preset.iconKey) {
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
