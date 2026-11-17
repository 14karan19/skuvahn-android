package com.skuvahn.module.home.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skuvahn.base.api.ApiInterface
import com.skuvahn.module.home.models.AddAddressRequest
import com.skuvahn.module.home.states.AddressState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val api: ApiInterface
) : ViewModel() {

    val state = MutableLiveData<AddressState>()

    fun getAddresses(token: String, userId: String) = viewModelScope.launch {

        state.value = AddressState.Loading

        try {
            val response = api.getAddresses(token, userId)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true) {
                    state.value = AddressState.Success(body.data ?: emptyList())
                } else {
                    state.value = AddressState.Error(body?.message ?: "Failed")
                }
            } else {
                state.value = AddressState.Error("Error ${response.code()}")
            }

        } catch (e: Exception) {
            state.value = AddressState.Error(e.message ?: "Error")
        }
    }

    fun addAddress(token: String, request: AddAddressRequest) = viewModelScope.launch {

        state.value = AddressState.Loading

        try {
            val response = api.addAddress(token,request)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true) {
                    state.value = AddressState.AddSuccess(
                        body.message ?: "Address added successfully"
                    )
                } else {
                    state.value = AddressState.Error(
                        body?.message ?: "Failed to add address"
                    )
                }

            } else {
                state.value = AddressState.Error("Server error: ${response.code()}")
            }

        } catch (e: Exception) {
            state.value = AddressState.Error(e.message ?: "Something went wrong")
        }
    }

    fun deleteAddress(token:String, addressId: String, userId: String) = viewModelScope.launch {

        state.value = AddressState.Loading

        try {
            val response = api.deleteAddress(token, addressId, userId)

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true && body.data == true) {
                    state.value = AddressState.DeleteSuccess(
                        body.message ?: "Deleted"
                    )
                } else {
                    state.value = AddressState.Error(body?.message ?: "Delete failed")
                }

            } else {
                state.value = AddressState.Error("Error ${response.code()}")
            }

        } catch (e: Exception) {
            state.value = AddressState.Error(e.message ?: "Error")
        }
    }
}