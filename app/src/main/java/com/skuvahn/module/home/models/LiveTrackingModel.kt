package com.skuvahn.module.home.models

data class LiveTrackingRequest(
    val queryType: Int = 1,
    val queryParams: List<String>
)

data class LiveTrackingResponse(
    val code: Int,
    val data: LiveDataWrapper?,
    val msg: String
)

data class LiveDataWrapper(
    val list: List<Vehicle>
)

data class Vehicle(
    val lat: Double,
    val lon: Double,
    val speed: Double,
    val plate: String
)