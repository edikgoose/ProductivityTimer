package eduard.zaripov.productivitytimer

data class Timer(private var minutes: Int = 0, private var seconds: Int = 0) {
    var currentMinutes: Int = minutes
    var currentSeconds: Int = seconds
    var isUp: Boolean = false

    public fun decrease() {
        if (currentSeconds == 0) {
            if (currentMinutes == 0) {
                isUp = true
            }
            currentMinutes--
            currentSeconds = 59
        }
        else {
            currentSeconds--
        }
    }

    fun reset() {
        currentMinutes = minutes
        currentSeconds = seconds
        isUp = false
    }

    fun updateTimer(minutes: Int, seconds: Int) {
        this.minutes = minutes
        this.seconds = seconds
        reset()
    }

    override fun toString() = String.format("%02d:%02d", currentMinutes, currentSeconds)
}
