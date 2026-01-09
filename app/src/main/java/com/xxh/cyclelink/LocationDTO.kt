package com.xxh.cyclelink

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val track_id: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float?
)
