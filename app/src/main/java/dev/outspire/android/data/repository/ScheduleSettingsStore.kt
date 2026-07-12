package dev.outspire.android.data.repository

import android.content.Context
import dev.outspire.android.data.model.ScheduleSettings
import dev.outspire.android.data.model.SchoolTime
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScheduleSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("schedule-settings", Context.MODE_PRIVATE)
    private var activeAccount: String? = null
    private val mutableSettings = MutableStateFlow(ScheduleSettings())
    val settings: StateFlow<ScheduleSettings> = mutableSettings.asStateFlow()

    fun selectAccount(code: String?) {
        val account = code?.trim()?.lowercase()?.takeIf(String::isNotEmpty)
        if (activeAccount == account) return
        activeAccount = account
        mutableSettings.value = account?.let(::readSettings) ?: ScheduleSettings()
    }

    fun setDayOverride(day: DayOfWeek?) = update { it.copy(dayOverride = day) }

    fun setHolidayEnabled(enabled: Boolean) = update { it.copy(holidayEnabled = enabled) }

    fun setHolidayEndDateEnabled(enabled: Boolean) = update {
        it.copy(holidayEndDateEnabled = enabled)
    }

    fun setHolidayEndDate(date: LocalDate) = update { it.copy(holidayEndDate = date) }

    fun setShowFutureCountdown(enabled: Boolean) = update {
        it.copy(showFutureCountdown = enabled)
    }

    private fun update(transform: (ScheduleSettings) -> ScheduleSettings) {
        if (activeAccount == null) return
        mutableSettings.update(transform)
        persist(activeAccount.orEmpty(), mutableSettings.value)
    }

    private fun readSettings(account: String): ScheduleSettings {
        val day = preferences.getString(key(account, KEY_DAY_OVERRIDE), null)
            ?.let { value -> runCatching { DayOfWeek.valueOf(value) }.getOrNull() }
        val storedEndDate = preferences.getLong(key(account, KEY_HOLIDAY_END_DATE), Long.MIN_VALUE)
        return ScheduleSettings(
            dayOverride = day,
            holidayEnabled = preferences.getBoolean(key(account, KEY_HOLIDAY_ENABLED), false),
            holidayEndDateEnabled = preferences.getBoolean(key(account, KEY_HOLIDAY_END_ENABLED), false),
            holidayEndDate = if (storedEndDate == Long.MIN_VALUE) {
                SchoolTime.now().toLocalDate().plusMonths(1)
            } else {
                LocalDate.ofEpochDay(storedEndDate)
            },
            showFutureCountdown = preferences.getBoolean(key(account, KEY_FUTURE_COUNTDOWN), true),
        )
    }

    private fun persist(account: String, settings: ScheduleSettings) {
        preferences.edit()
            .putString(key(account, KEY_DAY_OVERRIDE), settings.dayOverride?.name)
            .putBoolean(key(account, KEY_HOLIDAY_ENABLED), settings.holidayEnabled)
            .putBoolean(key(account, KEY_HOLIDAY_END_ENABLED), settings.holidayEndDateEnabled)
            .putLong(key(account, KEY_HOLIDAY_END_DATE), settings.holidayEndDate.toEpochDay())
            .putBoolean(key(account, KEY_FUTURE_COUNTDOWN), settings.showFutureCountdown)
            .apply()
    }

    private fun key(account: String, setting: String): String = "account:$account:$setting"

    private companion object {
        const val KEY_DAY_OVERRIDE = "day-override"
        const val KEY_HOLIDAY_ENABLED = "holiday-enabled"
        const val KEY_HOLIDAY_END_ENABLED = "holiday-end-enabled"
        const val KEY_HOLIDAY_END_DATE = "holiday-end-date"
        const val KEY_FUTURE_COUNTDOWN = "future-countdown"
    }
}
