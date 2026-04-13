package com.computerization.outspire.data.repository

import com.computerization.outspire.data.local.SecureCredentialStore
import com.computerization.outspire.data.remote.YearService
import com.computerization.outspire.data.remote.dto.YearOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YearRepository @Inject constructor(
    private val service: YearService,
    private val store: SecureCredentialStore,
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(YearOption.serializer())
    private val mutex = Mutex()

    private val _currentYearId = MutableStateFlow(store.currentYearId)
    val currentYearId: StateFlow<String?> = _currentYearId.asStateFlow()

    suspend fun ensureOptions(forceRefresh: Boolean = false): Result<List<YearOption>> =
        runCatching {
            mutex.withLock {
                if (!forceRefresh) {
                    cachedOptions()?.let { cached ->
                        if (_currentYearId.value == null) {
                            cached.firstOrNull()?.let { setCurrentYearIdInternal(it.id) }
                        }
                        return@withLock cached
                    }
                }
                val fresh = service.fetchYearOptions()
                store.cachedYearOptions = json.encodeToString(listSerializer, fresh)
                if (_currentYearId.value == null) {
                    fresh.firstOrNull()?.let { setCurrentYearIdInternal(it.id) }
                }
                fresh
            }
        }

    fun cachedOptions(): List<YearOption>? {
        val blob = store.cachedYearOptions ?: return null
        return runCatching { json.decodeFromString(listSerializer, blob) }.getOrNull()
    }

    fun setCurrentYearId(id: String) {
        setCurrentYearIdInternal(id)
    }

    private fun setCurrentYearIdInternal(id: String) {
        store.currentYearId = id
        _currentYearId.value = id
    }
}
