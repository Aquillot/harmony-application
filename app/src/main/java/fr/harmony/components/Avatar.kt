package fr.harmony.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun Avatar(
    userId: Int,
    modifier: Modifier = Modifier
) {
    val avatarIndex = (userId % 30).let { if (it == 0) 30 else it }
    val context = LocalContext.current
    val resId = remember(avatarIndex) {
        context.resources.getIdentifier("avatar_$avatarIndex", "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Avatar $avatarIndex",
            modifier = modifier
                .aspectRatio(1f)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // fallback au cas où
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default avatar",
            modifier = modifier
        )
    }
}