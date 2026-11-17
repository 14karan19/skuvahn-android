package com.skuvahn.utilities

enum class DriverDocType {
    PROFILE,
    DL_FRONT,
    DL_BACK,
    AADHAAR_FRONT,
    AADHAAR_BACK,
    RC,
    INSURANCE
}

class PreferenceConstants {
    companion object {
        const val phone = "phone"
        const val token = "token"
        const val firebaseToken = "firebaseToken"
        const val userId = "userId"
        const val allowOffline = "allowOffline"
        const val address = "address"
        const val deleteUser = "deleteUser"
        const val isLoggedIn = "isLoggedIn"
        const val email = "email"
        const val name = "name"
        const val group = "group"
        const val roleId = "roleId"
        const val roleName = "roleName"
        const val groupId = "groupId"
        const val profilePic = "profilePic"
        const val groupCode = "groupCode"

        const val editAddress = "editAddress"

        const val latitude = "latitude"
        const val longitude = "longitude"

        const val child = "child"
    }
}

class ApiConstants {
    companion object {
        const val something_wrong = "Something went wrong! Please try again later"
        const val command_already_processed = "Command has been processed already."
        const val something_wrong_command = "Something wrong while executing the command."
    }
}

class EndPoints {
    companion object {
        const val baseUrl = "https://api.skuvahn.com/api/v1/"

        const val sendOtp = "auth/sendOtp"
        const val resendOtp = "auth/resendOtp"
        const val verifyOtp = "auth/verifyOtp"
        const val saveGuardian = "users/saveGuardian"
        const val addresses = "mobile/addresses"
        const val deleteAddress = "mobile/deleteAddress/{addressId}"
        const val children = "mobile/children"
        const val deleteChildren = "mobile/deleteChildren/{childId}"
        const val updateChild = "mobile/children/{id}"
        const val bookings = "mobile/bookings"
        const val routes = "mobile/routes"
        const val schools = "mobile/schools"

        const val home = "mobile/home"

        const val deleteUser = "users/deleteUser"
    }
}

class DateConstants {
    companion object {
        const val dateFormat1 = "yyyy-MM-dd'T'HH:mm:ss.SSXXX"
        const val dateFormat2 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX"
        const val outputFormat = "dd-MM-yyyy"
    }
}