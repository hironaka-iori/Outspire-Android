package com.computerization.outspire.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    @SerialName("Id") val id: String = "",
    @SerialName("GroupNo") val groupNo: String? = null,
    @SerialName("NameC") val nameC: String? = null,
    @SerialName("NameE") val nameE: String? = null,
    @SerialName("TeacherName") val teacherName: String? = null,
    @SerialName("DescriptionC") val descriptionC: String? = null,
    @SerialName("DescriptionE") val descriptionE: String? = null,
    @SerialName("CategoryName") val categoryName: String? = null,
    @SerialName("IsProject") val isProject: Boolean? = null,
    @SerialName("ProjectName") val projectName: String? = null,
)

@Serializable
data class RecordDto(
    @SerialName("Id") val id: String = "",
    @SerialName("GroupId") val groupId: String = "",
    @SerialName("Theme") val theme: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("ActivityDateStr") val activityDateStr: String? = null,
    @SerialName("Date") val date: String? = null,
    @SerialName("CDuration") val cDuration: String? = null,
    @SerialName("ADuration") val aDuration: String? = null,
    @SerialName("SDuration") val sDuration: String? = null,
    @SerialName("Reflection") val reflection: String? = null,
    @SerialName("IsConfirm") val isConfirm: Int? = null,
)

@Serializable
data class ReflectionDto(
    @SerialName("Id") val id: String = "",
    @SerialName("GroupId") val groupId: String = "",
    @SerialName("Title") val title: String? = null,
    @SerialName("Summary") val summary: String? = null,
    @SerialName("Content") val content: String? = null,
    @SerialName("Outcome") val outcome: Int? = null,
)

@Serializable
data class EvaluationDto(
    @SerialName("GroupRecordList") val groupRecordList: List<EvaluationGroupDto> = emptyList(),
    @SerialName("RecLevel") val recLevel: String? = null,
    @SerialName("RefLevel") val refLevel: String? = null,
    @SerialName("Talk") val talk: String? = null,
    @SerialName("Final") val finalScore: String? = null,
)

@Serializable
data class EvaluationGroupDto(
    @SerialName("GroupName") val groupName: String? = null,
    @SerialName("NameC") val nameC: String? = null,
    @SerialName("NameE") val nameE: String? = null,
    @SerialName("CDuration") val cDuration: String? = null,
    @SerialName("ADuration") val aDuration: String? = null,
    @SerialName("SDuration") val sDuration: String? = null,
)
