package com.skuvahn.module.home.states

import com.skuvahn.module.home.models.AddressResponseModel

sealed class AddressState {

    object Loading : AddressState()

    data class Success(
        val list: List<AddressResponseModel.Address> = emptyList()
    ) : AddressState()

    data class AddSuccess(val message: String) : AddressState()

    data class DeleteSuccess(val message: String) : AddressState()

    data class Error(val message: String) : AddressState()
}