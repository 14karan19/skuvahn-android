package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.ChildResponseModel

interface ChildEditClickListener {
    fun onChildEditClick(child: ChildResponseModel.Child)
}
