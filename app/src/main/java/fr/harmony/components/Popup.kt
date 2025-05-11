package fr.harmony.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmony.ui.theme.AppTheme

@Composable
fun Popup(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismiss: () -> Unit = {},
    actionText: String,
    onAction: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val popupBounds = remember { mutableStateOf(Rect.Zero) }

    if (visible) {
        BackHandler(enabled = true) {
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -10 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -10 }) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val tappedInside = popupBounds.value.contains(offset)
                        if (!tappedInside) onDismiss()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.harmonyColors.darkCard)
                    .border(
                        width = 1.dp,
                        color = AppTheme.harmonyColors.darkCardStroke,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .align(Alignment.Center)
                    .onGloballyPositioned { coordinates ->
                        popupBounds.value = coordinates.boundsInParent()
                    }
                    .padding(12.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    content()

                    Row(
                        Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onDismiss() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            text = stringResource(id = fr.harmony.R.string.CANCEL),
                            color = AppTheme.harmonyColors.textColor,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Text(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onAction() }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            text = actionText,
                            color = Color(0xFFEC5E51),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}