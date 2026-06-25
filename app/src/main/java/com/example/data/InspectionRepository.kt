package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class InspectionRepository(private val inspectionDao: InspectionDao) {

    val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun insert(inspection: InspectionEntity): Long {
        return withContext(Dispatchers.IO) {
            inspectionDao.insertInspection(inspection)
        }
    }

    suspend fun deleteById(id: Int) {
        withContext(Dispatchers.IO) {
            inspectionDao.deleteInspectionById(id)
        }
    }

    suspend fun clearAll() {
        withContext(Dispatchers.IO) {
            inspectionDao.clearAllInspections()
        }
    }

    /**
     * Uploads an inspection to the Google Apps Script Web App URL.
     * Returns true on success, false on failure.
     */
    suspend fun uploadToGoogleSheet(url: String, inspection: InspectionEntity): Boolean {
        if (url.isBlank()) return false
        return withContext(Dispatchers.IO) {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val dateStr = sdf.format(java.util.Date(inspection.timestamp))
                val guruStr = if (inspection.guruAktif) "Aktif" else "Tidak Aktif"
                val muridStr = if (inspection.muridAktif) "Aktif" else "Tidak Aktif"

                val json = JSONObject().apply {
                    // 1. Exact Google Sheet Column Headers (Very common for header-mapping dynamic scripts)
                    put("Timestamp", dateStr)
                    put("Daerah", inspection.daerah)
                    put("Kelas", inspection.kelas)
                    put("Telat (Menit)", inspection.telatMenit)
                    put("Guru Aktif", guruStr)
                    put("Murid Aktif", muridStr)
                    put("Kekondusifan", inspection.kekondusifan)
                    put("Kerapian", inspection.kerapian)
                    put("Catatan", inspection.catatan)

                    // 2. Lowercase and standard camelCase/snake_case variants (for hardcoded property matching)
                    put("timestamp", dateStr)
                    put("daerah", inspection.daerah)
                    put("kelas", inspection.kelas)
                    
                    put("telat", inspection.telatMenit)
                    put("telatMenit", inspection.telatMenit)
                    put("telat_menit", inspection.telatMenit)
                    put("telat (menit)", inspection.telatMenit)

                    put("guruAktif", guruStr)
                    put("guru_aktif", guruStr)
                    put("guru", guruStr)
                    
                    put("muridAktif", muridStr)
                    put("murid_aktif", muridStr)
                    put("murid", muridStr)

                    put("kekondusifan", inspection.kekondusifan)
                    put("kerapian", inspection.kerapian)
                    put("catatan", inspection.catatan)

                    // 3. Raw boolean properties just in case
                    put("guruAktifRaw", inspection.guruAktif)
                    put("muridAktifRaw", inspection.muridAktif)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d("InspectionRepository", "Upload successful: $responseBody")
                        // Mark as synced locally since the server responded successfully
                        inspectionDao.updateInspectionSyncStatus(inspection.id, true)
                        true
                    } else {
                        Log.e("InspectionRepository", "Upload failed: ${response.code}")
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e("InspectionRepository", "Error uploading inspection", e)
                false
            }
        }
    }

    suspend fun syncUnsynced(url: String): Int {
        if (url.isBlank()) return 0
        var successCount = 0
        val unsynced = inspectionDao.getUnsyncedInspections()
        for (inspection in unsynced) {
            val ok = uploadToGoogleSheet(url, inspection)
            if (ok) successCount++
        }
        return successCount
    }
}
