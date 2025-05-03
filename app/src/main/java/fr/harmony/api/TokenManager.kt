package fr.harmony.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
    }

    val token: Flow<String?> = context.dataStore.data
        .map { it[TOKEN_KEY] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    fun getTokenSync(): String? {
        return runBlocking {
            context.dataStore.data.map { it[TOKEN_KEY] }.first()
        }
    }

}