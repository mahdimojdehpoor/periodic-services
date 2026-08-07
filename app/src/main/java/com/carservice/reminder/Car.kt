package com.carservice.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val model: String,
    val plate: String,
    val socialLink: String? = null
)
