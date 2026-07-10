package dev.outspire.android.data.model

data class User(
    val id: Int?,
    val code: String,
    val name: String,
    val role: String?,
    val isDemo: Boolean = false,
)
