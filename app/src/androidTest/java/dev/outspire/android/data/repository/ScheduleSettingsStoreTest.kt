package dev.outspire.android.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleSettingsStoreTest {
    @Test
    fun holidaySettingsFollowTheSelectedAccount() {
        val store = ScheduleSettingsStore(ApplicationProvider.getApplicationContext<Context>())
        val suffix = System.nanoTime()
        val first = "settings-test-first-$suffix"
        val second = "settings-test-second-$suffix"

        store.selectAccount(first)
        store.setHolidayEnabled(true)
        assertTrue(store.settings.value.holidayEnabled)

        store.selectAccount(second)
        assertFalse(store.settings.value.holidayEnabled)

        store.selectAccount(first)
        assertTrue(store.settings.value.holidayEnabled)

        store.selectAccount(null)
        assertFalse(store.settings.value.holidayEnabled)
    }
}
