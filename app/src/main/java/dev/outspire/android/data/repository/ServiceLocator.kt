package dev.outspire.android.data.repository

import android.content.Context
import dev.outspire.android.BuildConfig
import dev.outspire.android.data.remote.TsimsClient

object ServiceLocator {
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    val repository: OutspireRepository by lazy {
        DefaultOutspireRepository(TsimsClient(BuildConfig.TSIMS_BASE_URL))
    }

    val scheduleSettings: ScheduleSettingsStore by lazy {
        check(::applicationContext.isInitialized) { "ServiceLocator.initialize must be called first." }
        ScheduleSettingsStore(applicationContext)
    }

    val credentialRecorder: CredentialRecorder by lazy {
        check(::applicationContext.isInitialized) { "ServiceLocator.initialize must be called first." }
        AndroidCredentialRecorder(applicationContext)
    }
}
