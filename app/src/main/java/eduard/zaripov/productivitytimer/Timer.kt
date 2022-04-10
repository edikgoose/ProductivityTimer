package eduard.zaripov.productivitytimer

data class Timer(var minutes: Int = 0, var seconds: Int = 0) {
    var currentMinutes: Int = minutes
    var currentSeconds: Int = seconds
    var isUp: Boolean = false

    public fun decrease() {
        if (currentSeconds == 0) {
            if (currentMinutes == 0) {
                isUp = true
            }
            decreaseMinutes()
            currentSeconds = 59
        }
        currentSeconds--
    }

    private fun decreaseMinutes() {
        if (currentMinutes != 0) {
            currentMinutes--
        }
    }

    fun reset() {
        currentMinutes = minutes
        currentSeconds = seconds
        isUp = false
    }

    override fun toString() = String.format("%02d:%02d", minutes, seconds)
}
