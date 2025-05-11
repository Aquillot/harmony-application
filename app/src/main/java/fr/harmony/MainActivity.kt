package fr.harmony

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import fr.harmony.api.TokenManager
import fr.harmony.components.SnackBar
import fr.harmony.explore.ExploreScreen
import fr.harmony.harmonize.HarmonizeImageScreen
import fr.harmony.imageimport.ImportImageScreen
import fr.harmony.login.mvi.LoginScreen
import fr.harmony.profile.mvi.ProfileScreen
import fr.harmony.register.mvi.RegisterScreen
import fr.harmony.ui.theme.HarmonyTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

// Le point d'entrée de l'application Harmony
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.insetsController?.setSystemBarsAppearance(
            0, // On supprime le flag APPEARANCE_LIGHT_STATUS_BARS
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarScope = rememberCoroutineScope()

            HarmonyTheme {
                val nav = rememberNavController()
                val startDestination = "explore" //TODO EDIT

                var username = ""
                var email = ""

                println(startDestination)
                SnackBar(snackbarHostState = snackbarHostState) {
                    NavHost(navController = nav, startDestination = startDestination) {
                        composable("profile") {
                            ProfileScreen(
                                onGetTokenSuccess = { newUsername, newEmail ->
                                    username = newUsername
                                    email = newEmail
                                    if (nav.currentDestination?.route == "profile") {
                                        nav.navigate("import") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToLogin = {
                                    nav.navigate("login") {
                                        if (nav.currentDestination?.route == "profile") {
                                            popUpTo("profile") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    if (nav.currentDestination?.route == "login") {
                                        nav.navigate("profile") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegister = {
                                    if (nav.currentDestination?.route == "login") {
                                        nav.navigate("register") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    if (nav.currentDestination?.route == "register") {
                                        nav.navigate("profile") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToLogin = {
                                    nav.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("import") { _ ->
                            ImportImageScreen(
                                navController = nav,
                                onImageSelected = { uri ->
                                    val encodedUri = URLEncoder.encode(
                                        uri.toString(),
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    nav.navigate("harmonize?uri=$encodedUri") {
                                        popUpTo("import") { inclusive = true }
                                        launchSingleTop = true
                                        restoreState = false
                                    }
                                },
                            )
                        }

                        composable(
                            route = "harmonize?uri={uri}",
                            arguments = listOf(
                                navArgument("uri") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val encodedUri = backStackEntry.arguments?.getString("uri")
                            val decodedUri = encodedUri?.let { URLDecoder.decode(it, "UTF-8").toUri() }

                            println(decodedUri)
                            if (decodedUri != null) {
                                HarmonizeImageScreen(
                                    navController = nav,
                                    snackbarHostState = snackbarHostState,
                                    snackbarScope = snackbarScope,
                                    imageUri = decodedUri,
                                )
                            }
                        }

                        composable("explore") {
                            ExploreScreen(
                                navController = nav,
                                snackbarHostState = snackbarHostState,
                                snackbarScope = snackbarScope,
                            )
                        }

                        composable("home") { backStackEntry ->
                            HomeScreen(username)
                        }
                    }
                }
            }
        }
    }
}
