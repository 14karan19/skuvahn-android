package com.skuvahn.module.home.models

import com.google.gson.annotations.SerializedName

data class ChildAddRequest(
    val fullName: String,
    val dob: String,
    @SerializedName("class")
    val studentClass: String,
    val section: String,
    val schoolId: String,
    val imageUrl: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val userId: String
)

data class ChildUpdateRequest(
    val fullName: String,
    val dob: String,
    @SerializedName("class")
    val studentClass: String,
    val section: String,
    val schoolId: String,
    val imageUrl: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val userId: String,
    val id: String
)