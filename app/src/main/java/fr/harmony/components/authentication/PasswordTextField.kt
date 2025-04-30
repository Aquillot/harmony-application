package fr.harmony.components.authentication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme


@Composable
fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = stringResource(R.string.PASSWORD_LABEL),
    isError: Boolean = false,
    focusRequester: FocusRequester? = null,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
) {    // Variable pour savoir si le mot de passe est visible ou non
    var passwordVisible by remember { mutableStateOf(false) }
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
        // Champ de texte pour le mot de passe
        TextField(
            value = password,
            onValueChange = {
                fieldTouched = true
                onPasswordChange(it)
            },
            label = { Text(label) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() },
                onDone = { onDone?.invoke() }
            ),
            trailingIcon = {
                // Icône pour basculer la visibilité du mot de passe
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.padding(end = 14.dp)
                ) {
                    val icon = if (passwordVisible) R.drawable.eye else R.drawable.eye_slash
                    val description = if (passwordVisible)
                        stringResource(R.string.HIDE_PASSWORD)
                    else
                        stringResource(R.string.SHOW_PASSWORD)

                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = description,
                        tint = AppTheme.harmonyColors.disabledTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
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