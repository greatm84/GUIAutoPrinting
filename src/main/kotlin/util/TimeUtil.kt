package util

import java.text.SimpleDateFormat

object TimeUtil {

    private const val MS = 1000L
    private const val MIN = 60 * MS
    private const val HOUR = 60 * MIN
    private const val DAY = 24 * HOUR
    private const val WEEK = 7 * DAY

    fun isInTime(fromTime: Long, period: Long): Boolean {
        val curTime = System.currentTimeMillis()
        return period > (curTime - fromTime)
    }

    fun isInWeek(fromTime: Long): Boolean {
        return isInTime(fromTime, WEEK)
    }

    fun isIn2Weeks(fromTime: Long): Boolean {
        return isInTime(fromTime, 2 * WEEK)
    }

    fun isIn3Weeks(fromTime: Long): Boolean {
        return isInTime(fromTime, 3 * WEEK)
    }

    fun formatTime(timeMs: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(timeMs)
    }
}