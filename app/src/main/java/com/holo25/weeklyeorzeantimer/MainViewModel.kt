package com.holo25.weeklyeorzeantimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class MainViewModel : ViewModel() {

    private val _time = MutableStateFlow(formattedTime())
    val time: StateFlow<String> = _time.asStateFlow()

    init {
        viewModelScope.launch {
            //TODO refine this infinite cycle
            while (true) {
                _time.value = formattedTime()
                delay(1000)
            }
        }
    }

    private fun formattedTime() = SimpleDateFormat("H:mm:ss", Locale.ENGLISH)
        .format(GregorianCalendar.getInstance().time)
}