package eduard.zaripov.productivitytimer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimerViewModel(): ViewModel() {
    var initialTime = 5
    private val _timer: MutableLiveData<Int> = MutableLiveData()
    val timer: LiveData<Int> = _timer

    private var isTimerRunning = false

    init {
        _timer.value = initialTime
    }

    fun startTimer() = viewModelScope.launch {
        isTimerRunning = true
        createTimerFlow()
            .takeWhile {
                isTimerRunning
            }
            .collect { }
    }

    fun stopTimer() = viewModelScope.launch {
        isTimerRunning = false
        _timer.value = initialTime
    }

    fun updateTimer(seconds: Int) = viewModelScope.launch {
        _timer.value = seconds
    }

    private fun createTimerFlow(): Flow<Int?> = flow {
        while (true) {
            if (isTimerRunning) {
                _timer.value = _timer.value?.minus(1)
                emit(_timer.value)
                delay(1000)
            }
        }
    }
}