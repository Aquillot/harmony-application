package fr.harmony.register.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.register.data.RegisterApi
import fr.harmony.register.data.RegisterRepositoryImpl
import fr.harmony.register.domain.RegisterRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RegisterRepositoryModule {
    @Provides
    @Singleton
    fun provideRegisterRepository(
        api: RegisterApi,
        moshi: com.squareup.moshi.Moshi
        ): RegisterRepository = RegisterRepositoryImpl(api, moshi)
}
