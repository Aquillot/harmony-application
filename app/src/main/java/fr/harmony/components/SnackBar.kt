package fr.harmony.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.imageimport.darkenColor
import fr.harmony.ui.theme.AppTheme
import org.json.JSONObject

@Composable
fun SnackBar (
    snackbarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val value = JSONObject(data.visuals.message)
                val type = value.getString("type")
                val message = value.getString("message")

                val backgroundColor = when (type) {
                    "warning" -> Color(0xFF978839)
                    "error" -> Color(0xFFe26055)
                    else -> AppTheme.harmonyColors.darkCard
                }

                val strokeColor = when (type) {
                    "warning" -> darkenColor(Color(0xFF978839), 0.2f)
                    "error" -> darkenColor(Color(0xFFe26055), 0.2f)
                    else -> AppTheme.harmonyColors.darkCardStroke
                }

                Snackbar(
                    modifier = Modifier
                        .padding(12.dp)
                        .border(
                            width = 1.dp,
                            color = strokeColor,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    containerColor = backgroundColor,
                    shape = RoundedCornerShape(14.dp),
                    contentColor = Color.White,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.xmark),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .size(24.dp)
                                .clickable { data.dismiss() }
                                .align(Alignment.CenterVertically),
                            tint = Color.White
                        )
                    }

                }
            }
        }
    ) { contentPadding ->
        content(contentPadding)
    }
}