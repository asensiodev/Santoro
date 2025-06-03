package com.asensiodev.santoro.core.data.di

import com.asensiodev.core.domain.dispatcher.DispatcherProvider
import com.asensiodev.santoro.core.data.dispatcher.DefaultDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
}
