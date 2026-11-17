package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.ChildResponseModel

interface ChildClickListener {
    fun onChildClick(child: ChildResponseModel.Child)
}
