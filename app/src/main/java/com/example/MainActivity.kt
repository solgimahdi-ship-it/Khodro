package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.repository.CarMaintenanceRepository
import com.example.ui.navigation.AddServiceRoute
import com.example.ui.navigation.AddVehicleRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.screens.AddServiceScreen
import com.example.ui.screens.AddVehicleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CarMaintenanceViewModel
import com.example.viewmodel.CarMaintenanceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = AppDatabase.getDatabase(this)
        val repository = CarMaintenanceRepository(database.vehicleDao(), database.serviceRecordDao())
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CarApp(repository)
                }
            }
        }
    }
}

@Composable
fun CarApp(repository: CarMaintenanceRepository) {
    val navController = rememberNavController()
    val viewModel: CarMaintenanceViewModel = viewModel(
        factory = CarMaintenanceViewModelFactory(repository)
    )

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToAddVehicle = { navController.navigate(AddVehicleRoute) },
                onNavigateToAddService = { navController.navigate(AddServiceRoute) }
            )
        }
        composable<AddVehicleRoute> {
            AddVehicleScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddVehicle = { name, plate, initialMileage -> viewModel.addVehicle(name, plate, initialMileage) }
            )
        }
        composable<AddServiceRoute> {
            AddServiceScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddService = { cat, item, mil, cost, notes, date, nextMil ->
                    val vehicleId = viewModel.selectedVehicleId.value
                    if (vehicleId != null) {
                        viewModel.addRecord(vehicleId, cat, item, mil, cost, notes, date, nextMil)
                    }
                }
            )
        }
    }
}
