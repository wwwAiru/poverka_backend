package com.poverka.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDTO(
    val photoCodeName: String,
    val caption: String,
    val imageFileName: String
)