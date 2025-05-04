package fr.harmony.harmonize.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.harmony.R

@Composable
fun BeforeAfterImageSlider(
    beforeImage: Uri,
    afterImage: Bitmap,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0.3f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))

            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newX = change.position.x / size.width
                    sliderPosition = newX.coerceIn(0f, 1f)
                }
            },
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val clipWidth = sliderPosition * widthPx

        // Image du dessous (après)
        Image(
            bitmap = afterImage.asImageBitmap(),
            contentDescription = stringResource(id = R.string.AFTER),
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        // Image du dessus (before), clippée
        AsyncImage(
            model = beforeImage,
            contentDescription = stringResource(id = R.string.BEFORE),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    clip = true
                    shape = RectangleShape
                }
                .drawWithContent {
                    clipRect(right = clipWidth) {
                        this@drawWithContent.drawContent()
                    }
                }
        )

        // Curseur vertical
        val circleSize = 26.dp
        val sliderX = (sliderPosition * widthPx) - with(LocalDensity.current) { (circleSize / 2).toPx() }

        Box(
            modifier = Modifier
                .offset { IntOffset(sliderX.toInt(), 0) }
                .fillMaxHeight()
                .wrapContentWidth()
        ) {
            val lineWidth = 4.dp
            val gapHeight = circleSize - 2.dp

            // Trait haut
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(lineWidth)
                    .fillMaxHeight(fraction = 0.5f)
                    .padding(bottom = gapHeight / 2)
                    .background(Color.White)
            )

            // Trait bas
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(lineWidth)
                    .fillMaxHeight(fraction = 0.5f)
                    .padding(top = gapHeight / 2)
                    .background(Color.White)
            )

            // Cercle vide
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .align(Alignment.Center)
                    .border(width = 3.dp, color = Color.White, shape = CircleShape)
                    .background(Color.Transparent, shape = CircleShape)
            )
        }
    }
}