package com.holo25.weeklyeorzeantimer.main

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

class MainViewModel : ViewModel() {

    // TODO Add DI framework for these
    private val clock = Clock.systemUTC()
    private val formatter = RelativeDateTimeFormatter.getInstance()

    private val _resetTime = MutableStateFlow(getTimeToWeeklyReset())

    val resetTime: StateFlow<ResetTime> = _resetTime.asStateFlow()

    init {
        viewModelScope.launch {
            //TODO refine this infinite cycle
            while (true) {
                delay(1000)
                _resetTime.value = getTimeToWeeklyReset()
            }
        }
    }

    private fun getTimeToWeeklyReset(): ResetTime {
        val remainingDuration = getRemainingTimeUntilReset()
        val remainingDays = formatter.format(
            remainingDuration.toDays().toDouble(),
            Direction.NEXT,
            RelativeUnit.DAYS
        )
        val remainingTime = String.format(
            "%d:%02d:%02d",
            remainingDuration.toHours() % 24,
            remainingDuration.toMinutes() % 60,
            remainingDuration.seconds % 60
        )

        return ResetTime(remainingDays, remainingTime)
    }

    private fun getRemainingTimeUntilReset(): Duration {
        val currentTime = ZonedDateTime.now(clock)
        // Weekly reset time is at 08:00 UTC every Tuesday
        val nextResetTime = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
            .withHour(8)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        return Duration.between(currentTime, nextResetTime)
    }
}