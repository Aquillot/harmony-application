package fr.harmony.homescreen

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import fr.harmony.R
import fr.harmony.components.BottomBar
import fr.harmony.ui.theme.AppTheme
import kotlin.math.roundToInt
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.request.CachePolicy
import fr.harmony.User
import fr.harmony.components.Avatar

@Composable
fun HomeScreen(
    vm: ModelHomeScreen = hiltViewModel(),
    user: User,
    navController: NavController,
    onAddSession: () -> Unit,
    onRunSession: (Long, Uri) -> Unit,
) {
    val lazyImages = vm.pager.collectAsLazyPagingItems()

    val state by vm.state.collectAsState()
    var isLoadingHarmonization by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is StateHomeScreen.Refreshing) {
            println("HomeScreen: on refresh")
            lazyImages.refresh()
            vm.onIntent(IntentHomeScreen.EndRefresh)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Surface(modifier = Modifier.fillMaxSize()) {
            // Content column to offset Top/Bottom bars
            Column(
                modifier = Modifier
                    .padding(
                        top = 180.dp,  // hauteur estimée de la TopBar
                        bottom = 110.dp  // hauteur estimée de la BottomBar
                    )
            ) {
                // Si la liste est vide, on affiche un message
                if (lazyImages.itemCount == 0 && lazyImages.loadState.refresh is LoadState.NotLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.NO_HARMONIZED_IMAGES),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.harmonyColors.textColor
                        )
                    }
                }
                MasonryGrid(
                    lazyImages = lazyImages,
                    isLoadingHarmonization = isLoadingHarmonization,
                    setLoadingHarmonization = { loading -> isLoadingHarmonization = loading },
                    onRunSession=onRunSession, vm=vm
                )
            }

            // Top bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(11.dp, 40.dp, 11.dp, 11.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppTheme.harmonyColors.darkCard)
                            .padding(3.dp, 5.dp, 17.dp,5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            userId = user.id,
                            modifier = Modifier
                                .size(40.dp)
                        )
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedIconButton(
                        onClick = onAddSession,
                        border = BorderStroke(1.dp, AppTheme.harmonyColors.darkCardStroke),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = AppTheme.harmonyColors.darkCard,
                            contentColor = AppTheme.harmonyColors.subtleTextColor
                        ),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = stringResource(id = R.string.ADD_IMAGE),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.YOUR_HARMONIZED_IMAGES),
                    style = AppTheme.harmonyTypography.title,
                    color = AppTheme.harmonyColors.textColor,
                    modifier = Modifier.padding(6.dp, 11.dp)
                )
            }

            // Bottom bar
            BottomBar(id = 0, navController = navController)
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

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasonryGrid(
    lazyImages: androidx.paging.compose.LazyPagingItems<GalleryImage>,
    isLoadingHarmonization: Boolean,
    setLoadingHarmonization: (Boolean) -> Unit,
    columnCount: Int = 2,
    horizontalPadding: Dp = 16.dp,
    columnSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 6.dp,
    onRunSession: (Long, Uri) -> Unit,
    vm: ModelHomeScreen
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val availableWidthDp = screenWidthDp - horizontalPadding - columnSpacing * (columnCount - 1)
    val imageWidthDp = availableWidthDp / columnCount
    val density = LocalDensity.current
    val imageWidthPx: Int = with(density) { imageWidthDp.toPx().roundToInt() }

    val gridState = rememberLazyStaggeredGridState()

    var imageToDelete by remember { mutableStateOf<GalleryImage?>(null) }


    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(columnCount),
        state = gridState,
        contentPadding = PaddingValues(vertical = verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalItemSpacing = verticalSpacing,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        if (lazyImages.loadState.prepend is LoadState.Loading) {
            item(
                key = "loader-prepend",
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        items(
            count = lazyImages.itemCount,
            key = { index -> lazyImages.peek(index)?.uri.toString() ?: index }
        ) { idx ->
            val img = lazyImages[idx] ?: return@items
            val aspect = img.aspectRatio
            val heightPx = (imageWidthPx / aspect).roundToInt()

            val baseModifier = if (img.span == 2) Modifier.fillMaxWidth() else Modifier.width(imageWidthDp)

            Box(
                modifier = baseModifier
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(14.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                setLoadingHarmonization(true)
                                onRunSession(img.id,img.originalUri)
                                println("Image clicked: ${img.id}")
                            },
                            onLongPress = {
                                imageToDelete = img
                            }
                        )
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(img.uri)
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
                                        vm.onIntent(IntentHomeScreen.DeleteImage(imageToDelete!!.id))
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
        }
        // Handle loading / error footers
        lazyImages.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                loadState.append is LoadState.Loading -> {
                    item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                }
                loadState.append is LoadState.Error -> {
                    item {
                        OutlinedButton(onClick = { retry() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(text = "Réessayer")
                        }
                    }
                }
                loadState.refresh is LoadState.Error -> {
                    item {
                        OutlinedButton(onClick = { retry() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(text = "Réessayer")
                        }
                    }
                }
            }
        }
    }
}