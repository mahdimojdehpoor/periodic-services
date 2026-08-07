package com.carservice.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carservice.reminder.databinding.ActivityMainBinding
import com.carservice.reminder.databinding.DialogAddCarBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CarAdapter
    private val db by lazy { AppDatabase.getInstance(this) }

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()

        adapter = CarAdapter(emptyList()) { car ->
            val intent = Intent(this, CarDetailActivity::class.java)
            intent.putExtra("carId", car.id)
            startActivity(intent)
        }
        binding.rvCars.layoutManager = LinearLayoutManager(this)
        binding.rvCars.adapter = adapter

        lifecycleScope.launch {
            db.carDao().getAll().collect { cars ->
                adapter.submitList(cars)
            }
        }

        binding.fabAddCar.setOnClickListener { showAddCarDialog() }
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
}
