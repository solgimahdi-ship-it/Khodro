package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Int): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Int)
}

@Dao
interface ServiceRecordDao {
    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getRecordsForVehicle(vehicleId: Int): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ServiceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ServiceRecord)

    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteRecordById(id: Int)
    
    @Query("DELETE FROM service_records WHERE vehicleId = :vehicleId")
    suspend fun deleteRecordsByVehicleId(vehicleId: Int)
}
