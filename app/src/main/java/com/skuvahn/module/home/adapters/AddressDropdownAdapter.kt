package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemDropdownBinding
import com.skuvahn.module.home.listeners.AddressDropdownClickListener
import com.skuvahn.module.home.models.AddressResponseModel

class AddressDropdownAdapter(
    private val list: List<AddressResponseModel.Address>
) : RecyclerView.Adapter<AddressDropdownAdapter.VH>() {

    var listener: AddressDropdownClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {

        if (position < list.size) {
            val item = list[position]
            holder.binding.command.text = item.fullAddress

            holder.binding.root.setOnClickListener {
                listener?.onAddressSelected(item)
            }
        } else {
            holder.binding.command.text = "➕ Add Address"
            holder.binding.root.setOnClickListener {
                listener?.onAddAddress()
            }
        }
    }

    override fun getItemCount() = list.size + 1

    class VH(val binding: ItemDropdownBinding) : RecyclerView.ViewHolder(binding.root)
}