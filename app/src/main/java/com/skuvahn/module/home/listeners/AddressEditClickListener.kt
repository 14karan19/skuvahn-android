package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.AddressResponseModel

interface AddressEditClickListener {
    fun onEditAddressClick(address: AddressResponseModel.Address)
}
