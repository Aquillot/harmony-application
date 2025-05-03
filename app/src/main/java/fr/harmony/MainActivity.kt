package fr.harmony

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.harmony.ui.theme.HarmonyTheme
import fr.harmony.login.mvi.LoginScreen
import fr.harmony.api.TokenManager
import fr.harmony.profile.mvi.ProfileScreen
import fr.harmony.register.mvi.RegisterScreen
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
            HarmonyTheme {
                val nav = rememberNavController()
                val startDestination = "profile"

                var username = ""
                var email = ""

                println(startDestination)
                NavHost(navController = nav, startDestination = startDestination) {
                    composable("profile") {
                        ProfileScreen(
                            onGetTokenSuccess = { newUsername, newEmail ->
                                username = newUsername
                                email = newEmail
                                if (nav.currentDestination?.route == "profile") {
                                    nav.navigate("home") {
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
                        LoginScreen (onLoginSuccess = {
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

                    composable("home") { backStackEntry ->
                        HomeScreen(username)
                    }
                }
            }
        }
    }
}
