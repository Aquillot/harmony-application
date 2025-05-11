package fr.harmony.harmonize

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import fr.harmony.R
import fr.harmony.components.BottomBar
import fr.harmony.components.TopBar
import fr.harmony.harmonize.components.BeforeAfterImageSlider
import fr.harmony.harmonize.components.Pallet
import fr.harmony.harmonize.components.Patterns
import fr.harmony.harmonize.components.SharePopup
import fr.harmony.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject


fun navigateBack(navController: NavController) {
    navController.navigate("import") {
        popUpTo("harmonize") { inclusive = true }
    }
}

@Composable
fun HarmonizeImageScreen(
    imageUri: Uri = Uri.EMPTY,
    idFromDataBase : Long = -1,
    viewModel: ModelHarmonize = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    snackbarScope: CoroutineScope,
    navController: NavController
) {

    LaunchedEffect(imageUri) {
        if (idFromDataBase != -1L) {
            viewModel.initFromDataBase(idFromDataBase)
        } else {
            viewModel.initWith(imageUri)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (idFromDataBase != -1L) {
                viewModel.updateSessionOnDataBase(idFromDataBase)
            }else{
                viewModel.saveSessionOnDataBase()
                viewModel.cleanup()
            }
        }
    }

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val animatedAlpha by animateFloatAsState(
        targetValue = if (state.shareVisible) 0.5f else 1f,
        animationSpec = tween(
            durationMillis = 200,     // Durée de l’animation
            easing = FastOutSlowInEasing // Courbe d’interpolation
        ),
        label = "Image alpha animation"
    )

    fun saveBitmapToUri(bitmap: Bitmap, uri: Uri) {
        val outputStream = context.contentResolver.openOutputStream(uri)
        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    val saveImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
            uri?.let { saveBitmapToUri(state.imageBitmap, it) }
        }

    // 1. Collecter les Events UI (share, snackbar…)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EventHarmonize.LaunchShareIntent -> {
                    context.startActivity(
                        Intent.createChooser(event.shareIntent, null)
                    )
                }

                is EventHarmonize.ShowSnackbar ->  {
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

                is EventHarmonize.RequestSaveImage -> {
                    saveImageLauncher.launch("harmonized_image.png")
                }
            }
        }
    }

    // 2. Collecter la Navigation
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                is NavigationEventHarmonize.NavigateToImport -> {
                    navController.navigate("import") {
                        popUpTo("harmonize") { inclusive = true }
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(11.dp, 40.dp, 11.dp, 11.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopBar(
                returnAction = { navigateBack(navController) },
                downloadAction = if (state.step == HarmonizeStep.DONE) {
                    { viewModel.onIntent(IntentHarmonize.DownloadImage) }
                } else null,
                shareAction = if (state.step == HarmonizeStep.DONE) {
                    { viewModel.onIntent(IntentHarmonize.ToggleShareMenu) }
                } else null,
                modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                    val position = layoutCoordinates.positionInRoot()
                    val height = layoutCoordinates.size.height
                    viewModel.onShareOffsetChanged((position.y + height).toInt())
                }
            )

            if (state.step != HarmonizeStep.DONE) {
                LoadingView(state.step)
            } else {

                Column(
                    modifier = Modifier.alpha(animatedAlpha),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(6.dp, 11.dp)
                            .alpha(animatedAlpha),
                        text = stringResource(id = R.string.HARMONIZE_IMAGE_TITLE),
                        style = AppTheme.harmonyTypography.title,
                        color = AppTheme.harmonyColors.textColor,
                    )

                    BeforeAfterImageSlider(
                        beforeImage = imageUri,
                        afterImage = state.imageBitmap,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                    )

                    Pallet(
                        colors = state.harmonizedPalette[state.selectedPattern] ?: state.palette,
                        onColorSelected = { color -> println("TODO Selected color: $color") })

                    Patterns(
                        selectedPattern = state.selectedPattern,
                        onPatternClicked = { viewModel.onIntent(IntentHarmonize.PatternSelected(it)) },
                    )

                    Text(
                        modifier = Modifier.padding(top = 20.dp),
                        text = stringResource(id = R.string.STRENGTH_LABEL),
                        style = AppTheme.harmonyTypography.titleMedium,
                        color = AppTheme.harmonyColors.textColor,
                    )

                    Slider(
                        value = state.sliderPosition,
                        onValueChange = { viewModel.onIntent(IntentHarmonize.SliderChanged(it)) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AppTheme.harmonyColors.blueMunsell,
                            activeTrackColor = AppTheme.harmonyColors.subtleTextColor,
                            inactiveTrackColor = AppTheme.harmonyColors.darkCardStroke,
                        ),
                    )
                }

                // Spacer pour la navbar
                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        SharePopup(
            visible = state.shareVisible,
            offset = state.shareOffset,
            sharingState = state.sharingState,
            onOutsideClick = { viewModel.onIntent(IntentHarmonize.ToggleShareMenu) },
            onAppShareClick = { viewModel.onIntent(IntentHarmonize.ShareAppClicked) },
            onShareClick = { viewModel.onIntent(IntentHarmonize.ShareExternalClicked) }
        )

        BottomBar(id = 1, navController = navController)
    }
}

@Composable
fun LoadingView(step: HarmonizeStep) {
    val stepText = when (step) {
        HarmonizeStep.SEND_IMAGE -> R.string.STEP_SENDING
        HarmonizeStep.COMPUTE_PALETTE -> R.string.STEP_PALETTE
        HarmonizeStep.SIMPLIFY_PALETTE -> R.string.STEP_SIMPLIFICATION
        HarmonizeStep.IMAGE_SEGMENTATION -> R.string.STEP_SPLIT
        HarmonizeStep.RECEIVE_LAYERS -> R.string.STEP_LAYERS
        HarmonizeStep.PALETTE_HARMONIZATION -> R.string.STEP_HARMONIZATION
        HarmonizeStep.DONE -> R.string.STEP_SENDING
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
            )
            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = stringResource(id = stepText) + "...",
                style = AppTheme.harmonyTypography.titleMedium,
                textAlign = TextAlign.Center,
                color = AppTheme.harmonyColors.textColor
            )
        }
    }
}