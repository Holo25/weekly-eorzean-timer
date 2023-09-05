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
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val clock: Clock,
    private val formatter: RelativeDateTimeFormatter
) : ViewModel() {

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
        val remainingDuration = getRemainingTimeUntilWeeklyReset()
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

    private fun getRemainingTimeUntilWeeklyReset(): Duration {
        val currentTime = ZonedDateTime.now(clock)
        // Weekly reset time is at 08:00 UTC every Tuesday
        val nextResetTime = currentTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
            .withHour(8)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

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