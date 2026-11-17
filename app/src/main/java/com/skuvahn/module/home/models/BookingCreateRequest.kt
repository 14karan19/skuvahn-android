package com.skuvahn.module.home.models

data class BookingCreateRequest(
    val childId: String,
    val addressId: String,
    val schoolId: String,
    val tenure: String,
    val startDate: String,
    val userId: String
)