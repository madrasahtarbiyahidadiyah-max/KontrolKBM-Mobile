package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val daerah: String,
    val kelas: String,
    val telatMenit: Int,
    val guruAktif: Boolean,
    val muridAktif: Boolean,
    val kekondusifan: String,
    val kerapian: String,
    val catatan: String,
    val isSynced: Boolean = false
)
