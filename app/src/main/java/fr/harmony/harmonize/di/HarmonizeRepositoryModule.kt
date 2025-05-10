package fr.harmony.harmonize.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.harmonize.data.HarmonizeRepositoryImpl
import fr.harmony.harmonize.domain.HarmonizeRepository
import fr.harmony.harmonize.data.HarmonizeApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HarmonizeRepositoryModule {
    @Provides
    @Singleton
    fun provideHarmonizeRepository(
        api: HarmonizeApi,
        moshi: Moshi
    ): HarmonizeRepository = HarmonizeRepositoryImpl(api, moshi)
}
