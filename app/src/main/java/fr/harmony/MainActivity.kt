package fr.harmony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.harmony.ui.theme.MyApplicationTheme
import fr.harmony.login.mvi.LoginScreen
import fr.harmony.api.TokenManager
import javax.inject.Inject

// Le point d'entrée de l'application Harmony
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MyApplicationTheme {
                val nav = rememberNavController()
                // Determination de la destination de départ
                var startDestination = "home"
                var token = tokenManager.getToken()
                if (token == null) {
                    startDestination = "login"
                }
                println(startDestination)
                println(token)
                NavHost(navController = nav, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen { newToken ->
                            tokenManager.saveToken(newToken)
                            token = newToken
                            if (nav.currentDestination?.route == "login") {
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    }
                    composable("home") { backStackEntry ->
                        HomeScreen(token = token)
                    }
                }
            }
        }
    }
}
