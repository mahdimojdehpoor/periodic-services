package com.carservice.reminder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_items",
    foreignKeys = [ForeignKey(
        entity = Car::class,
        parentColumns = ["id"],
        childColumns = ["carId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("carId")]
)
data class ServiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carId: Int,
    val name: String,
    val currentKm: Int,
    val nextKm: Int,
    val currentDate: Long,
    val nextDate: Long
)
