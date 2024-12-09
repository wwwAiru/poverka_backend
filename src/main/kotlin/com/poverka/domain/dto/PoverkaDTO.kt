package com.poverka.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class PoverkaDTO(
    val uuid: String,
    val stageDTOS: List<StageDTO>
)