package fr.harmony.profile.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.profile.data.ProfileApi
import fr.harmony.profile.data.ProfileRepositoryImpl
import fr.harmony.profile.domain.ProfileRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileRepositoryModule {
    @Provides
    @Singleton
    fun provideProfileRepository(
        api: ProfileApi,
        moshi: com.squareup.moshi.Moshi
        ): ProfileRepository = ProfileRepositoryImpl(api, moshi)
}
