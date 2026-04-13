package com.computerization.outspire.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureCredentialStore @Inject constructor(context: Context) {

    private val prefs: SharedPreferences = run {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "outspire_secure_prefs",
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) { prefs.edit().putString(KEY_USERNAME, value).apply() }

    var password: String?
        get() = prefs.getString(KEY_PASSWORD, null)
        set(value) { prefs.edit().putString(KEY_PASSWORD, value).apply() }

    var studentId: String?
        get() = prefs.getString(KEY_STUDENT_ID, null)
        set(value) { prefs.edit().putString(KEY_STUDENT_ID, value).apply() }

    var cookieBlob: String?
        get() = prefs.getString(KEY_COOKIES, null)
        set(value) { prefs.edit().putString(KEY_COOKIES, value).apply() }

    var currentYearId: String?
        get() = prefs.getString(KEY_CURRENT_YEAR_ID, null)
        set(value) { prefs.edit().putString(KEY_CURRENT_YEAR_ID, value).apply() }

    var cachedYearOptions: String?
        get() = prefs.getString(KEY_YEAR_OPTIONS, null)
        set(value) { prefs.edit().putString(KEY_YEAR_OPTIONS, value).apply() }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasCredentials(): Boolean = !username.isNullOrBlank() && !password.isNullOrBlank()

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_STUDENT_ID = "student_id"
        private const val KEY_COOKIES = "cookies"
        private const val KEY_CURRENT_YEAR_ID = "current_year_id"
        private const val KEY_YEAR_OPTIONS = "year_options_json"
    }
}
