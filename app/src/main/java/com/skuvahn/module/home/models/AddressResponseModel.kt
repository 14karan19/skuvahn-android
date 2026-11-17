package com.skuvahn.module.home.models

import java.io.Serializable

data class AddressResponseModel(
    val success: Boolean,
    val statusCode: Int,
    val message: String?,
    val messages: List<String>?,
    val errors: Map<String, List<String>>?,
    val data: List<Address>?
) {

    data class Address(
        val id: String?,
        val latitude: String?,
        val longitude: String?,
        val locality: String?,
        val fullAddress: String?,
        val pincode: String?,
        val landmark: String?,
        val addressType: String?
    ) : Serializable {

        // ✅ UI MAPPING HELPERS (IMPORTANT)
        val addressTitle: String
            get() = addressType ?: ""

        val address: String
            get() = fullAddress ?: ""

        val city: String
            get() = locality ?: ""

        val pinCode: String
            get() = pincode ?: ""
    }
}