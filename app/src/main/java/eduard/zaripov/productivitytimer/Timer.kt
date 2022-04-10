package eduard.zaripov.productivitytimer

data class Timer(var minutes: Int = 0, var seconds: Int = 0) {
    var currentMinutes: Int = minutes
    var currentSeconds: Int = seconds

    public fun decrease(): Boolean {
        if (currentSeconds == 0) {
            if (currentMinutes == 0) {
                return false
            }
            decreaseMinutes()
            currentSeconds = 59
        }
        currentSeconds--
        return true
    }

    private fun decreaseMinutes() {
        if (currentMinutes != 0) {
            currentMinutes--
        }
    }

    fun reset() {
        currentMinutes = minutes
        currentSeconds = seconds
    }

    override fun toString() = String.format("%02d:%02d", minutes, seconds)
}
