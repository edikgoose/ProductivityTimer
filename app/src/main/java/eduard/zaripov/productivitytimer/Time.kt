package eduard.zaripov.productivitytimer

data class Time(var minutes: Int = 0, var seconds: Int = 0, var limitSeconds: Int = Int.MAX_VALUE) {
    fun addOneSecond() {
        if (seconds == 59) {
            if (minutes == 59) {
                reset()
            } else {
                minutes++
            }
        } else {
            seconds++
        }
    }

    fun isExceededLimit() = minutes * 60 + seconds > limitSeconds

    fun reset() {
        minutes = 0
        seconds = 0
    }

    override fun toString() = String.format("%02d:%02d", minutes, seconds)

}
