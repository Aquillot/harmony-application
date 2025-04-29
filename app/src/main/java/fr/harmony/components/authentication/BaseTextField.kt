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
import fr.harmony.ui.theme.AppTheme

@Composable
fun BaseTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, AppTheme.harmonyColors.darkCardStroke, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            placeholder = { Text("") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedTextColor = AppTheme.harmonyColors.textColor,
                unfocusedTextColor = AppTheme.harmonyColors.textColor,
                focusedLabelColor = AppTheme.harmonyColors.disabledTextColor,
                unfocusedLabelColor = AppTheme.harmonyColors.disabledTextColor,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = AppTheme.harmonyColors.darkCard,
                unfocusedContainerColor = AppTheme.harmonyColors.darkCard,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
