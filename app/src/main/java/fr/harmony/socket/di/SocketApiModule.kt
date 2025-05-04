package fr.harmony.socket.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.harmony.socket.data.SocketApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketApiModule {
    @Provides
    @Singleton
    fun provideSocketApi(retrofit: Retrofit): SocketApi =
        retrofit.create(SocketApi::class.java)
}