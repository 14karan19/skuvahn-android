package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.SchoolResponseModel

interface SchoolClickListener {
    fun onSchoolClick(school: SchoolResponseModel.School)
}