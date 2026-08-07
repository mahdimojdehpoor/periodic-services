package com.carservice.reminder

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carservice.reminder.databinding.ActivityCarDetailBinding
import com.carservice.reminder.databinding.DialogAddServiceItemBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var adapter: ServiceItemAdapter
    private val db by lazy { AppDatabase.getInstance(this) }
    private var carId: Int = -1
    private var car: Car? = null

    private val serviceNames = listOf(
        "روغن", "فیلتر روغن", "فیلتر هوا", "واسکارین",
        "شمع", "معاینه فنی", "بیمه", "تسمه تایم", "فیلتر اتاق"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getIntExtra("carId", -1)
        if (carId == -1) { finish(); return }

        adapter = ServiceItemAdapter(emptyList()) { item ->
            lifecycleScope.launch {
                db.serviceItemDao().delete(item)
                AlarmScheduler.cancel(this@CarDetailActivity, item.id)
            }
        }
        binding.rvServices.layoutManager = LinearLayoutManager(this)
        binding.rvServices.adapter = adapter

        lifecycleScope.launch {
            car = db.carDao().getById(carId)
            car?.let {
                binding.tvCarTitle.text = "${it.name} - ${it.model} - ${it.plate}"
                binding.etSocialLink.setText(it.socialLink ?: "")
            }
        }

        lifecycleScope.launch {
            db.serviceItemDao().getByCarId(carId).collect { list ->
                adapter.submitList(list)
            }
        }

        binding.btnSaveLink.setOnClickListener {
            val link = binding.etSocialLink.text.toString().trim()
            lifecycleScope.launch {
                car?.let {
                    val updated = it.copy(socialLink = link)
                    db.carDao().update(updated)
                    car = updated
                }
            }
        }

        binding.fabAddService.setOnClickListener { showAddServiceDialog() }
    }

    private fun showAddServiceDialog() {
        val dialogBinding = DialogAddServiceItemBinding.inflate(layoutInflater)
        dialogBinding.spinnerName.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, serviceNames
        )

        var currentDateMillis = System.currentTimeMillis()
        var nextDateMillis = System.currentTimeMillis()

        dialogBinding.btnCurDate.setOnClickListener {
            pickDate { millis ->
                currentDateMillis = millis
                dialogBinding.btnCurDate.text = formatDate(millis)
            }
        }
        dialogBinding.btnNextDate.setOnClickListener {
            pickDate { millis ->
                nextDateMillis = millis
                dialogBinding.btnNextDate.text = formatDate(millis)
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("افزودن سرویس")
            .setView(dialogBinding.root)
            .setPositiveButton("ذخیره") { _, _ ->
                val name = serviceNames[dialogBinding.spinnerName.selectedItemPosition]
                val curKm = dialogBinding.etCurKm.text.toString().toIntOrNull() ?: 0
                val nextKm = dialogBinding.etNextKm.text.toString().toIntOrNull() ?: 0

                lifecycleScope.launch {
                    val item = ServiceItem(
                        carId = carId,
                        name = name,
                        currentKm = curKm,
                        nextKm = nextKm,
                        currentDate = currentDateMillis,
                        nextDate = nextDateMillis
                    )
                    val newId = db.serviceItemDao().insert(item)
                    val saved = item.copy(id = newId.toInt())
                    car?.let {
                        AlarmScheduler.schedule(this@CarDetailActivity, saved, it.name, it.id, it.socialLink)
                    }
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun pickDate(onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day, 9, 0, 0)
            onPicked(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        return sdf.format(millis)
    }
}
