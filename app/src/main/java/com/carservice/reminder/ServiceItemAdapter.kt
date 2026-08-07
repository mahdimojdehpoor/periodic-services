package com.carservice.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carservice.reminder.databinding.ItemServiceBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceItemAdapter(
    private var items: List<ServiceItem>,
    private val onDelete: (ServiceItem) -> Unit
) : RecyclerView.Adapter<ServiceItemAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    inner class ViewHolder(val binding: ItemServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvRow.text = (position + 1).toString()
        holder.binding.tvName.text = item.name
        holder.binding.tvCurKm.text = item.currentKm.toString()
        holder.binding.tvNextKm.text = item.nextKm.toString()
        holder.binding.tvCurDate.text = sdf.format(item.currentDate)
        holder.binding.tvNextDate.text = sdf.format(item.nextDate)
        holder.binding.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<ServiceItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
