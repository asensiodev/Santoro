package com.asensiodev.santoro.observability

import android.content.Context
import com.asensiodev.core.domain.observability.ObservabilityTracker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ObservabilityModule {
    @Binds
    fun bindObservabilityTracker(tracker: FirebaseObservabilityTracker): ObservabilityTracker
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseObservabilityModule {
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context,
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
}
