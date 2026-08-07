package com.carservice.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceItemDao {
    @Insert
    suspend fun insert(item: ServiceItem): Long

    @Query("SELECT * FROM service_items WHERE carId = :carId ORDER BY id ASC")
    fun getByCarId(carId: Int): Flow<List<ServiceItem>>

    @Query("SELECT * FROM service_items WHERE nextDate > :now")
    suspend fun getAllFuture(now: Long): List<ServiceItem>

    @Delete
    suspend fun delete(item: ServiceItem)
}
