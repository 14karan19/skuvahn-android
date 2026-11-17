package com.skuvahn.module.home.listeners

import com.skuvahn.module.home.models.RoutesResponseModel

interface RouteClickListener {
    fun onRouteSelected(route: RoutesResponseModel.Route)
}