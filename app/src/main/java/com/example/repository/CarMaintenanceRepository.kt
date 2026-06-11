package com.example.repository

import com.example.data.ServiceRecord
import com.example.data.ServiceRecordDao
import com.example.data.Vehicle
import com.example.data.VehicleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CarMaintenanceRepository(
    private val vehicleDao: VehicleDao,
    private val serviceRecordDao: ServiceRecordDao
) {
    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    
    suspend fun getVehicleById(id: Int): Vehicle? {
        return vehicleDao.getVehicleById(id)
    }

    suspend fun insertVehicle(vehicle: Vehicle): Long {
        return vehicleDao.insertVehicle(vehicle)
    }

    suspend fun populateDummyData() {
        val vehicles = vehicleDao.getAllVehicles().first()
        if (vehicles.isEmpty()) {
            forcePopulateDummyData()
        }
    }

    suspend fun forcePopulateDummyData() {
        val v1Id = vehicleDao.insertVehicle(Vehicle(name = "پژو ۲۰۶", plateNumber = "۱۲ ب ۳۴۵ ایران ۶۷", year = "1398", currentMileage = 79400)).toInt()
        val v2Id = vehicleDao.insertVehicle(Vehicle(name = "تویوتا کمری", plateNumber = "۹۸ الف ۷۶۵ ایران ۱۱", year = "2015", currentMileage = 129800)).toInt()
        
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v1Id, date = "1402/05/12", mileage = 65000, category = "مایعات و روغن‌ها", item = "تعویض روغن موتور و فیلتر", notes = "روغن 10W40 ریخته شد", cost = 850000.0, nextServiceMileage = 70000))
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v1Id, date = "1402/08/20", mileage = 70000, category = "ایمنی", item = "تعویض لنت ترمز جلو", notes = "لنت تکستار", cost = 1200000.0, nextServiceMileage = 85000))
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v1Id, date = "1403/01/10", mileage = 75000, category = "فیلترها", item = "تعویض فیلتر بنزین و کابین", notes = "", cost = 400000.0, nextServiceMileage = 80000))
        
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v2Id, date = "1402/10/05", mileage = 120000, category = "لاستیک", item = "تعویض دو حلقه لاستیک جلو", notes = "لاستیک بارز طرح P640", cost = 3500000.0, nextServiceMileage = 160000))
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v2Id, date = "1403/02/15", mileage = 125000, category = "چراغ‌ها و برق", item = "تعویض باتری", notes = "باتری صبا 74 آمپر", cost = 2100000.0))
        serviceRecordDao.insertRecord(ServiceRecord(vehicleId = v2Id, date = "1403/02/16", mileage = 125010, category = "موتور و جانبی", item = "تعویض شمع موتور", notes = "شمع بوش پایه کوتاه", cost = 950000.0, nextServiceMileage = 130000))
    }

    suspend fun deleteVehicleById(id: Int) {
        serviceRecordDao.deleteRecordsByVehicleId(id)
        vehicleDao.deleteVehicleById(id)
    }

    fun getRecordsForVehicle(vehicleId: Int): Flow<List<ServiceRecord>> {
        return serviceRecordDao.getRecordsForVehicle(vehicleId)
    }
    
    fun getAllRecords(): Flow<List<ServiceRecord>> {
        return serviceRecordDao.getAllRecords()
    }

    suspend fun insertRecord(record: ServiceRecord) {
        serviceRecordDao.insertRecord(record)
    }

    suspend fun deleteRecordById(id: Int) {
        serviceRecordDao.deleteRecordById(id)
    }
}
