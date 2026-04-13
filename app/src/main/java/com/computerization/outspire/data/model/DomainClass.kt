package com.computerization.outspire.data.model

import kotlinx.datetime.LocalTime

data class DomainClass(
    val subject: String,
    val teacher: String,
    val room: String,
    val start: LocalTime,
    val end: LocalTime,
)
