package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.ChildResponseModel

interface ChildDeleteClickListener {
    fun onChildDeleteClick(child: ChildResponseModel.Child)
}
