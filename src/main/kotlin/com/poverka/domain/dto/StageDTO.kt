package com.poverka.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class StageDTO(
    val stageCodeName: String,
    val caption: String,
    val photoDTOS: List<PhotoDTO>
)