package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FalakEngine
import com.example.data.FalakState
import com.example.data.InspectionEntity
import com.example.data.InspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = InspectionRepository(database.inspectionDao())

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("mmu_idadiyah_prefs", Context.MODE_PRIVATE)

    // Falak Engine for WIS Time & Islamic Date
    private val falakEngine = FalakEngine()
    val falakState: StateFlow<FalakState> = falakEngine.falakState

    // Local DB Inspections List
    val inspections: StateFlow<List<InspectionEntity>> = repository.allInspections
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form states
    private val _selectedDaerah = MutableStateFlow("")
    val selectedDaerah: StateFlow<String> = _selectedDaerah.asStateFlow()

    private val _lockedDaerah = MutableStateFlow("")
    val lockedDaerah: StateFlow<String> = _lockedDaerah.asStateFlow()

    private val _selectedKelas = MutableStateFlow("")
    val selectedKelas: StateFlow<String> = _selectedKelas.asStateFlow()

    private val _telatMenit = MutableStateFlow(0)
    val telatMenit: StateFlow<Int> = _telatMenit.asStateFlow()

    private val _guruAktif = MutableStateFlow(false)
    val guruAktif: StateFlow<Boolean> = _guruAktif.asStateFlow()

    private val _muridAktif = MutableStateFlow(false)
    val muridAktif: StateFlow<Boolean> = _muridAktif.asStateFlow()

    private val _kekondusifan = MutableStateFlow("")
    val kekondusifan: StateFlow<String> = _kekondusifan.asStateFlow()

    private val _kerapian = MutableStateFlow("")
    val kerapian: StateFlow<String> = _kerapian.asStateFlow()

    private val _catatan = MutableStateFlow("")
    val catatan: StateFlow<String> = _catatan.asStateFlow()

    // Config: Apps Script Web App URL
    private val defaultUrl = "https://script.google.com/macros/s/AKfycby5nTQ6tbpI-JQYSRwNBxpFdxSCjQ_f90FX9qqGEjRSQD_ka0rrUC8DQLPBPxSUE_gL/exec"
    private val oldDefaultUrl = "https://script.google.com/macros/s/AKfycbw548QtPjt9D-xFwipoVbQP6bxOwQhTNnTyBHYNElet9DKkDQgym-lcfg0M4M8ZRoc-/exec"
    private val _googleSheetUrl = MutableStateFlow(
        sharedPrefs.getString("sheet_url", null)?.let { saved ->
            if (saved.contains("docs.google.com/spreadsheets") || saved.isBlank() || saved == oldDefaultUrl) {
                sharedPrefs.edit().putString("sheet_url", defaultUrl).apply()
                defaultUrl
            } else {
                saved
            }
        } ?: run {
            sharedPrefs.edit().putString("sheet_url", defaultUrl).apply()
            defaultUrl
        }
    )
    val googleSheetUrl: StateFlow<String> = _googleSheetUrl.asStateFlow()

    // Theme preference state: "system", "light", "dark"
    private val _themePreference = MutableStateFlow(sharedPrefs.getString("theme_pref", "system") ?: "system")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    fun updateThemePreference(pref: String) {
        _themePreference.value = pref
        sharedPrefs.edit().putString("theme_pref", pref).apply()
    }

    // Loading & Sync feedback
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Database of rooms
    val databaseRuangan = mapOf(
        "Daerah M" to listOf("M-03", "M-04", "M-05", "M-06", "M-07", "M-08", "M-09", "M-10", "M-11", "M-12", "M-13", "M-14"),
        "Daerah L dan Surau L" to listOf(
            "L-01", "L-02", "L-04", "L-05", "L-06", "L-07", "L-08", "L-09", "L-10",
            "Surau L Lt.1 - Kelas 1", "Surau L Lt.1 - Kelas 2", "Surau L Lt.1 - Kelas 3", "Surau L Lt.1 - Kelas 4", "Surau L Lt.1 - Kelas 5", "Surau L Lt.1 - Kelas 6",
            "Surau L Lt.2 - Kelas 1", "Surau L Lt.2 - Kelas 2", "Surau L Lt.2 - Kelas 3", "Surau L Lt.2 - Kelas 4", "Surau L Lt.2 - Kelas 5", "Surau L Lt.2 - Kelas 6"
        ),
        "Daerah N" to listOf("N-01", "N-02", "N-05", "N-06", "N-07", "N-08", "N-09", "N-10"),
        "Daerah S" to listOf("S-02", "S-03", "S-04", "S-05", "S-06", "S-07", "S-08", "S-09", "S-10"),
        "Daerah R" to listOf("R-03", "R-04", "R-05", "R-06", "R-07", "R-08", "R-09", "R-10", "R-11", "R-12", "R-13", "R-14", "R-15"),
        "Mabna Al-Ghazali" to listOf(
            "Ruang 3.08", "Ruang 3.09", "Ruang 3.10",
            "Kamar 4.01", "Kamar 4.02", "Kamar 4.03", "Kamar 4.04", "Kamar 4.05", "Kamar 4.06", "Kamar 4.07", "Kamar 4.08", "Kamar 4.09", "Kamar 4.10"
        ),
        "Mushalla Al-Ghazali" to listOf(
            "Mushalla Al-Ghazali 01", "Mushalla Al-Ghazali 02", "Mushalla Al-Ghazali 03",
            "Mushalla Al-Ghazali 04", "Mushalla Al-Ghazali 05", "Mushalla Al-Ghazali 06"
        ),
        "Mabna An-Nawawi" to listOf(
            "Ruang 3.01", "Ruang 3.02", "Ruang 3.03", "Ruang 3.04", "Ruang 3.05", "Ruang 3.06", "Ruang 3.07", "Ruang 3.08", "Ruang 3.09", "Ruang 3.10", "Ruang 3.11", "Ruang 3.12",
            "Ruang 4.01", "Ruang 4.02", "Ruang 4.03", "Ruang 4.04", "Ruang 4.05", "Ruang 4.06", "Ruang 4.07", "Ruang 4.08", "Ruang 4.09", "Ruang 4.10", "Ruang 4.11", "Ruang 4.12"
        ),
        "Barat An-Nawawi" to listOf("Semua Kelas"),
        "Ar-Raudhah 1" to listOf("Semua Kelas"),
        "Ar-Raudhah 2" to listOf("Semua Kelas")
    )

    // Format helper to shorten visual names of buttons
    fun formatRoomNameShort(name: String): String {
        return name
            .replace("Surau L Lt.1 - Kelas ", "SL1-")
            .replace("Surau L Lt.2 - Kelas ", "SL2-")
            .replace("Ruang ", "Rg.")
            .replace("Kamar ", "Km.")
            .replace("Mushalla Al-Ghazali ", "Mushalla ")
            .replace("Barat An-Nawawi (Semua)", "Barat Nawawi")
            .replace("Ar-Raudhah 1 (Semua)", "Raudhah 1")
            .replace("Ar-Raudhah 2 (Semua)", "Raudhah 2")
    }

    fun selectDaerah(daerah: String) {
        if (_lockedDaerah.value.isEmpty()) {
            _selectedDaerah.value = daerah
            _selectedKelas.value = ""
        }
    }

    fun selectKelas(kelas: String) {
        _selectedKelas.value = kelas
    }

    fun setTelatMenit(mnt: Int) {
        _telatMenit.value = mnt
    }

    fun toggleGuruAktif(active: Boolean) {
        _guruAktif.value = active
    }

    fun toggleMuridAktif(active: Boolean) {
        _muridAktif.value = active
    }

    fun selectKekondusifan(value: String) {
        _kekondusifan.value = value
    }

    fun selectKerapian(value: String) {
        _kerapian.value = value
    }

    fun updateCatatan(text: String) {
        _catatan.value = text
    }

    fun updateSheetUrl(url: String) {
        _googleSheetUrl.value = url
        sharedPrefs.edit().putString("sheet_url", url).apply()
    }

    fun saveSheetUrl(context: Context, url: String) {
        updateSheetUrl(url)
        Toast.makeText(context, "URL Google Sheet disimpan!", Toast.LENGTH_SHORT).show()
    }

    // Records the current form into Room DB and locks the Pos (Daerah)
    fun recordRoomInspection(context: Context, onSuccess: () -> Unit) {
        val currentDaerah = if (_lockedDaerah.value.isNotEmpty()) _lockedDaerah.value else _selectedDaerah.value
        val currentKelas = _selectedKelas.value

        if (currentDaerah.isEmpty()) {
            Toast.makeText(context, "Silakan pilih Pos/Daerah!", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentKelas.isEmpty()) {
            Toast.makeText(context, "Silakan ketuk salah satu Ruang Kelas!", Toast.LENGTH_SHORT).show()
            return
        }
        if (_kekondusifan.value.isEmpty() || _kerapian.value.isEmpty()) {
            Toast.makeText(context, "Mohon tentukan parameter Kekondusifan & Kerapian!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isSaving.value = true

            // Lock current daerah
            _lockedDaerah.value = currentDaerah

            val newInspection = InspectionEntity(
                daerah = currentDaerah,
                kelas = currentKelas,
                telatMenit = _telatMenit.value,
                guruAktif = _guruAktif.value,
                muridAktif = _muridAktif.value,
                kekondusifan = _kekondusifan.value,
                kerapian = _kerapian.value,
                catatan = _catatan.value,
                isSynced = false
            )

            // Save to database
            val id = repository.insert(newInspection)

            // Immediately try to sync to Google Sheet in background if URL is provided
            val sheetUrl = _googleSheetUrl.value
            if (sheetUrl.isNotBlank()) {
                val finalInspection = newInspection.copy(id = id.toInt())
                val cloudSuccess = repository.uploadToGoogleSheet(sheetUrl, finalInspection)
                if (cloudSuccess) {
                    Toast.makeText(context, "Berhasil disimpan ke HP & Google Sheet!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Tersimpan Lokal (Gagal upload ke Google Sheet)", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Berhasil disimpan ke memori HP!", Toast.LENGTH_SHORT).show()
            }

            // Reset inputs except Daerah
            _selectedKelas.value = ""
            _telatMenit.value = 0
            _guruAktif.value = false
            _muridAktif.value = false
            _kekondusifan.value = ""
            _kerapian.value = ""
            _catatan.value = ""

            _isSaving.value = false
            onSuccess()
        }
    }

    fun removeInspection(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            // If we cleared all inspections, unlock the Daerah selection
            checkAndUnlockDaerah()
        }
    }

    fun clearAllInspections(context: Context) {
        viewModelScope.launch {
            repository.clearAll()
            _lockedDaerah.value = ""
            Toast.makeText(context, "Semua data terekap dibersihkan!", Toast.LENGTH_SHORT).show()
        }
    }

    fun unlockDaerahForce() {
        _lockedDaerah.value = ""
        _selectedKelas.value = ""
    }

    private fun checkAndUnlockDaerah() {
        viewModelScope.launch {
            // Check if there are still inspections. If empty, unlock.
            // But we can check via current state of inspections flow
            if (inspections.value.isEmpty()) {
                _lockedDaerah.value = ""
            }
        }
    }

    // Direct synchronization of any unsynced items
    fun syncUnsyncedItems(context: Context) {
        val url = _googleSheetUrl.value
        if (url.isBlank()) {
            Toast.makeText(context, "Harap pasang URL Google Sheet di Pengaturan dahulu!", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            val count = repository.syncUnsynced(url)
            _isSyncing.value = false
            if (count > 0) {
                Toast.makeText(context, "$count data berhasil disinkronkan ke Google Sheet!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Semua data sudah sinkron!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Creates the text template for sending to WhatsApp
    fun generateWhatsAppText(): String {
        val list = inspections.value
        if (list.isEmpty()) return ""

        val activeDaerah = if (_lockedDaerah.value.isNotEmpty()) _lockedDaerah.value else _selectedDaerah.value
        val fState = falakState.value

        val builder = java.lang.StringBuilder()
        builder.append("*REKAP KONTROL KBM MMU IDADIYAH*\n")
        builder.append("*Waktu:* ${fState.dateText} (${fState.wisTime} WIS)\n")
        builder.append("*Pos:* $activeDaerah\n\n")

        list.reversed().forEachIndexed { index, item ->
            val guruStatus = if (item.guruAktif) "Aktif" else "Tidak Aktif"
            val muridStatus = if (item.muridAktif) "Aktif" else "Tidak Aktif"
            val telatText = if (item.telatMenit == 0) "Tepat Waktu" else "*Terlambat ${item.telatMenit} Menit*"

            builder.append("*${index + 1}. ${item.kelas}*\n")
            builder.append(" • Guru    : $telatText\n")
            builder.append(" • KBM     : Guru ($guruStatus) | Murid ($muridStatus)\n")
            builder.append(" • Suasana : Kondusif (${item.kekondusifan}) | Rapi (${item.kerapian})\n")
            if (item.catatan.trim().isNotEmpty()) {
                builder.append(" • Catatan : _\"${item.catatan}\"_\n")
            }
            builder.append("\n")
        }

        builder.append("_Total Terekap: ${list.size} Ruangan_")
        return builder.toString()
    }

    override fun onCleared() {
        super.onCleared()
        falakEngine.stop()
    }
}
