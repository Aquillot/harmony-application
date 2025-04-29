package fr.harmony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.harmony.ui.theme.HarmonyTheme
import fr.harmony.login.mvi.LoginScreen
import fr.harmony.api.TokenManager
import fr.harmony.register.mvi.RegisterScreen
import javax.inject.Inject

// Le point d'entrée de l'application Harmony
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            HarmonyTheme {
                val nav = rememberNavController()
                tokenManager.clearToken()
                var token = tokenManager.getToken()
                val startDestination = if (token == null) "register" else "home"

                println(startDestination)
                println(token)
                NavHost(navController = nav, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen (onLoginSuccess ={ newToken ->
                            tokenManager.saveToken(newToken)
                            token = newToken
                            if (nav.currentDestination?.route == "login") {
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                            onNavigateToRegister = {
                                nav.navigate("register") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { newToken ->
                                tokenManager.saveToken(newToken)
                                token = newToken
                                if (nav.currentDestination?.route == "register") {
                                    nav.navigate("home") {
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
                        HomeScreen(token = token)
                    }
                }
            }
        }
    }
}
