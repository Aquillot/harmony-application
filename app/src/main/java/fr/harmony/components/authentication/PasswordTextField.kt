package fr.harmony.components.authentication

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme


@Composable
fun PasswordTextField(password: String, onPasswordChange: (String) -> Unit) {
    // Variable pour savoir si le mot de passe est visible ou non
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.border(
            1.dp,
            AppTheme.harmonyColors.darkCardStroke,
            RoundedCornerShape(12.dp)
        )
    ) {
        // Champ de texte pour le mot de passe
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.PASSWORD_LABEL)) },
            singleLine = true,
            placeholder = { Text("") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                // Icône pour basculer la visibilité du mot de passe
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.padding(end = 14.dp)
                ) {
                    if (passwordVisible) {
                        Icon(
                            painter = painterResource(id = R.drawable.eye),
                            contentDescription = stringResource(R.string.HIDE_PASSWORD),
                            tint = AppTheme.harmonyColors.disabledTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.eye_slash),
                            contentDescription = stringResource(R.string.SHOW_PASSWORD),
                            tint = AppTheme.harmonyColors.disabledTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}