package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.ChildResponseModel

interface ChildDropdownClickListener {
    fun onChildSelected(child: ChildResponseModel.Child)
    fun onAddChild()
}
