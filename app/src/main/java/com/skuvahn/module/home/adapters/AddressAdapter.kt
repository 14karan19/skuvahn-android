package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemAddressBinding
import com.skuvahn.module.home.listeners.*
import com.skuvahn.module.home.models.AddressResponseModel

class AddressAdapter(
    private val list: ArrayList<AddressResponseModel.Address>
) : RecyclerView.Adapter<AddressViewHolder>() {

    var addressClickListener: AddressClickListener? = null
    var editAddressClickListener: AddressEditClickListener? = null
    var deleteAddressClickListener: AddressDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        return AddressViewHolder(
            ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {

        val item = list[position]
        holder.bind(item)

        holder.binding.mainLayout.setOnClickListener {
            addressClickListener?.onAddressClick(item)
        }

        holder.binding.btnEdit.setOnClickListener {
            editAddressClickListener?.onEditAddressClick(item)
        }

        holder.binding.btnDelete.setOnClickListener {
            deleteAddressClickListener?.onDeleteAddressClick(item)
        }

        holder.binding.ivDelete.setOnClickListener {
            deleteAddressClickListener?.onDeleteAddressClick(item)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<AddressResponseModel.Address>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}

class AddressViewHolder(val binding: ItemAddressBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(address: AddressResponseModel.Address) {
        binding.address = address
    }
}