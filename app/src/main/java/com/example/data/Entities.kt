package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val plateNumber: String = "",
    val year: String = "",
    val vin: String = "",
    val currentMileage: Int = 0
)

@Serializable
@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val date: String, // stored as Jalali format "YYYY/MM/DD"
    val mileage: Int,
    val category: String, // e.g. "مایعات", "ترمز", etc.
    val item: String, // e.g. "روغن موتور"
    val notes: String = "",
    val cost: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val nextServiceMileage: Int? = null
)
