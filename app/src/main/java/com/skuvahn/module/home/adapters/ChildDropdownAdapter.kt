package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemDropdownBinding
import com.skuvahn.module.home.listeners.ChildDropdownClickListener
import com.skuvahn.module.home.models.ChildResponseModel

class ChildDropdownAdapter(
    private val list: List<ChildResponseModel.Child>
) : RecyclerView.Adapter<ChildDropdownAdapter.VH>() {

    var listener: ChildDropdownClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDropdownBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {

        if (position < list.size) {
            val item = list[position]
            holder.binding.command.text = item.fullName

            holder.binding.root.setOnClickListener {
                listener?.onChildSelected(item)
            }
        } else {
            holder.binding.command.text = "➕ Add Child"
            holder.binding.root.setOnClickListener {
                listener?.onAddChild()
            }
        }
    }

    override fun getItemCount() = list.size + 1

    class VH(val binding: ItemDropdownBinding) : RecyclerView.ViewHolder(binding.root)
}