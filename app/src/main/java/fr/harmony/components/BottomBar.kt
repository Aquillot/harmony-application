package fr.harmony.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.harmony.ui.theme.AppTheme
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import fr.harmony.R

@Composable
fun BottomBar(id: Int, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 11.dp)
            .navigationBarsPadding()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.8f to Color.Black,
                            1.0f to Color.Black
                        )
                    )
                )
        )

        // BottomBar en position absolue
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(AppTheme.harmonyColors.darkerCard, shape = RoundedCornerShape(50))
                .border(
                    width = 1.dp,
                    color = AppTheme.harmonyColors.darkerCardStroke,
                    shape = RoundedCornerShape(50)
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarIcon(
                    icon = R.drawable.home,
                    contentDescription = R.string.BAR_RETURN_HOME,
                    active = id == 0,
                )

                BottomBarIcon(
                    icon = R.drawable.wand_magic_sparkles,
                    contentDescription = R.string.BAR_HARMONIZE,
                    active = id == 1,
                    onClick = {
                        navController.navigate("import") {
                            popUpTo("harmonize") { inclusive = true }
                        }
                    }
                )

                BottomBarIcon(
                    icon = R.drawable.globe,
                    contentDescription = R.string.BAR_GO_EXPLORE,
                    active = id == 2,
                )
            }
        }
    }
}

@Composable
fun BottomBarIcon(
    icon: Int,
    contentDescription: Int,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(50)
    val backgroundColor = if (active) AppTheme.harmonyColors.lightCard else AppTheme.harmonyColors.darkCard
    val borderColor = if (active) AppTheme.harmonyColors.lightCardStroke else AppTheme.harmonyColors.darkCardStroke
    val iconColor = if (active) AppTheme.harmonyColors.darkerCard else AppTheme.harmonyColors.subtleTextColor

    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .border(1.dp, borderColor, shape)
            .clip(shape)
            .background(backgroundColor)
            .clickable { onClick?.invoke() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Icones de la barre inférieure
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = contentDescription),
            tint = iconColor
        )
    }
}
