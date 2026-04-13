package com.computerization.outspire.feature.cas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.data.model.DomainRecord
import com.computerization.outspire.data.model.DomainReflection
import com.computerization.outspire.data.repository.CasRepository
import com.computerization.outspire.data.repository.YearRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CasViewModel @Inject constructor(
    private val repository: CasRepository,
    private val yearRepository: YearRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CasUiState())
    val state: StateFlow<CasUiState> = _state.asStateFlow()

    init {
        loadMyClubs()
    }

    fun selectTab(tab: CasTab) {
        if (_state.value.selectedTab == tab) return
        _state.update { it.copy(selectedTab = tab) }
        when (tab) {
            CasTab.MyClubs -> if (_state.value.myClubs is AsyncList.Error) loadMyClubs()
            CasTab.Browse -> if (_state.value.browse.items.isEmpty()) loadNextBrowsePage()
            CasTab.Evaluation -> if (_state.value.evaluation !is AsyncValue.Data) loadEvaluation()
        }
    }

    fun retryMyClubs() = loadMyClubs()
    fun retryEvaluation() = loadEvaluation()

    fun openGroup(group: DomainCasGroup) {
        _state.update {
            it.copy(
                selectedGroup = group,
                records = AsyncList.Loading,
                reflections = AsyncList.Loading,
            )
        }
        loadGroupDetail(group.id)
    }

    fun closeGroup() {
        _state.update { it.copy(selectedGroup = null) }
    }

    fun retryGroupDetail() {
        val id = _state.value.selectedGroup?.id ?: return
        _state.update { it.copy(records = AsyncList.Loading, reflections = AsyncList.Loading) }
        loadGroupDetail(id)
    }

    fun loadNextBrowsePage() {
        val current = _state.value.browse
        if (current.loading || (current.pageIndex > 0 && !current.hasMore)) return
        val next = current.pageIndex + 1
        _state.update { it.copy(browse = current.copy(loading = true, error = null)) }
        viewModelScope.launch {
            repository.allGroups(next)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            browse = BrowseState(
                                items = current.items + page.items,
                                pageIndex = page.pageIndex,
                                pageCount = page.pageCount,
                                loading = false,
                            )
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            browse = current.copy(
                                loading = false,
                                error = t.message ?: "Failed to load groups",
                            )
                        )
                    }
                }
        }
    }

    fun retryBrowse() {
        _state.update { it.copy(browse = BrowseState()) }
        loadNextBrowsePage()
    }

    fun join(group: DomainCasGroup) {
        if (_state.value.joiningId != null) return
        _state.update { it.copy(joiningId = group.id) }
        viewModelScope.launch {
            repository.join(group.id)
                .onSuccess { msg ->
                    _state.update { it.copy(joiningId = null, snackbar = msg.ifBlank { "Joined ${group.name}" }) }
                    loadMyClubs()
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(joiningId = null, snackbar = t.message ?: "Join failed")
                    }
                }
        }
    }

    fun consumeSnackbar() {
        _state.update { it.copy(snackbar = null) }
    }

    fun openAddRecord() {
        if (_state.value.selectedGroup == null) return
        _state.update { it.copy(recordEditor = RecordEditorState(date = todayString())) }
    }

    fun openEditRecord(record: DomainRecord) {
        _state.update {
            it.copy(
                recordEditor = RecordEditorState(
                    id = record.id,
                    date = record.date.ifBlank { todayString() },
                    theme = record.theme,
                    cDuration = record.cDuration,
                    aDuration = record.aDuration,
                    sDuration = record.sDuration,
                    reflection = record.reflection,
                )
            )
        }
    }

    fun updateRecordEditor(transform: (RecordEditorState) -> RecordEditorState) {
        _state.update { s ->
            s.recordEditor?.let { s.copy(recordEditor = transform(it).copy(error = null)) } ?: s
        }
    }

    fun closeRecordEditor() {
        _state.update { it.copy(recordEditor = null, savingEditor = false) }
    }

    fun saveRecord() {
        val editor = _state.value.recordEditor ?: return
        val group = _state.value.selectedGroup ?: return
        val err = validateRecord(editor)
        if (err != null) {
            _state.update { it.copy(recordEditor = editor.copy(error = err)) }
            return
        }
        _state.update { it.copy(savingEditor = true) }
        viewModelScope.launch {
            repository.saveRecord(
                id = editor.id,
                groupId = group.id,
                activityDate = editor.date,
                theme = editor.theme.trim(),
                cDuration = editor.cDuration,
                aDuration = editor.aDuration,
                sDuration = editor.sDuration,
                reflection = editor.reflection.trim(),
            ).onSuccess {
                _state.update {
                    it.copy(
                        recordEditor = null,
                        savingEditor = false,
                        snackbar = if (editor.isEdit) "Record updated" else "Record added",
                    )
                }
                loadGroupDetail(group.id)
            }.onFailure { t ->
                _state.update {
                    it.copy(
                        savingEditor = false,
                        recordEditor = editor.copy(error = t.message ?: "Save failed"),
                    )
                }
            }
        }
    }

    fun deleteRecord(record: DomainRecord) {
        val groupId = _state.value.selectedGroup?.id ?: return
        viewModelScope.launch {
            repository.deleteRecord(record.id)
                .onSuccess {
                    _state.update { it.copy(snackbar = "Record deleted") }
                    loadGroupDetail(groupId)
                }
                .onFailure { t ->
                    _state.update { it.copy(snackbar = t.message ?: "Delete failed") }
                }
        }
    }

    fun openAddReflection() {
        if (_state.value.selectedGroup == null) return
        _state.update { it.copy(reflectionEditor = ReflectionEditorState()) }
    }

    fun openEditReflection(reflection: DomainReflection) {
        _state.update {
            it.copy(
                reflectionEditor = ReflectionEditorState(
                    id = reflection.id,
                    title = reflection.title,
                    summary = reflection.summary,
                    content = reflection.contentPreview,
                    outcome = reflection.outcome?.code,
                )
            )
        }
    }

    fun updateReflectionEditor(transform: (ReflectionEditorState) -> ReflectionEditorState) {
        _state.update { s ->
            s.reflectionEditor?.let { s.copy(reflectionEditor = transform(it).copy(error = null)) } ?: s
        }
    }

    fun closeReflectionEditor() {
        _state.update { it.copy(reflectionEditor = null, savingEditor = false) }
    }

    fun saveReflection() {
        val editor = _state.value.reflectionEditor ?: return
        val group = _state.value.selectedGroup ?: return
        val err = validateReflection(editor)
        if (err != null) {
            _state.update { it.copy(reflectionEditor = editor.copy(error = err)) }
            return
        }
        _state.update { it.copy(savingEditor = true) }
        viewModelScope.launch {
            repository.saveReflection(
                id = editor.id,
                groupId = group.id,
                title = editor.title.trim(),
                summary = editor.summary.trim(),
                content = editor.content.trim(),
                outcome = editor.outcome,
            ).onSuccess {
                _state.update {
                    it.copy(
                        reflectionEditor = null,
                        savingEditor = false,
                        snackbar = if (editor.isEdit) "Reflection updated" else "Reflection added",
                    )
                }
                loadGroupDetail(group.id)
            }.onFailure { t ->
                _state.update {
                    it.copy(
                        savingEditor = false,
                        reflectionEditor = editor.copy(error = t.message ?: "Save failed"),
                    )
                }
            }
        }
    }

    fun deleteReflection(reflection: DomainReflection) {
        val groupId = _state.value.selectedGroup?.id ?: return
        viewModelScope.launch {
            repository.deleteReflection(reflection.id)
                .onSuccess {
                    _state.update { it.copy(snackbar = "Reflection deleted") }
                    loadGroupDetail(groupId)
                }
                .onFailure { t ->
                    _state.update { it.copy(snackbar = t.message ?: "Delete failed") }
                }
        }
    }

    private fun validateRecord(e: RecordEditorState): String? {
        if (e.theme.isBlank()) return "Theme is required"
        if (e.theme.length > 400) return "Theme must be ≤ 400 chars"
        if (e.date.isBlank()) return "Date is required"
        listOf(e.cDuration, e.aDuration, e.sDuration).forEach {
            it.toDoubleOrNull() ?: return "Durations must be numeric"
        }
        val words = e.reflection.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        if (words < 80) return "Reflection needs ≥ 80 words (currently $words)"
        return null
    }

    private fun validateReflection(e: ReflectionEditorState): String? {
        if (e.title.isBlank()) return "Title is required"
        if (e.title.length > 200) return "Title must be ≤ 200 chars"
        if (e.summary.length > 500) return "Summary must be ≤ 500 chars"
        if (e.content.isBlank()) return "Content is required"
        return null
    }

    private fun todayString(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "%04d-%02d-%02d".format(now.year, now.monthNumber, now.dayOfMonth)
    }

    private fun loadMyClubs() {
        _state.update { it.copy(myClubs = AsyncList.Loading) }
        viewModelScope.launch {
            repository.myGroups()
                .onSuccess { groups -> _state.update { it.copy(myClubs = AsyncList.Data(groups)) } }
                .onFailure { t ->
                    _state.update {
                        it.copy(myClubs = AsyncList.Error(t.message ?: "Failed to load clubs"))
                    }
                }
        }
    }

    private fun loadGroupDetail(groupId: String) {
        viewModelScope.launch {
            repository.records(groupId)
                .onSuccess { list -> _state.update { it.copy(records = AsyncList.Data(list)) } }
                .onFailure { t ->
                    _state.update { it.copy(records = AsyncList.Error(t.message ?: "Failed")) }
                }
        }
        viewModelScope.launch {
            repository.reflections(groupId)
                .onSuccess { list -> _state.update { it.copy(reflections = AsyncList.Data(list)) } }
                .onFailure { t ->
                    _state.update { it.copy(reflections = AsyncList.Error(t.message ?: "Failed")) }
                }
        }
    }

    private fun loadEvaluation() {
        _state.update { it.copy(evaluation = AsyncValue.Loading) }
        viewModelScope.launch {
            yearRepository.ensureOptions()
            val yearId = yearRepository.currentYearId.value
            if (yearId == null) {
                _state.update {
                    it.copy(evaluation = AsyncValue.Error("Select a term in Settings first"))
                }
                return@launch
            }
            repository.evaluation(yearId)
                .onSuccess { v -> _state.update { it.copy(evaluation = AsyncValue.Data(v)) } }
                .onFailure { t ->
                    _state.update {
                        it.copy(evaluation = AsyncValue.Error(t.message ?: "Failed to load evaluation"))
                    }
                }
        }
    }
}
