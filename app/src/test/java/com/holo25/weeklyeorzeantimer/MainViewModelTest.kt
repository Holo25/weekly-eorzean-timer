package com.holo25.weeklyeorzeantimer

import android.icu.text.RelativeDateTimeFormatter
import androidx.lifecycle.viewModelScope
import com.holo25.weeklyeorzeantimer.main.MainViewModel
import com.holo25.weeklyeorzeantimer.util.MainDispatcherRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class MainViewModelTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    lateinit var formatter: RelativeDateTimeFormatter

    private lateinit var viewModel: MainViewModel

    @Before
    fun init() {
        val durationSlot = slot<Double>()
        every {
            formatter.format(
                capture(durationSlot),
                any(),
                any()
            )
        } answers { "${durationSlot.captured.toInt()} days" } // Actual formatter uses whole numbers
    }

    @Test
    fun `returned weekly reset time is calculated correctly`() {
        // Reset time is 2023-09-05T08:00:00.00Z
        // Before reset
        initViewModel("2023-09-05T06:11:22.00Z")
        assertDateAndTime("0 days", "01:48:38")

        // Right after reset
        initViewModel("2023-09-05T09:00:00.00Z")
        assertDateAndTime("6 days", "23:00:00")

        //Mid-week, same time as reset
        initViewModel("2023-09-09T08:00:00.00Z")
        assertDateAndTime("3 days", "00:00:00")

        //Mid-week
        initViewModel("2023-09-09T09:00:00.00Z")
        assertDateAndTime("2 days", "23:00:00")

        // Next week
        initViewModel("2023-12-05T09:00:00.00Z")
        assertDateAndTime("6 days", "23:00:00")

        // Right on reset
        initViewModel("2023-09-05T08:00:00.00Z")
        assertDateAndTime("0 days", "00:00:00")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `returned time is updated every second`() = runTest(StandardTestDispatcher()) {
        /**
         * For mockedClock spyk would be more simple, but JDK 16+ Java classes requires workaround for spyk to work.
         * https://mockk.io/doc/md/jdk16-access-exceptions.html
         */
        val mockedClock = mockk<Clock>()
        every { mockedClock.instant() } returns Instant.parse("2023-09-09T08:00:00.00Z")
        every { mockedClock.zone } returns Clock.systemUTC().zone

        // Reset time is 2023-09-05T08:00:00.00Z
        viewModel = MainViewModel(mockedClock, formatter)
        assertDateAndTime("3 days", "00:00:00")

        every { mockedClock.instant() } returns Instant.parse("2023-09-09T08:00:01.00Z")
        // Result should not change before 1 second has passed
        assertDateAndTime("3 days", "00:00:00")

        advanceTimeBy(500)
        assertDateAndTime("3 days", "00:00:00")

        advanceTimeBy(1000)
        assertDateAndTime("2 days", "23:59:59")

        viewModel.viewModelScope.cancel()
    }

    private fun assertDateAndTime(date: String, time: String) {
        val result = viewModel.resetTime.value

        assertEquals(date, result.remainingDays)
        assertEquals(time, result.remainingTime)
    }

    private fun initViewModel(dateTime: String) {
        val clock = Clock.fixed(Instant.parse(dateTime), ZoneId.of("UTC"))

        viewModel = MainViewModel(clock, formatter)
    }
}
