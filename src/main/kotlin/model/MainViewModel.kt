package model

import java.util.prefs.Preferences

class MainViewModel {

    companion object {
        private const val LAST_PRINT_TIME_KEY = "lastPrintTime"
        private const val LAST_PERIOD_INDEX_KEY = "lastPeriodIndex"
    }

    private val pref = Preferences.userRoot().node(this.javaClass.name)

    fun getLastPrintingTime(): Long {
        val savedLastPrintTime = pref.getLong(LAST_PRINT_TIME_KEY, 0)
        return if (savedLastPrintTime == 0L) {
            System.currentTimeMillis()
        } else {
            savedLastPrintTime
        }
    }

    fun getLastPeriodIndex(): Int {
        return pref.getInt(LAST_PERIOD_INDEX_KEY, 0)
    }

    fun putLastPrintingTime() {
        val curTime = System.currentTimeMillis()
        pref.putLong(LAST_PRINT_TIME_KEY, curTime)
    }

    fun putLastPeriodIndex(index: Int) {
        pref.putInt(LAST_PERIOD_INDEX_KEY, index)
    }
}