package com.skuvahn.module.home.states

import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.BookingsResponseModel
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.models.RoutesResponseModel
import com.skuvahn.module.home.models.SchoolResponseModel

sealed class BookingState {

    object Loading : BookingState()

    data class Success(val list: List<BookingsResponseModel.Booking>) : BookingState()

    data class Error(val message: String) : BookingState()

    data class CreateSuccess(val message: String) : BookingState()

    data class DropdownData(
        val children: List<ChildResponseModel.Child>,
        val addresses: List<AddressResponseModel.Address>,
        val schools: ArrayList<SchoolResponseModel.School>,
        val routes: List<RoutesResponseModel.Route>
    ) : BookingState()
}