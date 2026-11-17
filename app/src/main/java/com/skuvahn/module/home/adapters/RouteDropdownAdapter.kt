package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemDropdownBinding
import com.skuvahn.module.home.listeners.RouteClickListener
import com.skuvahn.module.home.models.RoutesResponseModel

class RouteDropdownAdapter(
    private val list: List<RoutesResponseModel.Route>
) : RecyclerView.Adapter<RouteDropdownAdapter.VH>() {

    var listener: RouteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.binding.command.text = item.routeStop

        holder.binding.root.setOnClickListener {
            listener?.onRouteSelected(item)
        }
    }

    override fun getItemCount() = list.size

    class VH(val binding: ItemDropdownBinding) : RecyclerView.ViewHolder(binding.root)
}