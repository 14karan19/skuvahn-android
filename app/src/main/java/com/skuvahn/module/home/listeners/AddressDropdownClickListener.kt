package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.AddressResponseModel

interface AddressDropdownClickListener {
    fun onAddressSelected(address: AddressResponseModel.Address)
    fun onAddAddress()
}
