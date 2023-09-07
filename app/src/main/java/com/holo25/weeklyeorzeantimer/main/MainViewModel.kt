package com.holo25.weeklyeorzeantimer.main

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val clock: Clock,
    private val formatter: RelativeDateTimeFormatter
) : ViewModel() {

    companion object {
        const val REFRESH_INTERVAL: Long = 1000 // ms

        const val HOURS_IN_DAY = 24
        const val MINUTES_IN_HOUR = 60
        const val SECONDS_IN_MINUTES = 60

        const val WEEKLY_RESET_HOUR = 8
    }

    private val _resetTime = MutableStateFlow(getTimeToWeeklyReset())

    val resetTime: StateFlow<ResetTime> = _resetTime.asStateFlow()

    init {
        viewModelScope.launch {
            //TODO refine this infinite cycle
            while (true) {
                delay(REFRESH_INTERVAL)
                _resetTime.value = getTimeToWeeklyReset()
            }
        }
    }

    private fun getTimeToWeeklyReset(): ResetTime {
        val remainingDuration = getRemainingTimeUntilWeeklyReset()
        val remainingDays = formatter.format(
            remainingDuration.toDays().toDouble(),
            Direction.NEXT,
            RelativeUnit.DAYS
        )

        @Suppress("ImplicitDefaultLocale")
        val remainingTime = String.format(
            "%02d:%02d:%02d",
            remainingDuration.toHours() % HOURS_IN_DAY,
            remainingDuration.toMinutes() % MINUTES_IN_HOUR,
            remainingDuration.seconds % SECONDS_IN_MINUTES
        )

        return ResetTime(remainingDays, remainingTime)
    }

    private fun getRemainingTimeUntilWeeklyReset(): Duration {
        val currentTime = ZonedDateTime.now(clock)

        // Weekly reset time is at 08:00 UTC every Tuesday
        val nextResetTime = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
            .with(LocalTime.of(WEEKLY_RESET_HOUR, 0, 0))

        val duration = Duration.between(currentTime, nextResetTime)

        // Using .nextOrSame(...) only works pre-reset time on the day of the reset.
        // After the reset we need to use .next(...) to get the next week's reset time.
        return if (duration.isNegative) {
            Duration.between(currentTime, nextResetTime.with(TemporalAdjusters.next(DayOfWeek.TUESDAY)))
        } else {
            duration
        }
    }
}
