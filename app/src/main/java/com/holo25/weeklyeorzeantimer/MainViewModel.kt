package com.holo25.weeklyeorzeantimer

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
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.GregorianCalendar
import java.util.Locale

class MainViewModel : ViewModel() {

    // TODO Add DI framework for these
    private val clock = Clock.systemUTC()
    private val formatter = RelativeDateTimeFormatter.getInstance()

    private val _time = MutableStateFlow(formattedTime())

    val time: StateFlow<String> = _time.asStateFlow()

    init {
        viewModelScope.launch {
            //TODO refine this infinite cycle
            while (true) {
                _time.value = getTimeToWeeklyReset()
                delay(1000)
            }
        }
    }

    private fun getTimeToWeeklyReset(): String {
        var remainingTime = ""
        val remainingDuration = getRemainingTimeUntilReset()
        val remainingDays = formatter.format(
            remainingDuration.toDays().toDouble(),
            Direction.NEXT,
            RelativeUnit.DAYS
        )

        if (remainingDuration.toDays() > 0) remainingTime += "$remainingDays, "
        remainingTime += "${(remainingDuration.toHours() % 24)}" +
                ":${remainingDuration.toMinutes() % 60}" +
                ":${remainingDuration.seconds % 60}"

        return remainingTime
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

    private fun formattedTime() = SimpleDateFormat("H:mm:ss", Locale.ENGLISH)
        .format(GregorianCalendar.getInstance().time)
}