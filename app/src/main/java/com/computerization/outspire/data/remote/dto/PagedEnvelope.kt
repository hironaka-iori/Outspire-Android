package com.computerization.outspire.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedEnvelope<T>(
    val PageIndex: Int = 1,
    val PageSize: Int = 0,
    val TotalCount: Int = 0,
    val PageCount: Int = 0,
    val List: List<T> = emptyList(),
)
