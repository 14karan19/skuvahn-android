package com.skuvahn.module.profile

sealed class ProfileState {
    object Loading : ProfileState()
    object Success : ProfileState()
    object DeleteSuccess : ProfileState()
    data class Error(val message: String) : ProfileState()
}