package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.AddressResponseModel

interface AddressClickListener {
    fun onAddressClick(address: AddressResponseModel.Address)
}
