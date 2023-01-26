package utils

object PingCalc {

    private var firstPing: Long = 0

    fun start() {
        firstPing = System.currentTimeMillis()
    }

    fun end(): Long {
        return System.currentTimeMillis() - firstPing
    }

}