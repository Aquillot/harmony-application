package fr.harmony.explore.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme
import kotlin.math.absoluteValue
import kotlin.math.max

@Composable
fun ImagePager(
    visible: Boolean,
    beforeImage: String,
    afterImage: String,
    modifier: Modifier = Modifier,
    onOutsideClick: () -> Unit = {},
    onImageDoubleTap: (image: String) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val imageBounds = remember { mutableStateOf(List(2) { androidx.compose.ui.geometry.Rect.Zero }) }

    LaunchedEffect(visible) {
        if (visible) {
            pagerState.scrollToPage(0)
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
                        val tappedInside = imageBounds.value.any { it.contains(offset) }
                        if (!tappedInside) onOutsideClick()
                    }
                }
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 14.dp,
                contentPadding = PaddingValues(horizontal = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imageUrl = if (page == 0) beforeImage else afterImage
                val label = if (page == 0) stringResource(R.string.BEFORE) else stringResource(R.string.AFTER)
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val alignment = if (page == 0) Alignment.CenterEnd else Alignment.CenterStart

                ZoomableImage(
                    imageUrl = imageUrl,
                    label = label,
                    pageOffset = pageOffset,
                    alignment = alignment,
                    onBoundsUpdated = { newBounds ->
                        imageBounds.value = imageBounds.value.toMutableList().apply {
                            this[page] = newBounds
                        }
                    },
                    onImageDoubleTap = { onImageDoubleTap(if(page == 0) "before" else "after") }
                )
            }
        }
    }
}

fun Modifier.zoomableIfMultiTouch(
    scale: MutableState<Float>,
    offsetX: MutableState<Float>,
    offsetY: MutableState<Float>,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    resetThreshold: Float = 1.2f
): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Main)
            if (event.changes.size > 1) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                val newScale = (scale.value * zoomChange).coerceIn(minScale, maxScale)

                if (newScale < resetThreshold && zoomChange < 1f) {
                    scale.value = 1f
                    offsetX.value = 0f
                    offsetY.value = 0f
                } else {
                    scale.value = newScale
                    offsetX.value += panChange.x
                    offsetY.value += panChange.y
                }

                // Consume all pointer changes
                event.changes.forEach { it.consume() }
            }

            if (event.changes.all { it.changedToUp() }) {
                if (scale.value < resetThreshold) {
                    scale.value = 1f
                    offsetX.value = 0f
                    offsetY.value = 0f
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    label: String,
    pageOffset: Float,
    alignment: Alignment,
    onBoundsUpdated: (androidx.compose.ui.geometry.Rect) -> Unit,
    onImageDoubleTap: () -> Unit = {},
) {
    val context = LocalContext.current
    var showLike by remember { mutableStateOf(false) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data("https://harmony.jhune.dev/$imageUrl")
            .crossfade(true)
            .build()
    )

    val intrinsicSize = painter.intrinsicSize
    val imageRatio = if (intrinsicSize.width > 0 && intrinsicSize.height > 0) {
        intrinsicSize.width / intrinsicSize.height
    } else 1f

    val scale = remember { mutableFloatStateOf(1f) }
    val offsetX = remember { mutableFloatStateOf(0f) }
    val offsetY = remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zoomableIfMultiTouch(scale, offsetX, offsetY)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(max(0.8f, 1f - pageOffset))
                .align(alignment)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(14.dp)
                }
                .aspectRatio(imageRatio)
                .onGloballyPositioned {
                    onBoundsUpdated(it.boundsInRoot())
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showLike = true
                        }
                    )
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://harmony.jhune.dev/$imageUrl")
                    .crossfade(true)
                    .build(),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale.floatValue,
                        scaleY = scale.floatValue,
                        translationX = offsetX.floatValue,
                        translationY = offsetY.floatValue
                    ),
                onError = {
                    Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
                }
            )

            Text(
                text = label,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(
                        color = AppTheme.harmonyColors.lightCard,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = AppTheme.harmonyColors.lightCardStroke,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(6.dp, 3.dp),
                color = Color.Black,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            if (showLike) {
                onImageDoubleTap()
                LikeAnimation(
                    trigger = showLike,
                    modifier = Modifier
                        .align(Alignment.Center) // Ou l’emplacement du tap
                        .size(60.dp)
                ) {
                    showLike = false // Reset après animation
                }
            }
        }
    }
}

@Composable
fun LikeAnimation(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {}
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            scale.snapTo(0f)
            alpha.snapTo(1f)

            scale.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 0.9f,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1.1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )

            onAnimationEnd()
        }
    }

    Icon(
        painter = painterResource(id = R.drawable.plain_heart),
        contentDescription = "Like",
        tint = Color(0xFFF72747),
        modifier = modifier
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                alpha = alpha.value
            )
    )
}
