package fr.harmony.socket.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.socket.data.SocketRepositoryImpl
import fr.harmony.socket.domain.SocketRepository
import fr.harmony.socket.data.SocketApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketRepositoryModule {
    @Provides
    @Singleton
    fun provideSocketRepository(
        api: SocketApi,
    ): SocketRepository = SocketRepositoryImpl(api)
}