package dev.outspire.android.data.repository

import dev.outspire.android.BuildConfig
import dev.outspire.android.data.remote.TsimsClient

object ServiceLocator {
    val repository: OutspireRepository by lazy {
        DefaultOutspireRepository(TsimsClient(BuildConfig.TSIMS_BASE_URL))
    }
}
