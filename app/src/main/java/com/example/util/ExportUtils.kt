package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.ServiceRecord
import com.example.data.Vehicle
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object ExportUtils {
    
    fun exportToCsvAndShare(context: Context, vehicles: List<Vehicle>, records: List<ServiceRecord>) {
        try {
            val fileName = "car_maintenance_backup_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            
            val writer = OutputStreamWriter(FileOutputStream(file), "UTF-8")
            
            // Write BOM for UTF-8 Excel compatibility
            writer.write("\uFEFF")
            
            // Header
            writer.write("نوع رکورد,شناسه,نام خودرو,پلاک خودرو,تاریخ,کیلومتر,دسته بندی,آیتم,هزینه,توضیحات\n")
            
            val vehicleMap = vehicles.associateBy { it.id }
            
            // Write Vehicles
            for (v in vehicles) {
                writer.write("خودرو,${v.id},${v.name},${v.plateNumber},-,-,-,-,-,-\n")
            }
            
            // Write Records
            for (r in records) {
                val vName = vehicleMap[r.vehicleId]?.name ?: "نامشخص"
                val vPlate = vehicleMap[r.vehicleId]?.plateNumber ?: ""
                writer.write("سرویس,${r.id},${vName},${vPlate},${r.date},${r.mileage},${r.category},${r.item},${r.cost},${r.notes}\n")
            }
            
            writer.flush()
            writer.close()
            
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "بک آپ نگهداری خودرو")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری بک‌آپ در گوگل درایو یا..."))
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
