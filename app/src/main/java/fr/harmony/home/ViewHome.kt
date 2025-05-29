package fr.harmony.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import fr.harmony.R
import fr.harmony.User
import fr.harmony.components.BottomBar
import fr.harmony.components.TopBar
import fr.harmony.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    viewModel: ModelHomeScreen = hiltViewModel(),
    user: User,
    navController: NavController,
) {
    val lazyImages = viewModel.pager.collectAsLazyPagingItems()

    val state by viewModel.state.collectAsState()
    var isLoadingHarmonization by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state is StateHome.Refreshing) {
            println("HomeScreen: on refresh")
            lazyImages.refresh()
            viewModel.handleIntent(IntentHome.EndRefresh)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                is NavigationEventHome.NavigateToImport -> {
                    navController.navigate("import")
                }
                is NavigationEventHome.NavigateToHarmonize -> {
                    println("HomeScreen: navigate to harmonize")
                    navController.navigate("harmonize?idFromDataBase=${event.id}/originalUri=${event.uri}")
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Content column to offset Top/Bottom bars
        Box(modifier = Modifier.statusBarsPadding()) {
            Column(
                modifier = Modifier.padding(11.dp, 40.dp, 11.dp, 11.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TopBar(
                    user = user,
                    userButtonLeft = true,
                    userButtonAction = { },
                    addImageAction = {
                        viewModel.handleIntent(IntentHome.NavigateToImport)
                    },
                )

                // Titre de la page
                Text(
                    text = stringResource(id = R.string.YOUR_HARMONIZED_IMAGES),
                    style = AppTheme.harmonyTypography.title,
                    color = AppTheme.harmonyColors.textColor,
                    modifier = Modifier.padding(6.dp, 11.dp)
                )

                // Si la liste est vide, on affiche un message
                if (lazyImages.itemCount == 0 && lazyImages.loadState.refresh is LoadState.NotLoading) {
                    Text(
                        text = stringResource(id = R.string.NO_HARMONIZED_IMAGES),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.harmonyColors.textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    )
                }

                MasonryGrid(
                    lazyImages = lazyImages,
                    isLoadingHarmonization = isLoadingHarmonization,
                    setLoadingHarmonization = { loading -> isLoadingHarmonization = loading },
                    coroutineScope = coroutineScope,
                    onRunSession = { id, uri ->
                        viewModel.handleIntent(
                            IntentHome.NavigateToHarmonize(id, uri.toString())
                        )
                    },
                    viewModel = viewModel
                )
            }
        }

        if (isLoadingHarmonization) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // Indicateur indéterminé :contentReference[oaicite:0]{index=0}
            }
        }

        // Bottom bar
        BottomBar(id = 0, navController = navController)
    }
}

@Composable
fun MasonryGrid(
    lazyImages: androidx.paging.compose.LazyPagingItems<GalleryImage>,
    isLoadingHarmonization: Boolean,
    coroutineScope: CoroutineScope,
    setLoadingHarmonization: (Boolean) -> Unit,
    columnCount: Int = 2,
    columnSpacing: Dp = 10.dp,
    verticalSpacing: Dp = 10.dp,
    onRunSession: (Long, Uri) -> Unit,
    viewModel: ModelHomeScreen
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val availableWidthDp = screenWidthDp - columnSpacing * (columnCount - 1)
    val imageWidthDp = availableWidthDp / columnCount
    val density = LocalDensity.current
    val imageWidthPx: Int = with(density) { imageWidthDp.toPx().roundToInt() }

    val gridState = rememberLazyStaggeredGridState()

    var imageToDelete by remember { mutableStateOf<GalleryImage?>(null) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalItemSpacing = verticalSpacing,
        modifier = Modifier.fillMaxSize()
    ) {
        if (lazyImages.loadState.prepend is LoadState.Loading) {
            item(
                key = "loader-prepend",
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(
            count = lazyImages.itemCount,
            key = { index -> lazyImages.peek(index)?.id.toString() },
        ) { idx ->
            val img = lazyImages[idx] ?: return@items
            val aspect = img.aspectRatio
            val heightPx = (imageWidthPx / aspect).roundToInt()

            val baseModifier = if (img.span == 2) Modifier.fillMaxWidth() else Modifier.width(imageWidthDp)

            Card (
                modifier = baseModifier,
                image = img,
                aspect = aspect,
                imageWidthPx = imageWidthPx,
                heightPx = heightPx,
                onTap = {
                    setLoadingHarmonization(true)
                    coroutineScope.launch {
                        onRunSession(img.id, img.originalUri)
                    }
                },
                onLongPress = { imageToDelete = img }
            )
        }

        // Handle loading / error footers
        lazyImages.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { Box(Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                loadState.append is LoadState.Loading -> {
                    item { Box(Modifier
                        .fillMaxWidth()
                        .padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                loadState.append is LoadState.Error -> {
                    item {
                        OutlinedButton(onClick = { retry() }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = "Réessayer")
                        }
                    }
                }
                loadState.refresh is LoadState.Error -> {
                    item {
                        OutlinedButton(onClick = { retry() }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Text(text = "Réessayer")
                        }
                    }
                }
            }
        }

        // Spacer pour la navbar
        item {
            Spacer(modifier = Modifier.height(270.dp))
        }
    }

    // AlerteDialog pour la suppression d'image
    if (imageToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Supprimer l'image ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                Text(
                    "Supprimer",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            viewModel.handleIntent(
                                IntentHome.DeleteImage(
                                    imageToDelete!!.id
                                )
                            )
                            imageToDelete = null
                        },
                    color = Color.Red
                )
            },
            dismissButton = {
                Text(
                    "Annuler",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { imageToDelete = null }
                )
            }
        )
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    image: GalleryImage,
    aspect: Float,
    imageWidthPx: Int,
    heightPx: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = { onTap() },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )

    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image.uri)
                .size(imageWidthPx, heightPx)
                .crossfade(true)
                .allowHardware(true)
                // ↓ Désactive complètement le cache mémoire
                .memoryCachePolicy(CachePolicy.DISABLED)
                // (Tu peux laisser le cache disque si tu veux recharger depuis le fichier plus vite)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
}