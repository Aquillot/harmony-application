package fr.harmony.components.authentication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.harmony.ui.theme.AppTheme

@Composable
fun BaseTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    focusRequester: FocusRequester? = null,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    var fieldTouched by remember { mutableStateOf(false) }

    val imeAction = when {
        onNext != null -> ImeAction.Next
        onDone != null -> ImeAction.Done
        else -> ImeAction.Default
    }

    val fieldModifier = Modifier
        .fillMaxWidth()
        .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)

    Box(
        modifier = modifier.border(
            if (fieldTouched && isError) 2.dp else 1.dp,
            if (fieldTouched && isError) AppTheme.harmonyColors.errorColor
            else AppTheme.harmonyColors.darkCardStroke,
            RoundedCornerShape(12.dp)
        )
    ) {
        TextField(
            value = value,
            onValueChange = {
                fieldTouched = true
                onValueChange(it)
            },
            label = { Text(label) },
            singleLine = true,
            placeholder = { Text("") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() },
                onDone = { onDone?.invoke() }
            ),
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
            modifier = fieldModifier,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
