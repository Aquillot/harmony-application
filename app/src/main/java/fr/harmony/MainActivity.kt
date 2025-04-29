package fr.harmony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.harmony.ui.theme.MyApplicationTheme
import fr.harmony.login.mvi.LoginScreen
import fr.harmony.api.TokenManager

// Le point d'entrée de l'application Harmony
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val tokenManager = TokenManager(this)
        tokenManager.clearToken()
        var token = tokenManager.getToken()

        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val nav = rememberNavController()
                // Determination de la destination de départ
                var startDestination = "home"
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
