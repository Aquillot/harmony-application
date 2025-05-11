package fr.harmony.explore

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import fr.harmony.IntentExplore
import fr.harmony.R
import fr.harmony.components.Avatar
import fr.harmony.components.BottomBar
import fr.harmony.components.TopBar
import fr.harmony.explore.components.ImagePager
import fr.harmony.explore.data.SharedImage
import fr.harmony.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ExploreScreen(
    viewModel: ModelExplore = hiltViewModel(),
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    snackbarScope: CoroutineScope,
) {
    val state = viewModel.state.collectAsState().value

    // 1. Collecter les événements
    LaunchedEffect(viewModel) {
        viewModel.handleIntent(IntentExplore.LoadImages)

        viewModel.events.collect { event ->
            when (event) {
                is EventExplore.ShowError -> {
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
                is NavigationEventExplore.NavigateToHome -> {
                    navController.navigate("home")
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
                viewModel.handleIntent(IntentExplore.NavigateToHome)
            })

            Box(
                modifier = Modifier.padding(6.dp, 11.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.EXPLORE_TITLE),
                    style = AppTheme.harmonyTypography.title,
                    color = AppTheme.harmonyColors.textColor,
                )
            }

            when {
                state.images.isEmpty() -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(50.dp)
                        )
                    }
                }

                else -> {
                    // Affichage des images
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(state.images.size) { id ->
                            Card(
                                image = state.images[id],
                                onImageClick = {
                                    viewModel.handleIntent(IntentExplore.ShowSlider(state.images[id]))
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
        }

        // Popup pour afficher l'image avant/après (avec zoom)
        state.sliderImage?.let {
            ImagePager(
                visible = state.sliderVisible,
                beforeImage = it.original_image_url,
                afterImage = it.harmonized_image_url,
                onOutsideClick = {
                    viewModel.handleIntent(IntentExplore.HideSlider)
                },
                onImageDoubleTap = { type ->
                    viewModel.handleIntent(IntentExplore.LikeImage(type == "before"))
                },
            )
        }

        BottomBar(id = 2, navController)
    }
}

@Composable
fun Card(
    image: SharedImage,
    modifier: Modifier = Modifier,
    onImageClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.harmonyColors.darkCard, shape = RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = AppTheme.harmonyColors.darkCardStroke,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(10.dp, 10.dp, 10.dp, 10.dp),
    ) {
        Column (
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ){
            // Header
            Row (
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Avatar(
                    userId = image.user.id,
                    modifier = Modifier
                        .size(40.dp)
                )
                Text(
                    text = image.user.username,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            // Body
            if (image.user_vote == null) {
                // Affichage de l'image avant/après
                ImagesSameSize(
                    image = image,
                    onImageClick = onImageClick,
                )
            } else {
                // Affichage d'une image recouvrante
                OverlappingImages(
                    image = image,
                    firstOverlap = image.user_vote == "original",
                    onImageClick = onImageClick,
                )
            }
        }
    }
}

@Composable
fun ImagesSameSize(
     image: SharedImage,
     onImageClick: () -> Unit = {},
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
                }
                .clickable { onImageClick() },
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
                }
                .clickable { onImageClick() },
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
    onImageClick: () -> Unit = {},
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
                }
                .clickable { onImageClick() },
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
                }
                .clickable { onImageClick() },
            onError = {
                Log.e("AsyncImage", "Erreur de chargement", it.result.throwable)
            }
        )
    }
}