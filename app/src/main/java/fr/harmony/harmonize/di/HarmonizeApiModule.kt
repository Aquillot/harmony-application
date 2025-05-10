package fr.harmony.harmonize.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.harmonize.data.HarmonizeApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HarmonizeApiModule {
    @Provides
    @Singleton
    fun provideHarmonizeApi(retrofit: Retrofit): HarmonizeApi =
        retrofit.create(HarmonizeApi::class.java)
}
