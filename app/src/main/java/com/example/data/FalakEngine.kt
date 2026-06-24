package com.example.data

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

data class FalakState(
    val wisTime: String = "00:00:00",
    val dateText: String = "Memuat Falak..."
)

class FalakEngine {

    private val _falakState = MutableStateFlow(FalakState())
    val falakState: StateFlow<FalakState> = _falakState.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            updateFalak()
            handler.postDelayed(this, 1000)
        }
    }

    private val indonesianDays = mapOf(
        DayOfWeek.SUNDAY to "Ahad",
        DayOfWeek.MONDAY to "Senin",
        DayOfWeek.TUESDAY to "Selasa",
        DayOfWeek.WEDNESDAY to "Rabu",
        DayOfWeek.THURSDAY to "Kamis",
        DayOfWeek.FRIDAY to "Jumat",
        DayOfWeek.SATURDAY to "Sabtu"
    )

    private val hijrahMonths = listOf(
        "Muharram", "Safar", "Rabi'ul Awal", "Rabi'ul Akhir",
        "Jumadil Awal", "Jumadil Akhir", "Rajab", "Sya'ban",
        "Ramadhan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
    )

    init {
        handler.post(tickRunnable)
    }

    fun stop() {
        handler.removeCallbacks(tickRunnable)
    }

    private fun updateFalak() {
        try {
            // 1. Get current local date and time
            val nowTime = LocalTime.now()
            val nowDate = LocalDate.now()

            // 2. Waktu Istiwak (WIS) is 31 minutes ahead of local time
            val wisTime = nowTime.plusMinutes(31)

            // Format WIS Time as HH:mm:ss
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val wisTimeString = wisTime.format(timeFormatter)

            // 3. Islamic day and date transition at WIS 18:00 (Sunset/Maghrib)
            val isSunsetPassed = wisTime.hour >= 18

            val adjustedDate = if (isSunsetPassed) {
                nowDate.plusDays(1)
            } else {
                nowDate
            }

            // Get Day of Week
            val localDayOfWeek = adjustedDate.dayOfWeek
            val dayName = indonesianDays[localDayOfWeek] ?: "Ahad"
            val dayPrefix = if (isSunsetPassed) "Malam $dayName" else "Hari $dayName"

            // Convert to Hijri Date
            val hijrahDate = HijrahDate.from(adjustedDate)
            val hijriDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val hijriMonthIdx = hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1
            val hijriMonth = if (hijriMonthIdx in 0..11) hijrahMonths[hijriMonthIdx] else ""
            val hijriYear = hijrahDate.get(ChronoField.YEAR)

            val dateTextString = "$dayPrefix, $hijriDay $hijriMonth $hijriYear H"

            _falakState.value = FalakState(
                wisTime = wisTimeString,
                dateText = dateTextString
            )
        } catch (e: Exception) {
            _falakState.value = FalakState(
                wisTime = "--:--:--",
                dateText = "Gagal memproses Falak"
            )
        }
    }
}
