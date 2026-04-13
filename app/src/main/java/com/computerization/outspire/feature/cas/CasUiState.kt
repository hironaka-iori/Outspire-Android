package com.computerization.outspire.feature.cas

import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.data.model.DomainEvaluation
import com.computerization.outspire.data.model.DomainRecord
import com.computerization.outspire.data.model.DomainReflection

enum class CasTab { MyClubs, Browse, Evaluation }

data class CasUiState(
    val selectedTab: CasTab = CasTab.MyClubs,
    val myClubs: AsyncList<DomainCasGroup> = AsyncList.Loading,
    val browse: BrowseState = BrowseState(),
    val evaluation: AsyncValue<DomainEvaluation> = AsyncValue.Loading,
    val selectedGroup: DomainCasGroup? = null,
    val records: AsyncList<DomainRecord> = AsyncList.Loading,
    val reflections: AsyncList<DomainReflection> = AsyncList.Loading,
    val joiningId: String? = null,
    val snackbar: String? = null,
    val recordEditor: RecordEditorState? = null,
    val reflectionEditor: ReflectionEditorState? = null,
    val savingEditor: Boolean = false,
)

data class RecordEditorState(
    val id: String = "",
    val date: String = "",
    val theme: String = "",
    val cDuration: String = "0",
    val aDuration: String = "0",
    val sDuration: String = "0",
    val reflection: String = "",
    val error: String? = null,
) {
    val isEdit: Boolean get() = id.isNotBlank() && id != "0"
}

data class ReflectionEditorState(
    val id: String = "",
    val title: String = "",
    val summary: String = "",
    val content: String = "",
    val outcome: Int? = null,
    val error: String? = null,
) {
    val isEdit: Boolean get() = id.isNotBlank() && id != "0"
}

data class BrowseState(
    val items: List<DomainCasGroup> = emptyList(),
    val pageIndex: Int = 0,
    val pageCount: Int = 1,
    val loading: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = pageIndex < pageCount
}

sealed interface AsyncList<out T> {
    data object Loading : AsyncList<Nothing>
    data class Error(val message: String) : AsyncList<Nothing>
    data class Data<T>(val items: List<T>) : AsyncList<T>
}

sealed interface AsyncValue<out T> {
    data object Loading : AsyncValue<Nothing>
    data class Error(val message: String) : AsyncValue<Nothing>
    data class Data<T>(val value: T) : AsyncValue<T>
}
