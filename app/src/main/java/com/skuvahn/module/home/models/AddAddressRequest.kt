package com.skuvahn.module.home.models

data class AddAddressRequest(
    val addressType: String,
    val latitude: String,
    val longitude: String,
    val locality: String,
    val city: String,
    val state: String,
    val fullAddress: String,
    val landmark: String?,
    val pincode: String,
    val userId: String
)