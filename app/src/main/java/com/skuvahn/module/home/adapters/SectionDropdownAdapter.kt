package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemDropdownBinding
import com.skuvahn.module.home.listeners.ClassClickListener
import com.skuvahn.module.home.listeners.SchoolClickListener
import com.skuvahn.module.home.listeners.SectionClickListener
import com.skuvahn.module.home.models.SchoolResponseModel

class SectionDropdownAdapter(
    private val sections: ArrayList<String>
) : RecyclerView.Adapter<SectionHolder>() {

    var listener: SectionClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionHolder {
        return SectionHolder(
            ItemDropdownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SectionHolder, position: Int) {

        val item = sections[position]

        holder.binding.command.text = item

        holder.binding.mainLayout.setOnClickListener {
            listener?.onSectionClick(item)
        }
    }

    override fun getItemCount() = sections.size
}

class SectionHolder(val binding: ItemDropdownBinding) :
    RecyclerView.ViewHolder(binding.root)