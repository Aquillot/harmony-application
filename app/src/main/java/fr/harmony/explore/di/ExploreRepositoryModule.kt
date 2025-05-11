package fr.harmony.explore.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.explore.data.ExploreApi
import fr.harmony.explore.data.ExploreRepositoryImpl
import fr.harmony.explore.domain.ExploreRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExploreRepositoryModule {
    @Provides
    @Singleton
    fun provideExploreRepository(
        api: ExploreApi,
        moshi: Moshi
    ): ExploreRepository = ExploreRepositoryImpl(api, moshi)
}
