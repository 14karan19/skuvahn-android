package com.skuvahn.module.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skuvahn.R
import com.skuvahn.databinding.ItemChildBinding
import com.skuvahn.module.home.listeners.*
import com.skuvahn.module.home.models.ChildResponseModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChildAdapter(
    private val context: Context,
    private val list: ArrayList<ChildResponseModel.Child>
) : RecyclerView.Adapter<ChildViewHolder>() {

    var childClickListener: ChildClickListener? = null
    var childEditClickListener: ChildEditClickListener? = null
    var childDeleteClickListener: ChildDeleteClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChildViewHolder(
            ItemChildBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)

        Glide.with(context)
            .load(list[position].imageUrl)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.binding.imgProfile)

        holder.binding.tvChildDob.text = formatDate(item.dob)

        holder.binding.tvGrade.text = "${item.studentClass} - ${item.section}"

        holder.binding.btnEdit.setOnClickListener {
            childEditClickListener?.onChildEditClick(item)
        }

        holder.binding.btnDelete.setOnClickListener {
            childDeleteClickListener?.onChildDeleteClick(item)
        }
    }

    private fun formatDate(date: String?): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val parsed = input.parse(date ?: "")
            output.format(parsed ?: Date())
        } catch (e: Exception) {
            "-"
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<ChildResponseModel.Child>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}

class ChildViewHolder(val binding: ItemChildBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(child: ChildResponseModel.Child) {
        binding.child = child
    }
}