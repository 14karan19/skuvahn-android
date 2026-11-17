package com.skuvahn.module.home.states

import com.skuvahn.module.home.models.HomeResponseModel

sealed class HomeState {
    object Idle : HomeState()
    object Loading : HomeState()
    data class Success(val data: HomeResponseModel.Data) : HomeState()
    data class Error(val message: String) : HomeState()
}