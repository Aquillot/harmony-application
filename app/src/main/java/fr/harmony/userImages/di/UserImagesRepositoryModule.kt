package fr.harmony.userImages.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.userImages.data.UserImagesApi
import fr.harmony.userImages.data.UserImagesRepositoryImpl
import fr.harmony.userImages.domain.UserImagesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserImagesRepositoryModule {
    @Provides
    @Singleton
    fun provideUserImagesRepository(
        api: UserImagesApi,
        moshi: Moshi
    ): UserImagesRepository = UserImagesRepositoryImpl(api, moshi)
}