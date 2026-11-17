package com.skuvahn.module.home.models

data class ChildDeleteResponse(
    val success: Boolean,
    val statusCode: Int,
    val message: String?,
    val messages: List<String>?,
    val errors: Map<String, List<String>>?,
    val data: Boolean
)