package com.skuvahn.module.home.viewmodels

import androidx.lifecycle.*
import com.skuvahn.base.api.ApiInterface
import com.skuvahn.module.home.models.BookingCreateRequest
import com.skuvahn.module.home.states.BookingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val api: ApiInterface
) : ViewModel() {

    val state = MutableLiveData<BookingState>()

    fun getBookings(token:String, userId: String) = viewModelScope.launch {

        state.value = BookingState.Loading

        try {
            val response = api.getBookings(token, userId)

            if (response.isSuccessful) {

                val body = response.body()

                if (body?.success == true) {
                    state.value = BookingState.Success(body.data ?: emptyList())
                } else {
                    state.value = BookingState.Error(body?.message ?: "Failed")
                }

            } else {
                state.value = BookingState.Error("Error ${response.code()}")
            }

        } catch (e: Exception) {
            state.value = BookingState.Error(e.message ?: "Something went wrong")
        }
    }

    fun loadBookingDropdowns(token: String, userId: String) = viewModelScope.launch {

        state.value = BookingState.Loading

        try {

            val childrenRes = api.getChildren(token, userId)
            val addressRes = api.getAddresses(token, userId)
            val schoolRes = api.getSchools(token)
            val routeRes = api.getRoutes(token)

            state.value = BookingState.DropdownData(
                children = childrenRes.body()?.data ?: emptyList(),
                addresses = addressRes.body()?.data ?: emptyList(),
                schools = schoolRes.body()?.data ?: ArrayList(),
                routes = routeRes.body()?.data ?: emptyList()
            )

        } catch (e: Exception) {
            state.value = BookingState.Error(e.message ?: "Failed to load data")
        }
    }

    fun createBooking(token: String, request: BookingCreateRequest) = viewModelScope.launch {

        state.value = BookingState.Loading

        try {

            val response = api.createBooking(token, request)

            if (response.isSuccessful && response.body()?.success == true) {
                state.value = BookingState.CreateSuccess(
                    response.body()?.message ?: "Booking created"
                )
            } else {
                state.value = BookingState.Error(
                    response.body()?.message ?: "Failed"
                )
            }

        } catch (e: Exception) {
            state.value = BookingState.Error(e.message ?: "Error")
        }
    }
}