package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.BookingsResponseModel

interface BookingClickListener {

    fun onBookingClick(booking: BookingsResponseModel.Booking)
}
