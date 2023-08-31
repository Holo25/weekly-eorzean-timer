package com.holo25.weeklyeorzeantimer

import android.icu.text.RelativeDateTimeFormatter
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

    // TODO Add DI
    private val clock = Clock.systemUTC()
    //private val formatter = RelativeDateTimeFormatter.getInstance()

    private val _time = MutableStateFlow(formattedTime())

    val time: StateFlow<String> = _time.asStateFlow()

    init {
        viewModelScope.launch {
            //TODO refine this infinite cycle
            while (true) {
                _time.value = getTimeToWeeklyReset()
                delay(1000 * 60)
            }
        }
    }

    private fun getTimeToWeeklyReset(): String {
        // TODO Decide on which implementation to use
        var remainingTime = ""
        val remainingTimeDuration = getRemainingTimeUntilReset()
//        val remainingDays = formatter.format(
//            remainingTimeDuration.toDays().toDouble(),
//            RelativeDateTimeFormatter.Direction.NEXT,
//            RelativeUnit.DAYS
//        )
//        val remainingHours = formatter.format(
//            (remainingTimeDuration.toHours() % 24).toDouble(),
//            RelativeDateTimeFormatter.Direction.NEXT,
//            RelativeUnit.HOURS
//        )
//        val remainingMinutes = formatter.format(
//            (remainingTimeDuration.toMinutes() % 60).toDouble(),
//            RelativeDateTimeFormatter.Direction.NEXT,
//            RelativeUnit.MINUTES
//        )
//
//        return formatter.combineDateAndTime(remainingDays, "$remainingHours, $remainingMinutes")
        if (remainingTimeDuration.toDays() > 0) remainingTime += "${remainingTimeDuration.toDays()}D "
        remainingTime += "${(remainingTimeDuration.toHours() % 24)}H "
        remainingTime += "${remainingTimeDuration.toMinutes() % 60}S"

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