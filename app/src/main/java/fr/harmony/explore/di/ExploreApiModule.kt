package fr.harmony.explore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.explore.data.ExploreApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExploreApiModule {
    @Provides
    @Singleton
    fun provideExploreApi(retrofit: Retrofit): ExploreApi =
        retrofit.create(ExploreApi::class.java)
}