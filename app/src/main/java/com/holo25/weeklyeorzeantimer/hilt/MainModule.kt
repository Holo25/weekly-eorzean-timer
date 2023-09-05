package com.holo25.weeklyeorzeantimer.hilt

import android.icu.text.RelativeDateTimeFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import java.time.Clock

@Module
@InstallIn(ViewModelComponent::class)
object MainModule {

    @Provides
    fun provideDefaultClock(): Clock = Clock.systemUTC()

    @Provides
    fun provideRelativeDateTimeFormatter(): RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()
}