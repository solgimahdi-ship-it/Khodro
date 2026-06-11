package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ServiceRecord
import com.example.data.Vehicle
import com.example.repository.CarMaintenanceRepository
import com.example.util.ExportUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CarMaintenanceViewModel(private val repository: CarMaintenanceRepository) : ViewModel() {

    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedVehicleId = MutableStateFlow<Int?>(null)
    val selectedVehicleId: StateFlow<Int?> = _selectedVehicleId
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        viewModelScope.launch {
            repository.populateDummyData()
        }
        viewModelScope.launch {
            allVehicles.collect { vehicles ->
                if (vehicles.isNotEmpty() && _selectedVehicleId.value == null) {
                    _selectedVehicleId.value = vehicles.first().id
                }
            }
        }
    }

    val recordsForSelected: StateFlow<List<ServiceRecord>> = _selectedVehicleId
        .flatMapLatest { vehicleId ->
            if (vehicleId == null) {
                flowOf(emptyList())
            } else {
                repository.getRecordsForVehicle(vehicleId)
            }
        }.combine(_searchQuery) { records, query ->
            if (query.isBlank()) {
                records
            } else {
                records.filter { 
                    it.category.contains(query, ignoreCase = true) || 
                    it.item.contains(query, ignoreCase = true) || 
                    it.notes.contains(query, ignoreCase = true) 
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectVehicle(id: Int?) {
        _selectedVehicleId.value = id
        _searchQuery.value = "" // reset search when switching
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addVehicle(name: String, plate: String, initialMileage: Int = 0) {
        viewModelScope.launch {
            repository.insertVehicle(Vehicle(name = name, plateNumber = plate, currentMileage = initialMileage))
        }
    }

    fun deleteVehicle(id: Int) {
        viewModelScope.launch {
            repository.deleteVehicleById(id)
            if (_selectedVehicleId.value == id) {
                _selectedVehicleId.value = null
            }
        }
    }

    fun updateVehicleMileage(vehicleId: Int, mileage: Int) {
        viewModelScope.launch {
            val vehicle = repository.getVehicleById(vehicleId)
            if (vehicle != null) {
                repository.insertVehicle(vehicle.copy(currentMileage = mileage))
            }
        }
    }

    fun addRecord(vehicleId: Int, category: String, item: String, mileage: Int, cost: Double, notes: String, date: String, nextServiceMileage: Int? = null) {
        viewModelScope.launch {
            repository.insertRecord(
                ServiceRecord(
                    vehicleId = vehicleId,
                    category = category,
                    item = item,
                    mileage = mileage,
                    cost = cost,
                    notes = notes,
                    date = date,
                    nextServiceMileage = nextServiceMileage
                )
            )
            // Auto update vehicle's current mileage if this service record has higher mileage
            val vehicle = repository.getVehicleById(vehicleId)
            if (vehicle != null && mileage > vehicle.currentMileage) {
                repository.insertVehicle(vehicle.copy(currentMileage = mileage))
            }
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
        }
    }
    
    fun exportData(context: Context) {
        viewModelScope.launch {
            val records = repository.getAllRecords().first()
            val vehicles = allVehicles.value
            ExportUtils.exportToCsvAndShare(context, vehicles, records)
        }
    }
    
    fun forcePopulateDummyData() {
        viewModelScope.launch {
            repository.forcePopulateDummyData()
            // Switch to one of the newly added dummy vehicles so the user sees it immediately
            val vehicles = repository.allVehicles.first()
            if (vehicles.isNotEmpty()) {
                _selectedVehicleId.value = vehicles.last().id // Select latest added to show it
            }
        }
    }
}

class CarMaintenanceViewModelFactory(private val repository: CarMaintenanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarMaintenanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarMaintenanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
