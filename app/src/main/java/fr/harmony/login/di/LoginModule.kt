package fr.harmony.login.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.login.data.LoginApi
import fr.harmony.login.data.LoginRepositoryImpl
import fr.harmony.login.domain.LoginRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {
    @Provides
    @Singleton
    fun provideLoginRepository(
        api: LoginApi
    ): LoginRepository = LoginRepositoryImpl(api)
}
