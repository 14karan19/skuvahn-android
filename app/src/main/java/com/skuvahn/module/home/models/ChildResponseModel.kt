package com.skuvahn.module.home.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class ChildResponseModel(
    val success: Boolean,
    val statusCode: Int,
    val message: String?,
    val data: List<Child>?
) {
    data class Child(
        val id: String?,
        val fullName: String?,
        val dob: String?,
        val grade: String?,
        @SerializedName("class")
        val studentClass: String?,
        val section: String?,
        val schoolId: String?,
        val schoolName: String?,
        val imageUrl: String?,
        val emergencyContactName: String?,
        val emergencyContactPhone: String?
    ) : Serializable {

        val formattedDob: String
            get() = try {
                val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                dob?.let { output.format(input.parse(it)!!) } ?: ""
            } catch (e: Exception) {
                dob ?: ""
            }
    }
}