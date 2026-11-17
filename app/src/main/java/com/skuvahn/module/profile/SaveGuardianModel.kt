package com.skuvahn.module.profile

data class SaveGuardianResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String?,
    val messages: List<String>?,
    val errors: Map<String, List<String>>?,
    val data: GuardianData?
)

data class GuardianData(
    val userId: String?,
    val isProfileCompleted: Boolean?
)

data class SaveGuardianRequest(
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val imageUrl: String
)