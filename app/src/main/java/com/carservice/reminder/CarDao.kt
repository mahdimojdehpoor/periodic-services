package com.carservice.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Insert
    suspend fun insert(car: Car): Long

    @Query("SELECT * FROM cars ORDER BY id DESC")
    fun getAll(): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :carId")
    suspend fun getById(carId: Int): Car?

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)
}
