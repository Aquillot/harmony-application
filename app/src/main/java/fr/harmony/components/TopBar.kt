package fr.harmony.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.harmony.R
import fr.harmony.ui.theme.AppTheme

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    returnAction: (() -> Unit)? = null,
    downloadAction: (() -> Unit)? = null,
    shareAction: (() -> Unit)? = null,
) {
    // TopBar : Bouton de retour
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (returnAction != null) {
            OutlinedIconButton(
                onClick = { returnAction() },
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = AppTheme.harmonyColors.darkCard,
                    contentColor = AppTheme.harmonyColors.subtleTextColor,
                ),
                border = BorderStroke(
                    1.dp,
                    AppTheme.harmonyColors.darkCardStroke
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = stringResource(id = R.string.BACK_BUTTON),
                    tint = AppTheme.harmonyColors.textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // On passe à droite
        Spacer(modifier = Modifier.weight(1f))

        if (downloadAction != null) {
            OutlinedIconButton(
                onClick = { downloadAction() },
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = AppTheme.harmonyColors.darkCard,
                    contentColor = AppTheme.harmonyColors.subtleTextColor,
                ),
                border = BorderStroke(
                    1.dp,
                    AppTheme.harmonyColors.darkCardStroke
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = stringResource(id = R.string.DOWNLOAD_BUTTON),
                    tint = AppTheme.harmonyColors.textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (shareAction != null) {
            OutlinedIconButton(
                onClick = { shareAction() },
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = AppTheme.harmonyColors.darkCard,
                    contentColor = AppTheme.harmonyColors.subtleTextColor,
                ),
                border = BorderStroke(
                    1.dp,
                    AppTheme.harmonyColors.darkCardStroke
                ),
                modifier = Modifier
                    .size(46.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.share_nodes),
                    contentDescription = stringResource(id = R.string.SHARE_BUTTON),
                    tint = AppTheme.harmonyColors.textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}