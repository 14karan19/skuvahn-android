package com.skuvahn.module.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemDropdownBinding
import com.skuvahn.module.home.listeners.ClassClickListener
import com.skuvahn.module.home.listeners.SchoolClickListener
import com.skuvahn.module.home.models.SchoolResponseModel

class ClassDropdownAdapter(
    private val classes: ArrayList<String>
) : RecyclerView.Adapter<ClassHolder>() {

    var listener: ClassClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassHolder {
        return ClassHolder(
            ItemDropdownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ClassHolder, position: Int) {

        val item = classes[position]

        holder.binding.command.text = item

        holder.binding.mainLayout.setOnClickListener {
            listener?.onClassClick(item)
        }
    }

    override fun getItemCount() = classes.size
}

class ClassHolder(val binding: ItemDropdownBinding) :
    RecyclerView.ViewHolder(binding.root)