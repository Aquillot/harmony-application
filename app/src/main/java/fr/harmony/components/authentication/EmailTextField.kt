package fr.harmony.components.authentication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.harmony.ui.theme.AppThemeColors

@Composable
fun EmailTextField(email: String, onEmailChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, AppThemeColors.custom.darkCardStroke, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            placeholder = { Text("") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.colors(
                focusedTextColor = AppThemeColors.custom.textColor,
                unfocusedTextColor = AppThemeColors.custom.textColor,
                focusedLabelColor = AppThemeColors.custom.disabledTextColor,
                unfocusedLabelColor = AppThemeColors.custom.disabledTextColor,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = AppThemeColors.custom.darkCard,
                unfocusedContainerColor = AppThemeColors.custom.darkCard,
            ),
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
