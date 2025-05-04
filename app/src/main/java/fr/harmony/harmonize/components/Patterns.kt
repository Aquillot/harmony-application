package fr.harmony.harmonize.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme

@Composable
fun Patterns(
    selectedPattern: String,
    modifier: Modifier = Modifier,
    onPatternClicked: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        val patterns = listOf("triadic-harmony", "complementary-harmony", "square-harmony", "split-harmony", "double-split-harmony", "analogous-harmony", "monochromatic-harmony")

        val resIds = listOf(
            R.drawable.triadic,
            R.drawable.complementary,
            R.drawable.square,
            R.drawable.split,
            R.drawable.double_split,
            R.drawable.analogous,
            R.drawable.monochromatic
        )

        patterns.zip(resIds).forEach { (pattern, resId) ->
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPatternClicked(pattern) }
                    .background(AppTheme.harmonyColors.darkCard)
                    .border(
                        width = if (selectedPattern == pattern) 2.dp else 1.dp,
                        color = if (selectedPattern == pattern) AppTheme.harmonyColors.blueMunsell else AppTheme.harmonyColors.darkCardStroke,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(7.dp)
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
