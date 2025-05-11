package fr.harmony.userImages.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.userImages.data.UserImagesApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserImagesApiModule {
    @Provides
    @Singleton
    fun provideUserImagesApi(retrofit: Retrofit): UserImagesApi =
        retrofit.create(UserImagesApi::class.java)
}