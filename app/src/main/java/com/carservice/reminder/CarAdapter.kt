package com.carservice.reminder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carservice.reminder.databinding.ItemCarBinding

class CarAdapter(
    private var cars: List<Car>,
    private val onClick: (Car) -> Unit
) : RecyclerView.Adapter<CarAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCarBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val car = cars[position]
        holder.binding.tvCarName.text = car.name
        holder.binding.tvCarModel.text = car.model
        holder.binding.tvCarPlate.text = car.plate
        holder.binding.root.setOnClickListener { onClick(car) }
    }

    override fun getItemCount() = cars.size

    fun submitList(newCars: List<Car>) {
        cars = newCars
        notifyDataSetChanged()
    }
}
