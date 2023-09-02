package com.holo25.weeklyeorzeantimer

import com.holo25.weeklyeorzeantimer.main.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MainViewModelTest {

    private val viewModel = MainViewModel()

    @Test
    fun `returned weekly reset time is correct`() {
        //TODO Implement mocking

        assertEquals("1 days, 10:23:12", viewModel.resetTime.value)
    }
}