package utils

class Time(time: String) {
    init {
        Companion.time
        ms(time)
    }

    companion object {
        val time: Long
            get() = System.currentTimeMillis()

        fun ms(_time: String): Long {
            var time = _time
            if (time.endsWith("s")) {
                time = time.replace("s", "")
                return try {
                    time.toLong() * 1000
                } catch (e: Exception) {
                    0
                }
            }
            if (time.endsWith("m")) {
                time = time.replace("m", "")
                return try {
                    time.toLong() * 60000
                } catch (e: Exception) {
                    0
                }
            }
            if (time.endsWith("h")) {
                time = time.replace("h", "")
                return try {
                    time.toLong() * 3600000
                } catch (e: Exception) {
                    0
                }
            }
            if (time.endsWith("d")) {
                time = time.replace("d", "")
                return try {
                    time.toLong() * 86400000
                } catch (e: Exception) {
                    0
                }
            }
            return try {
                time.toLong()
            } catch (e: Exception) {
                0
            }
        }
    }
}