package fr.harmony.userImages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import fr.harmony.R
import fr.harmony.components.BottomBar
import fr.harmony.components.Popup
import fr.harmony.components.TopBar
import fr.harmony.ui.theme.AppTheme
import fr.harmony.userImages.data.SharedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun UserImagesScreen(
    viewModel: ModelUserImages = hiltViewModel(),
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    snackbarScope: CoroutineScope,
) {
    val state = viewModel.state.collectAsState().value

    // 1. Collecter les événements
    LaunchedEffect(viewModel) {
        viewModel.handleIntent(IntentUserImages.LoadImages)

        viewModel.events.collect { event ->
            when (event) {
                is EventUserImages.ShowError -> {
                    // Afficher un message d'erreur
                    snackbarScope.launch {
                        snackbarHostState.showSnackbar(
                            message = JSONObject(
                                mapOf("message" to event.message, "type" to "error")
                            ).toString(),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }

    // 2. Collecter la Navigation
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                is NavigationEventUserImages.NavigateToExplore -> {
                    navController.navigate("explore")
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(11.dp, 40.dp, 11.dp, 11.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopBar(returnAction = {
                viewModel.handleIntent(IntentUserImages.NavigateToExplore)
            })

            Box(
                modifier = Modifier.padding(6.dp, 11.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.USER_IMAGES_TITLE),
                    style = AppTheme.harmonyTypography.title,
                    color = AppTheme.harmonyColors.textColor,
                )
            }

            // Affichage d'un loader si l'état est en cours de chargement
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .padding(top = 40.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            else {
                // Affichage d'un message si l'utilisateur n'a pas d'images
                if (state.images.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.USER_IMAGES_EMPTY),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.harmonyColors.textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp),
                    )
                }

                // Affichage des images
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(state.images.size) { id ->
                        Card(
                            image = state.images[id],
                            onCardLongPress = {
                                viewModel.handleIntent(IntentUserImages.ShowPopup(state.images[id]))
                            },
                        )

                        // Spacer pour la navbar
                        if (id == state.images.size - 1) {
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
            }
        }

        // Popup pour afficher l'image avant/après (avec zoom)
        Popup(
            visible = state.popupVisible,
            actionText = stringResource(id = R.string.DELETE_IMAGE),
            onDismiss = {
                viewModel.handleIntent(IntentUserImages.HidePopup)
            },
            onAction = {
                viewModel.handleIntent(IntentUserImages.DeleteImage)
            }
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = R.string.DELETE_IMAGE_CONFIRM),
                style = MaterialTheme.typography.titleMedium,
                color = AppTheme.harmonyColors.textColor,
            )
        }

        BottomBar(id = 2, navController)
    }
}

@Composable
fun Card(
    image: SharedImage,
    modifier: Modifier = Modifier,
    onCardLongPress: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.harmonyColors.darkCard, shape = RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = AppTheme.harmonyColors.darkCardStroke,
                shape = RoundedCornerShape(14.dp)
            )
            .combinedClickable(
                onClick = {}, // Obligatoire mais ignoré
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCardLongPress()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
            .padding(10.dp)
    ) {
        if (image.harmonized_votes == image.original_votes) {
            // Affichage de l'image avant/après
            ImagesSameSize(
                image = image,
            )
        } else {
            // Affichage d'une image recouvrante
            OverlappingImages(
                image = image,
                firstOverlap = image.original_votes > image.harmonized_votes,
            )
        }
    }
}

@Composable
fun ImagesSameSize(
     image: SharedImage,
) {
    Row (
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        val translation = LocalDensity.current.density
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://harmony.jhune.dev/${image.original_image_url}")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.BEFORE),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(14.dp)
                    translationX = -0.7f * translation // 70% recouvrant
                },
            placeholder = painterResource(R.drawable.placeholder),
            onError = {
                Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
            }
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://harmony.jhune.dev/${image.harmonized_image_url}")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.AFTER),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(14.dp)
                },
            placeholder = painterResource(R.drawable.placeholder),
            onError = {
                Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
            }
        )
    }
}

@Composable
fun OverlappingImages(
    image: SharedImage,
    firstOverlap: Boolean = true,
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Image de fond
        val backgroundColor = AppTheme.harmonyColors.darkCard
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://harmony.jhune.dev/${image.original_image_url}")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.BEFORE),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .zIndex(if (firstOverlap) 1f else 0f)
                .then(
                    if (firstOverlap) Modifier.drawWithContent {
                        drawRoundRect(
                            color = backgroundColor,
                            size = Size(size.width, size.height + 20f),
                            topLeft = Offset(20f, -10f),
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                        )
                        drawContent()
                    } else Modifier
                )
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(14.dp)
                },
            onError = {
                Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
            }
        )

        // Image qui peut se superposer
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://harmony.jhune.dev/${image.harmonized_image_url}")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.AFTER),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .align(Alignment.CenterEnd)
                .zIndex(if (firstOverlap) 0f else 1f)
                .then(
                    if (!firstOverlap) Modifier.drawWithContent {
                        drawRoundRect(
                            color = backgroundColor,
                            size = Size(size.width, size.height + 20f),
                            topLeft = Offset(-20f, -10f),
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                        )
                        drawContent()
                    } else Modifier
                )
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(14.dp)
                },
            onError = {
                Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
            }
        )
    }
}