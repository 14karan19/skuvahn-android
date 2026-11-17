package com.skuvahn.module.home.states

import com.skuvahn.module.home.models.Vehicle

sealed class LiveTrackingState {
    object Loading : LiveTrackingState()
    data class Success(val vehicle: Vehicle?) : LiveTrackingState()
    data class Error(val message: String) : LiveTrackingState()
}