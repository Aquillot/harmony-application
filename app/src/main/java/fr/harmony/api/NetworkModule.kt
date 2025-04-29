package fr.harmony.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.login.data.LoginApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Provide est une annotation de Dagger qui permet de créer des instances de classes
    // Singleton est une annotation de Dagger qui permet de créer une seule instance
    // de la classe pour toute l'application
    @Provides @Singleton
    fun provideOkHttpClient( authInterceptor: AuthInterceptor ): OkHttpClient {
        // HttpLoggingInterceptor est une bibliothèque qui permet de logger les requêtes et les réponses
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides @Singleton
    // Moshi est une bibliothèque qui permet de convertir des objets en JSON et inversement
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides @Singleton
    // Retrofit est une bibliothèque qui permet de faire des appels réseau
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://harmony.jhune.dev/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(client)
        .build()

}
