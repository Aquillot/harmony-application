package fr.harmony.api

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}


class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "auth_token"
    }

    // Sauvegarder le token
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    // Récupérer le token
    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    // Supprimer le token
    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }
}

