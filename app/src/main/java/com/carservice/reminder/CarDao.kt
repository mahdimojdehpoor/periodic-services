package com.carservice.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class SearchResult(
    val serviceName: String,
    val carId: Int,
    val carName: String,
    val carNumber: Int,
    val nextDate: Long
)

@Dao
interface CarDao {
    @Insert
    suspend fun insert(car: Car): Long

    @Query("SELECT * FROM cars ORDER BY id ASC")
    fun getAll(): Flow<List<Car>>

    @Query("SELECT * FROM cars WHERE id = :carId")
    suspend fun getById(carId: Int): Car?

    @Query("SELECT COUNT(*) FROM cars WHERE id <= :carId")
    suspend fun getCarNumber(carId: Int): Int

    @Update
    suspend fun update(car: Car)

    @Delete
    suspend fun delete(car: Car)

    @Query("""
        SELECT s.name as serviceName, s.carId as carId, c.name as carName,
               (SELECT COUNT(*) FROM cars c2 WHERE c2.id <= c.id) as carNumber,
               s.nextDate as nextDate
        FROM service_items s
        INNER JOIN cars c ON s.carId = c.id
        WHERE s.name LIKE '%' || :query || '%'
        ORDER BY s.nextDate ASC
    """)
    suspend fun search(query: String): List<SearchResult>
}
