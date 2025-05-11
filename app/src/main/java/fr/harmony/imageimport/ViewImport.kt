package fr.harmony.imageimport

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import fr.harmony.R
import fr.harmony.components.BottomBar
import fr.harmony.components.TopBar
import fr.harmony.ui.theme.AppTheme

/**
 * Crée un URI pour une nouvelle image dans le stockage externe.
 *
 * @param context Le contexte de l'application utilisé pour accéder au ContentResolver.
 * @return L'URI de l'image nouvellement créée.
 */
fun createImageUri(context: Context): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "camera_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
}

/**
 * Assombrit une couleur en appliquant un facteur donné.
 *
 * @param color La couleur à assombrir, représentée sous forme d'objet `Color`.
 * @param factor Le facteur d'assombrissement, compris entre 0.0 et 1.0.
 *               Un facteur de 0.0 ne modifie pas la couleur, tandis qu'un facteur de 1.0 rend la couleur complètement noire.
 * @return Une nouvelle couleur assombrie en fonction du facteur appliqué.
 * @throws IllegalArgumentException Si le facteur n'est pas compris entre 0.0 et 1.0.
 */
fun darkenColor(color: Color, factor: Float): Color {
    require(factor in 0f..1f) { "Le facteur doit être compris entre 0 et 1." }
    return Color(
        red = color.red * (1 - factor),
        green = color.green * (1 - factor),
        blue = color.blue * (1 - factor),
        alpha = color.alpha
    )
}

@Composable
fun ImportImageScreen(
    viewModel: ModelImport = hiltViewModel(),
    navController: NavController,
    onImageSelected: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { viewModel.onIntent(IntentImport.Selected(it)) }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                cameraUri?.let { viewModel.onIntent(IntentImport.Selected(it)) }
            }
        }

    val documentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { viewModel.copyImageToTemporaryFolder(context, uri)?.let { viewModel.onIntent(IntentImport.Selected(it)) }}
        }

    // Écoute les événements de navigation (ponctuels)
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                NavigationEventImport.OpenGallery -> galleryLauncher.launch("image/*")
                NavigationEventImport.OpenDocument -> documentLauncher.launch(arrayOf("image/*"))
                NavigationEventImport.OpenCamera -> {
                    val uri = createImageUri(context)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }

                is NavigationEventImport.NavigateHarmonize -> onImageSelected(event.uri)
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
            TopBar(returnAction = { navController.navigate("home") { popUpTo("import") { inclusive = true }} })

            Box(
                modifier = Modifier.padding(6.dp, 11.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.IMPORT_IMAGE_TITLE),
                    style = AppTheme.harmonyTypography.title,
                    color = AppTheme.harmonyColors.textColor,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Carte gauche : Galerie
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            AppTheme.harmonyColors.darkCard,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppTheme.harmonyColors.darkCardStroke,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .weight(2f)
                        .fillMaxHeight()
                        .clickable { viewModel.onIntent(IntentImport.OpenGallery) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.images),
                        contentDescription = stringResource(id = R.string.OPEN_GALLERY),
                        tint = AppTheme.harmonyColors.subtleTextColor,
                        modifier = Modifier.size(66.dp)
                    )
                }

                // Carte droite : Caméra et Document
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Caméra
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                AppTheme.harmonyColors.greenCeladon,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = darkenColor(AppTheme.harmonyColors.greenCeladon, 0.1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .weight(1f)
                            .clickable { viewModel.onIntent(IntentImport.OpenCamera) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_bold),
                            contentDescription = stringResource(id = R.string.OPEN_CAMERA),
                            tint = AppTheme.harmonyColors.darkerCard,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    // Document
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                AppTheme.harmonyColors.blueMunsell,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = darkenColor(AppTheme.harmonyColors.blueMunsell, 0.1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .weight(1f)
                            .clickable { viewModel.onIntent(IntentImport.OpenDocument) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cloud_arrow_down_bold),
                            contentDescription = stringResource(id = R.string.OPEN_CLOUD),
                            tint = AppTheme.harmonyColors.subtleTextColor,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            }
        }

        BottomBar(id = 1, navController)
    }
}