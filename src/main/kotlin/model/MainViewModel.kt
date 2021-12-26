package model

import androidx.compose.runtime.mutableStateOf
import java.util.prefs.Preferences

class MainViewModel {

    companion object {
        private const val LAST_PRINT_TIME_KEY = "lastPrintTime"
    }

    val lastPrintTime = mutableStateOf(0L)
    private val pref = Preferences.userRoot().node(this.javaClass.name)

    init {
        lastPrintTime.value = getLastPrintingTime()
    }

    fun getLastPrintingTime(): Long {
        val savedLastPrintTime = pref.getLong(LAST_PRINT_TIME_KEY, 0)
        return if (savedLastPrintTime == 0L) {
            System.currentTimeMillis()
        } else {
            savedLastPrintTime
        }.also {
            lastPrintTime.value = it
        }
    }

    fun putLastPrintingTIme() {
        val curTime = System.currentTimeMillis()
        pref.putLong(LAST_PRINT_TIME_KEY, curTime)
        lastPrintTime.value = curTime
    }
}