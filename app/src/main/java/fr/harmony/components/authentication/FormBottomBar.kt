package fr.harmony.components.authentication

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.harmony.ui.theme.AppTheme

@Composable
fun FormBottomBar(
    text: String,
    onClick: () -> Unit,
){
    TextButton(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .navigationBarsPadding(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
        ),
    ) {
        Text(
            text = text,
            style = AppTheme.harmonyTypography.smallLine,
            color = AppTheme.harmonyColors.textColor
        )
    }
}