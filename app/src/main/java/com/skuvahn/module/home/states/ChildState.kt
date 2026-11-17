package com.skuvahn.module.home.states

import com.skuvahn.module.home.models.*

sealed class ChildState {

    object Loading : ChildState()

    data class Success(val list: List<ChildResponseModel.Child>) : ChildState()

    data class SchoolListSuccess(val list: ArrayList<SchoolResponseModel.School>) : ChildState()

    data class AddSuccess(val message: String) : ChildState()

    data class UpdateSuccess(val message: String) : ChildState()

    data class DeleteSuccess(val message: String) : ChildState()

    data class Error(val message: String) : ChildState()
}