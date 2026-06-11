package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Garage
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OilBarrel
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ServiceRecord
import com.example.data.Vehicle
import com.example.viewmodel.CarMaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CarMaintenanceViewModel,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToAddService: () -> Unit,
) {
    val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsStateWithLifecycle()
    val records by viewModel.recordsForSelected.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showMileageEditDialog by remember { mutableStateOf(false) }
    var newMileageText by remember { mutableStateOf("") }

    val context = LocalContext.current
    var titleText = "نگهداری خودرو"
    val selectedVehicle = vehicles.find { it.id == selectedVehicleId }
    if (selectedVehicle != null) {
        titleText = selectedVehicle.name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText, fontWeight = FontWeight.Bold) },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "تنظیمات خودروها")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vehicles.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v.name) },
                                onClick = {
                                    viewModel.selectVehicle(v.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    if (v.id == selectedVehicleId) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Text("افزودن خودروی جدید") },
                            onClick = {
                                expanded = false
                                onNavigateToAddVehicle()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("خروجی گرفتن (بک‌آپ)") },
                            onClick = {
                                expanded = false
                                viewModel.exportData(context)
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("افزودن داده‌های نمونه") },
                            onClick = {
                                expanded = false
                                viewModel.forcePopulateDummyData()
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedVehicleId != null) {
                FloatingActionButton(onClick = onNavigateToAddService) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن سرویس")
                }
            }
        }
    ) { paddingValues ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (vehicles.isEmpty()) {
                    EmptyVehiclesState(onNavigateToAddVehicle, onAddSampleData = { viewModel.forcePopulateDummyData() })
                } else if (selectedVehicleId == null) {
                    SelectVehicleState()
                } else {
                    val warnings = remember(selectedVehicle, records) {
                        if (selectedVehicle == null) emptyList<ServiceRecord>()
                        else {
                            records.filter { record ->
                                record.nextServiceMileage != null && selectedVehicle.currentMileage >= (record.nextServiceMileage - 1000)
                            }
                        }
                    }

                    if (showMileageEditDialog && selectedVehicle != null) {
                        AlertDialog(
                            onDismissRequest = { showMileageEditDialog = false },
                            title = { Text("به‌روزرسانی کارکرد") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("کیلومتر فعلی خودرو ${selectedVehicle.name} را وارد کنید:")
                                    OutlinedTextField(
                                        value = newMileageText,
                                        onValueChange = { newMileageText = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        placeholder = { Text("مثال: ۸۰۰۰۰") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val m = newMileageText.toIntOrNull()
                                        if (m != null) {
                                            viewModel.updateVehicleMileage(selectedVehicle.id, m)
                                        }
                                        showMileageEditDialog = false
                                    }
                                ) {
                                    Text("تأیید")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showMileageEditDialog = false }) {
                                    Text("انصراف")
                                }
                            }
                        )
                    }

                    if (selectedVehicle != null) {
                        VehicleOverviewCard(
                            vehicle = selectedVehicle,
                            onEditMileageClick = {
                                newMileageText = selectedVehicle.currentMileage.toString()
                                showMileageEditDialog = true
                            }
                        )
                    }

                    if (warnings.isNotEmpty() && selectedVehicle != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "هشدار",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "هشدارهای سرویس مقتضی خودرو",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                warnings.forEach { warning ->
                                    val isOverdue = selectedVehicle.currentMileage >= warning.nextServiceMileage!!
                                    val diff = warning.nextServiceMileage - selectedVehicle.currentMileage
                                    val text = if (isOverdue) {
                                        "زمان تعویض ${warning.item} در ${warning.nextServiceMileage} کیلومتر فرا رسیده یا گذشته است! (کارکرد فعلی: ${selectedVehicle.currentMileage} کیلومتر)"
                                    } else {
                                        "به زمان تعویض ${warning.item} در ${warning.nextServiceMileage} کیلومتر نزدیک می‌شوید. ($diff کیلومتر باقی‌مانده)"
                                    }
                                    Text(
                                        text = "• $text",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("جستجو در سرویس‌ها...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    if (records.isEmpty()) {
                        EmptyRecordsState(searchQuery.isNotEmpty(), onAddSampleData = { viewModel.forcePopulateDummyData() })
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                        ) {
                            items(records, key = { it.id }) { record ->
                                ServiceRecordCard(
                                    record = record,
                                    onDelete = { viewModel.deleteRecord(record.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyVehiclesState(onNavigateToAddVehicle: () -> Unit, onAddSampleData: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "شما هیچ خودرویی ثبت نکرده‌اید", 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToAddVehicle, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)) {
            Text("افزودن اولین خودرو")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onAddSampleData, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)) {
            Text("اضافه کردن داده‌های آزمایشی")
        }
    }
}

@Composable
fun SelectVehicleState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Garage,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "لطفاً یک خودرو انتخاب کنید", 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmptyRecordsState(isSearchMode: Boolean, onAddSampleData: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchMode) Icons.Default.Search else Icons.Rounded.History,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            if (isSearchMode) "هیچ نتیجه‌ای یافت نشد" else "سرویسی برای این خودرو ثبت نشده است",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!isSearchMode) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onAddSampleData, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(56.dp)) {
                Text("اضافه کردن داده‌های آزمایشی")
            }
        }
    }
}


data class CategoryTheme(val icon: ImageVector, val contentColor: Color, val containerColor: Color)

fun getCategoryTheme(category: String, isDark: Boolean): CategoryTheme {
    val alphaBackground = if (isDark) 0.15f else 0.15f 
    return when {
        category.contains("ایمنی") -> {
            val baseColor = Color(0xFFEF4444) // Tailwind Red 500
            CategoryTheme(Icons.Rounded.History, baseColor, baseColor.copy(alpha = alphaBackground))
        }
        category.contains("مایعات") || category.contains("روغن") -> {
            val baseColor = Color(0xFF3B82F6) // Tailwind Blue 500
            CategoryTheme(Icons.Rounded.WaterDrop, baseColor, baseColor.copy(alpha = alphaBackground))
        }
        category.contains("فیلتر") -> {
            val baseColor = Color(0xFF10B981) // Tailwind Emerald 500
            CategoryTheme(Icons.Rounded.Build, baseColor, baseColor.copy(alpha = alphaBackground))
        }
        category.contains("موتور") -> {
            val baseColor = Color(0xFFF97316) // Tailwind Orange 500
            CategoryTheme(Icons.Rounded.Build, baseColor, baseColor.copy(alpha = alphaBackground))
        }
        category.contains("لاستیک") -> {
            val baseColor = Color(0xFFA855F7) // Tailwind Purple 500
            CategoryTheme(Icons.Rounded.TireRepair, baseColor, baseColor.copy(alpha = alphaBackground))
        }
        category.contains("چراغ") || category.contains("برق") -> {
            val baseColor = Color(0xFFEAB308) // Tailwind Yellow 500
            val containerAlpha = if (isDark) 0.15f else 0.2f
            CategoryTheme(Icons.Rounded.Lightbulb, if(isDark) Color(0xFFFDE047) else Color(0xFFCA8A04), baseColor.copy(alpha = containerAlpha))
        }
        else -> {
            val baseColor = Color(0xFF6366F1) // Tailwind Indigo 500
            CategoryTheme(Icons.Rounded.Build, baseColor, baseColor.copy(alpha = alphaBackground))
        }
    }
}

@Composable
fun ServiceRecordCard(record: ServiceRecord, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val theme = getCategoryTheme(record.category, isDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(theme.containerColor, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(theme.icon, contentDescription = null, tint = theme.contentColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = record.item, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = record.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "بیشتر")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("حذف کردن") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = record.date, style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${record.mileage} کیلومتر", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (record.cost > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AttachMoney, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${record.cost} تومان", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = record.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (record.nextServiceMileage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "سرویس بعدی در: ${record.nextServiceMileage} کیلومتر", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VehicleOverviewCard(
    vehicle: Vehicle,
    onEditMileageClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (vehicle.plateNumber.isNotBlank()) {
                    Text(
                        text = "پلاک: ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "کارکرد فعلی:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable { onEditMileageClick() }
                ) {
                    Text(
                        text = "${vehicle.currentMileage} کیلومتر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش کارکرد",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
