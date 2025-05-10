package fr.harmony.harmonize.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SharePopup(
    modifier: Modifier = Modifier,
    visible: Boolean,
    offset: Int = 0,
    sharingState: String = "none", // "none", "loading", "done"
    onOutsideClick: () -> Unit,     // Callback appelé lorsqu’on clique en dehors du menu
    onAppShareClick: () -> Unit,    // Callback pour le bouton d'app sharing
    onShareClick: () -> Unit,       // Callback générique pour les autres boutons
) {
    // Stocke la position et la taille du Row pour détecter les clics en dehors
    var rowBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Utilise un pointerInput pour détecter les taps sur toute la surface
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val layoutBounds = rowBounds
                        // Si le tap est en dehors des bounds du menu, on déclenche onOutsideClick
                        if (layoutBounds != null && !layoutBounds.contains(tapOffset)) {
                            onOutsideClick()
                        }
                    }
                }
        )
    }

    // Gère l’apparition/disparition animée du composant
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -10 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -10 }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(0, offset) }
                .padding(12.dp)
        ) {
            // Le menu flottant aligné en haut à droite
            Row(
                modifier = Modifier
                    .height(66.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.harmonyColors.darkCard)
                    .border(
                        width = 1.dp,
                        color = AppTheme.harmonyColors.darkCardStroke,
                        shape = RoundedCornerShape(0.dp)
                    )
                    // Enregistre la position globale du Row à l’écran
                    .onGloballyPositioned { coordinates ->
                        rowBounds = coordinates.boundsInRoot()
                    }
                    .padding(10.dp, 8.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // --- App Share Button ---
                if (sharingState == "loading" || sharingState == "done") {
                    AnimatedContent(
                        targetState = sharingState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { state ->
                        when (state) {
                            "loading" -> {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF171A21))
                                        .padding(12.dp)
                                )
                            }

                            "done" -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.check),
                                    contentDescription = stringResource(id = R.string.SHARE_SUCCESS),
                                    tint = AppTheme.harmonyColors.lightCard,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF171A21))
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { if (visible && sharingState == "none") onAppShareClick() },
                        contentScale = ContentScale.Fit
                    )
                }

                // --- BlueSky Button ---
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0085FF))
                        .clickable { if (visible) onShareClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.bluesky),
                        contentDescription = stringResource(id = R.string.SHARE_BLUESKY),
                        tint = AppTheme.harmonyColors.lightCard,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // --- Pinterest Button ---
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE60023))
                        .clickable { if (visible) onShareClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pinterest),
                        contentDescription = stringResource(id = R.string.SHARE_PINTEREST),
                        tint = AppTheme.harmonyColors.lightCard,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // --- Facebook Button ---
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1877F2))
                        .clickable { if (visible) onShareClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = stringResource(id = R.string.SHARE_FACEBOOK),
                        tint = AppTheme.harmonyColors.lightCard,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}