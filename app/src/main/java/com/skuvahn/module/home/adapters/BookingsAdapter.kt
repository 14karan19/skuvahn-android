package com.skuvahn.module.home.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skuvahn.databinding.ItemBookingsBinding
import com.skuvahn.module.home.listeners.BookingClickListener
import com.skuvahn.module.home.models.BookingsResponseModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingsAdapter(
    val context: Context,
    private val bookings: ArrayList<BookingsResponseModel.Booking>
) : RecyclerView.Adapter<BookingsViewHolder>() {

    var bookingClickListener: BookingClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BookingsViewHolder(
            ItemBookingsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: BookingsViewHolder, position: Int) {

        val item = bookings[position]

        holder.binding.tvDate.text = formatDate(item.createdAt)
        holder.binding.tvStartDate.text = formatDate(item.startDate)

        holder.binding.btnViewDetails.setOnClickListener {
            bookingClickListener?.onBookingClick(item)
        }

        holder.bind(item)
    }

    private fun formatDate(date: String?): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val parsed = input.parse(date ?: "")
            output.format(parsed ?: Date())
        } catch (e: Exception) {
            "-"
        }
    }
    override fun getItemCount() = bookings.size

    fun updateData(newList: List<BookingsResponseModel.Booking>) {
        bookings.clear()
        bookings.addAll(newList)
        notifyDataSetChanged()
    }
}

class BookingsViewHolder(val binding: ItemBookingsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(booking: BookingsResponseModel.Booking) {
        binding.booking = booking
    }
}