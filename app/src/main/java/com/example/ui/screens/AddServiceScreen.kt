package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.util.JalaliDateUtils

val SERVICE_CATEGORIES = listOf(
    "ایمنی",
    "مایعات و روغن‌ها",
    "فیلترها",
    "موتور و جانبی",
    "لاستیک",
    "چراغ‌ها و برق",
    "شاسی و تعلیق",
    "بدنه و داخلی",
    "سایر"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    onNavigateBack: () -> Unit,
    onAddService: (category: String, item: String, mileage: Int, cost: Double, notes: String, date: String, nextServiceMileage: Int?) -> Unit
) {
    var item by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SERVICE_CATEGORIES[0]) }
    var mileageStr by remember { mutableStateOf("") }
    var nextMileageStr by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val currentDate = remember { JalaliDateUtils.getCurrentJalaliDate() }
    var date by remember { mutableStateOf(currentDate) }

    var expandedCategory by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ثبت سرویس جدید", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("دسته بندی") },
                        leadingIcon = { Icon(Icons.Rounded.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        SERVICE_CATEGORIES.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = item,
                    onValueChange = { item = it },
                    label = { Text("مورد سرویس (مثال: تعویض لنت)") },
                    leadingIcon = { Icon(Icons.Rounded.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = mileageStr,
                    onValueChange = { mileageStr = it },
                    label = { Text("کیلومتر فعلی") },
                    leadingIcon = { Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = nextMileageStr,
                    onValueChange = { nextMileageStr = it },
                    label = { Text("کیلومتر سرویس بعدی (اختیاری - مثال: ۸۰۰۰۰)") },
                    leadingIcon = { Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("هزینه (تومان - اختیاری)") },
                    leadingIcon = { Icon(Icons.Rounded.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("تاریخ (خورشیدی)") },
                    leadingIcon = { Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات تکمیلی (اختیاری)") },
                    leadingIcon = { Icon(Icons.Rounded.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val m = mileageStr.toIntOrNull() ?: 0
                        val c = costStr.toDoubleOrNull() ?: 0.0
                        val nextMil = nextMileageStr.toIntOrNull()
                        if (item.isNotBlank() && mileageStr.isNotBlank()) {
                            onAddService(selectedCategory, item, m, c, notes, date, nextMil)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = item.isNotBlank() && mileageStr.isNotBlank(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ثبت اطلاعات", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
