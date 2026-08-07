package com.carservice.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carservice.reminder.databinding.ActivityMainBinding
import com.carservice.reminder.databinding.DialogAddCarBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CarAdapter
    private val db by lazy { AppDatabase.getInstance(this) }
    private val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()

        adapter = CarAdapter(
            emptyList(),
            onClick = { car ->
                val intent = Intent(this, CarDetailActivity::class.java)
                intent.putExtra("carId", car.id)
                startActivity(intent)
            },
            onLongClickDelete = { car -> showDeleteCarConfirm(car) }
        )
        binding.rvCars.layoutManager = LinearLayoutManager(this)
        binding.rvCars.adapter = adapter

        lifecycleScope.launch {
            db.carDao().getAll().collect { cars ->
                adapter.submitList(cars)
            }
        }

        binding.fabAddCar.setOnClickListener { showAddCarDialog() }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    binding.svSearchResults.visibility = View.GONE
                    binding.rvCars.visibility = View.VISIBLE
                } else {
                    binding.svSearchResults.visibility = View.VISIBLE
                    binding.rvCars.visibility = View.GONE
                    runSearch(query)
                }
            }
        })
    }

    private fun runSearch(query: String) {
        lifecycleScope.launch {
            val results = db.carDao().search(query)
            binding.llSearchResults.removeAllViews()
            if (results.isEmpty()) {
                val tv = TextView(this@MainActivity)
                tv.text = "چیزی پیدا نشد"
                tv.setPadding(16, 16, 16, 16)
                binding.llSearchResults.addView(tv)
                return@launch
            }
            for (r in results) {
                val tv = TextView(this@MainActivity)
                tv.text = "${r.serviceName}  —  ماشین #${r.carNumber} (${r.carName})  —  تاریخ بعدی: ${sdf.format(r.nextDate)}"
                tv.textSize = 15f
                tv.setPadding(16, 24, 16, 24)
                tv.setOnClickListener {
                    val intent = Intent(this@MainActivity, CarDetailActivity::class.java)
                    intent.putExtra("carId", r.carId)
                    startActivity(intent)
                }
                binding.llSearchResults.addView(tv)
                val divider = View(this@MainActivity)
                divider.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                divider.setBackgroundColor(0xFFDDDDDD.toInt())
                binding.llSearchResults.addView(divider)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showAddCarDialog() {
        val dialogBinding = DialogAddCarBinding.inflate(layoutInflater)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("افزودن ماشین")
            .setView(dialogBinding.root)
            .setPositiveButton("ذخیره") { _, _ ->
                val name = dialogBinding.etCarName.text.toString().trim()
                val model = dialogBinding.etCarModel.text.toString().trim()
                val plate = dialogBinding.etCarPlate.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.carDao().insert(Car(name = name, model = model, plate = plate))
                    }
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    private fun showDeleteCarConfirm(car: Car) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("حذف ماشین")
            .setMessage("ماشین «${car.name}» و همه‌ی سرویس‌های آن حذف شود؟")
            .setPositiveButton("حذف") { _, _ ->
                lifecycleScope.launch {
                    val services = db.serviceItemDao().getByCarIdOnce(car.id)
                    for (s in services) {
                        AlarmScheduler.cancel(this@MainActivity, s.id)
                    }
                    db.carDao().delete(car)
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }
}
