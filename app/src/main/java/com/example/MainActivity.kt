package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.FalakState
import com.example.data.InspectionEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
            val isDarkTheme = when (themePreference) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    InspeksiKbmApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspeksiKbmApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Observe State from ViewModel
    val falakState by viewModel.falakState.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()

    val selectedDaerah by viewModel.selectedDaerah.collectAsStateWithLifecycle()
    val lockedDaerah by viewModel.lockedDaerah.collectAsStateWithLifecycle()
    val selectedKelas by viewModel.selectedKelas.collectAsStateWithLifecycle()
    val telatMenit by viewModel.telatMenit.collectAsStateWithLifecycle()
    val guruAktif by viewModel.guruAktif.collectAsStateWithLifecycle()
    val muridAktif by viewModel.muridAktif.collectAsStateWithLifecycle()
    val kekondusifan by viewModel.kekondusifan.collectAsStateWithLifecycle()
    val kerapian by viewModel.kerapian.collectAsStateWithLifecycle()
    val catatan by viewModel.catatan.collectAsStateWithLifecycle()
    val googleSheetUrl by viewModel.googleSheetUrl.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCartDialog by remember { mutableStateOf(false) }
    var showDaerahDropdown by remember { mutableStateOf(false) }

    // Active daerah depends on whether locked or selected
    val activeDaerah = if (lockedDaerah.isNotEmpty()) lockedDaerah else selectedDaerah
    val isPosLocked = lockedDaerah.isNotEmpty()

    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp), // Leaves spacing for floating bottom dock
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. TOP HEADER ROW (Settings & Theme)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDark = when (themePreference) {
                        "light" -> false
                        "dark" -> true
                        else -> isSystemInDarkTheme()
                    }

                    // Sliding Theme Switcher Pill next to Settings
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Terang Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (!isDark) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewModel.updateThemePreference("light") }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "☀️", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Terang",
                                    color = if (!isDark) EmeraldSidogiri else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Gelap Option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isDark) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewModel.updateThemePreference("dark") }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🌙", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Gelap",
                                    color = if (isDark) EmeraldSidogiri else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Settings Gear Button
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 1. SMART COCKPIT HEADER CARD
            item {
                CockpitHeaderCard(
                    falakState = falakState
                )
            }

            // 2. POS/DAERAH SELECTION & LOCK BANNER
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Wilayah Inspeksi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isPosLocked) {
                            // Locked pill view
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MintSoft, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GreenOnTime, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "POS AKTIF",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CharcoalText.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = lockedDaerah.uppercase(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldSidogiri
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (inspections.isNotEmpty()) {
                                            // Show warning dialog or simply alert
                                            Toast.makeText(
                                                context,
                                                "Selesaikan rekap Pos ini terlebih dahulu atau kosongkan memori!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            viewModel.unlockDaerahForce()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, GreenOnTime),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Ganti Pos",
                                        color = EmeraldSidogiri,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            // Selection Dropdown view
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .border(1.5.dp, BorderMedium, RoundedCornerShape(12.dp))
                                        .clickable { showDaerahDropdown = true }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (selectedDaerah.isEmpty()) "-- PILIH POS / DAERAH --" else selectedDaerah,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedDaerah.isEmpty()) DarkGreyText else CharcoalText,
                                        fontSize = 15.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Icon",
                                        tint = CharcoalText
                                    )
                                }

                                DropdownMenu(
                                    expanded = showDaerahDropdown,
                                    onDismissRequest = { showDaerahDropdown = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(Color.White)
                                ) {
                                    viewModel.databaseRuangan.keys.forEach { pos ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = pos,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CharcoalText
                                                )
                                            },
                                            onClick = {
                                                viewModel.selectDaerah(pos)
                                                showDaerahDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. TACTILE CLASSROOMS CHIP GRID
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val availableRooms = viewModel.databaseRuangan[activeDaerah] ?: emptyList()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. Ketuk Ruang Kelas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${availableRooms.size} Ruang",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreyText
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeDaerah.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MintSoft, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, GreenOnTime, RoundedCornerShape(12.dp))
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Pos",
                                        tint = EmeraldSidogiri,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Silakan tentukan Wilayah Inspeksi di atas",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSidogiri,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Chunk list of classrooms into lists of 3 items for a grid layout
                            val chunkedRooms = availableRooms.chunked(3)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                chunkedRooms.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { roomName ->
                                            val isSelected = selectedKelas == roomName
                                            val isCompleted = inspections.any { it.kelas == roomName && it.daerah == activeDaerah }

                                            val containerColor by animateColorAsState(
                                                targetValue = when {
                                                    isSelected -> EmeraldSidogiri
                                                    isCompleted -> MintSoft
                                                    else -> Color(0xFFF8FAFC)
                                                }
                                            )
                                            val borderColor by animateColorAsState(
                                                targetValue = when {
                                                    isSelected -> EmeraldSidogiri
                                                    isCompleted -> GreenOnTime
                                                    else -> BorderMedium
                                                }
                                            )
                                            val textColor by animateColorAsState(
                                                targetValue = when {
                                                    isSelected -> Color.White
                                                    isCompleted -> GreenOnTimeDark
                                                    else -> CharcoalText
                                                }
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(12.dp))
                                                    .background(containerColor, RoundedCornerShape(12.dp))
                                                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                                    .clickable { viewModel.selectKelas(roomName) }
                                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    if (isCompleted && !isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Done",
                                                            tint = EmeraldSidogiri,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                    }
                                                    Text(
                                                        text = viewModel.formatRoomNameShort(roomName),
                                                        color = textColor,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        // Fill remaining column cells if row is not full
                                        if (rowItems.size < 3) {
                                            repeat(3 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. KBM INSPECTION FORM CARD
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.testTag("form_control")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. Parameter Inspeksi" + if (selectedKelas.isNotEmpty()) " : $selectedKelas" else "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Selected Room Alert Warning Banner
                        if (selectedKelas.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RedLateBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, RedLate, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = RedLateDark,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ketuk kotak Ruang Kelas di atas terlebih dahulu!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RedLateDark
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Form Fields (Disabled or visually faded if no room selected, but responsive)
                        val isFormActive = selectedKelas.isNotEmpty()
                        val contentAlpha = if (isFormActive) 1f else 0.5f

                        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = contentAlpha)) {

                            // A. Keterlambatan Guru Slider / Input presets
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Keterlambatan Guru",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CharcoalText
                                    )

                                    // Badge Status
                                    val isLate = telatMenit > 0
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isLate) RedLateBg else GreenOnTimeBg,
                                                shape = RoundedCornerShape(50.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isLate) RedLate else GreenOnTime,
                                                shape = RoundedCornerShape(50.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isLate) "⚠️ Telat ${telatMenit}m" else "✓ Tepat Waktu",
                                            color = if (isLate) RedLateDark else GreenOnTimeDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
                                        .border(1.5.dp, BorderLight, RoundedCornerShape(14.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Number display / Manual Input
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .width(75.dp)
                                            .background(Color.White, RoundedCornerShape(10.dp))
                                            .border(1.dp, BorderMedium, RoundedCornerShape(10.dp))
                                            .padding(vertical = 4.dp)
                                    ) {
                                        BasicTextFieldWithoutLabel(
                                            value = if (telatMenit == 0) "0" else telatMenit.toString(),
                                            onValueChange = { newVal ->
                                                if (isFormActive) {
                                                    val minutes = newVal.filter { it.isDigit() }.toIntOrNull() ?: 0
                                                    viewModel.setTelatMenit(minutes)
                                                }
                                            },
                                            modifier = Modifier.width(40.dp),
                                            enabled = isFormActive
                                        )
                                        Text(
                                            text = "m",
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGreyText,
                                            fontSize = 14.sp
                                        )
                                    }

                                    // Preset chips Row
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(0, 5, 10, 15).forEach { presetMin ->
                                            val isActivePreset = telatMenit == presetMin
                                            val presetBgColor by animateColorAsState(
                                                targetValue = if (isActivePreset) CharcoalText else Color.White
                                            )
                                            val presetTextColor by animateColorAsState(
                                                targetValue = if (isActivePreset) Color.White else CharcoalText
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(presetBgColor, RoundedCornerShape(10.dp))
                                                    .border(1.dp, BorderMedium, RoundedCornerShape(10.dp))
                                                    .clickable(enabled = isFormActive) {
                                                        viewModel.setTelatMenit(presetMin)
                                                    }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${presetMin}m",
                                                    color = presetTextColor,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // B. Verifikasi KBM (Guru/Murid Aktif Checkboxes)
                            Column {
                                Text(
                                    text = "Verifikasi KBM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Guru Aktif card
                                    KbmToggleCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Guru Aktif",
                                        icon = Icons.Default.Person,
                                        isChecked = guruAktif,
                                        onCheckedChange = { if (isFormActive) viewModel.toggleGuruAktif(it) },
                                        enabled = isFormActive
                                    )

                                    // Murid Aktif card
                                    KbmToggleCard(
                                        modifier = Modifier.weight(1f),
                                        label = "Murid Aktif",
                                        icon = Icons.Default.AccountBox,
                                        isChecked = muridAktif,
                                        onCheckedChange = { if (isFormActive) viewModel.toggleMuridAktif(it) },
                                        enabled = isFormActive
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // C. Suasana / Kekondusifan (Segmented Control)
                            Column {
                                Text(
                                    text = "Suasana / Kekondusifan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                SegmentedControl(
                                    options = listOf("Sangat Baik", "Baik", "Cukup", "Kurang"),
                                    selectedOption = kekondusifan,
                                    onOptionSelected = { if (isFormActive) viewModel.selectKekondusifan(it) },
                                    enabled = isFormActive
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // D. Kerapian Kelas (Segmented Control)
                            Column {
                                Text(
                                    text = "Kerapian Kelas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                SegmentedControl(
                                    options = listOf("Sangat Baik", "Baik", "Cukup", "Kurang"),
                                    selectedOption = kerapian,
                                    onOptionSelected = { if (isFormActive) viewModel.selectKerapian(it) },
                                    enabled = isFormActive
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // E. Catatan Opsional Textarea
                            Column {
                                Text(
                                    text = "Catatan Opsional",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CharcoalText
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = catatan,
                                    onValueChange = { if (isFormActive) viewModel.updateCatatan(it) },
                                    placeholder = {
                                        Text(
                                            text = "Tuliskan temuan di kelas ini...",
                                            color = DarkGreyText,
                                            fontSize = 14.sp
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color(0xFFF8FAFC),
                                        focusedBorderColor = EmeraldSidogiri,
                                        unfocusedBorderColor = BorderMedium,
                                        disabledContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.5f)
                                    ),
                                    enabled = isFormActive,
                                    maxLines = 3
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // F. Action Button REKAM & SIMPAN
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.recordRoomInspection(context) {
                                        Toast.makeText(context, "Inspeksi disimpan!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("submit_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CharcoalText,
                                    disabledContainerColor = CharcoalText.copy(alpha = 0.5f)
                                ),
                                enabled = isFormActive && !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Save Icon",
                                            tint = GreenOnTime,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "REKAM & SIMPAN RUANGAN INI",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. FLOATING BOTTOM BAR / DOCK
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            FloatingDock(
                inspectionsCount = inspections.size,
                onLeftSectionClick = {
                    showCartDialog = true
                },
                onWhatsAppClick = {
                    val rawText = viewModel.generateWhatsAppText()
                    if (rawText.isNotEmpty()) {
                        val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(rawText))
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                },
                enabled = inspections.isNotEmpty()
            )
        }

        // 6. POPUP DIALOGS (SETTINGS & CART)
        if (showSettingsDialog) {
            SettingsDialog(
                currentUrl = googleSheetUrl,
                onDismiss = { showSettingsDialog = false },
                onSave = { newUrl ->
                    viewModel.saveSheetUrl(context, newUrl)
                    showSettingsDialog = false
                },
                onSyncClick = {
                    viewModel.syncUnsyncedItems(context)
                },
                isSyncing = isSyncing
            )
        }

        if (showCartDialog) {
            CartDialog(
                inspections = inspections,
                activePos = activeDaerah,
                onDismiss = { showCartDialog = false },
                onDelete = { item -> viewModel.removeInspection(item.id) },
                onClearAll = {
                    viewModel.clearAllInspections(context)
                    showCartDialog = false
                },
                onSendWhatsApp = {
                    val rawText = viewModel.generateWhatsAppText()
                    if (rawText.isNotEmpty()) {
                        val uri = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(rawText))
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                        showCartDialog = false
                    }
                }
            )
        }
    }
}

// ----------------- SUB COMPOSABLES & VIEW UTILS -----------------

@Composable
fun CockpitHeaderCard(
    falakState: FalakState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(EmeraldSidogiri, DarkSidogiri)
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left kop emblem and title
                Row(
                    modifier = Modifier
                        .weight(0.48f)
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mmu_header_logo),
                            contentDescription = "Logo MMU",
                            contentScale = ContentScale.Fit,
                            colorFilter = null,
                            modifier = Modifier
                                .height(72.dp)
                                .fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "SISTEM PENGAWASAN\nKBM HARIAN",
                            color = Color(0xFFCCFBF1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(84.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )

                // Right falak time & date cockpit
                Column(
                    modifier = Modifier
                        .weight(0.52f)
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WAKTU ISTIWAK (WIS)",
                        color = GoldFalak,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = falakState.wisTime,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = falakState.dateText,
                        color = Color(0xFFF0FDF4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun KbmToggleCard(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (isChecked) MintSoft else Color(0xFFF8FAFC)
    )
    val borderColor by animateColorAsState(
        targetValue = if (isChecked) GreenOnTime else BorderMedium
    )
    val contentColor by animateColorAsState(
        targetValue = if (isChecked) EmeraldSidogiri else CharcoalText
    )

    Box(
        modifier = modifier
            .background(cardBgColor, RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isChecked) EmeraldSidogiri else DarkGreyText,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            val bgSegmentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Transparent
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) EmeraldSidogiri else CharcoalText
            )
            val textWeight = if (isSelected) FontWeight.Black else FontWeight.Bold

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bgSegmentColor)
                    .clickable(enabled = enabled) { onOptionSelected(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    color = textColor,
                    fontWeight = textWeight,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FloatingDock(
    inspectionsCount: Int,
    onLeftSectionClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left cart click triggers popup
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(0.5f)
                    .clickable { onLeftSectionClick() }
            ) {
                Text(
                    text = inspectionsCount.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Ruang Kelas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 12.sp
                    )
                    Text(
                        text = "LIHAT MEMORI ➔",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldSidogiri,
                        lineHeight = 10.sp
                    )
                }
            }

            // WhatsApp send button
            Button(
                onClick = onWhatsAppClick,
                enabled = enabled,
                modifier = Modifier
                    .weight(0.5f)
                    .height(48.dp)
                    .testTag("wa_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366),
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "WhatsApp",
                        tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KIRIM WA ($inspectionsCount)",
                        color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BasicTextFieldWithoutLabel(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = CharcoalText,
            unfocusedTextColor = CharcoalText,
            disabledTextColor = CharcoalText.copy(alpha = 0.5f)
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        ),
        modifier = modifier,
        enabled = enabled
    )
}

// ----------------- MODAL DIALOGS -----------------

@Composable
fun SettingsDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onSyncClick: () -> Unit,
    isSyncing: Boolean
) {
    var urlInput by remember { mutableStateOf(currentUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengaturan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Text(
                    text = "Aplikasi ini mendukung sinkronisasi otomatis ke Google Sheet harian Anda.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Column {
                    Text(
                        text = "URL WEB APP GOOGLE APPS SCRIPT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text(
                                "https://script.google.com/macros/s/.../exec",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = EmeraldSidogiri,
                            unfocusedBorderColor = BorderMedium
                        ),
                        singleLine = true
                    )
                }

                // Sync action trigger
                Button(
                    onClick = onSyncClick,
                    enabled = currentUrl.isNotBlank() && !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MintSoft),
                    border = BorderStroke(1.dp, GreenOnTime),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = EmeraldSidogiri, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync", tint = EmeraldSidogiri)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "SINKRONISASI DATA MANDIRI", color = EmeraldSidogiri, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }

                // Brief Guide in Indonesian
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Cara Menghubungkan Google Sheet:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Buka Google Sheet rekap Anda.\n" +
                                    "2. Klik Ekstensi -> Apps Script.\n" +
                                    "3. Tempel fungsi doPost(e) pembaca JSON.\n" +
                                    "4. Terapkan sebagai Aplikasi Web (Akses: Siapa Saja).\n" +
                                    "5. Salin URL Web App dan tempel di kotak atas.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderMedium),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSave(urlInput) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSidogiri),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CartDialog(
    inspections: List<InspectionEntity>,
    activePos: String,
    onDismiss: () -> Unit,
    onDelete: (InspectionEntity) -> Unit,
    onClearAll: () -> Unit,
    onSendWhatsApp: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Cart Icon",
                            tint = EmeraldSidogiri,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daftar Tunggu WhatsApp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                // Active POS Pill banner
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "📍 POS AKTIF: ${activePos.uppercase()}",
                        color = if (isSystemInDarkTheme()) Color(0xFFCCFBF1) else EmeraldSidogiri,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List of inspection entries
                Box(modifier = Modifier.weight(1f)) {
                    if (inspections.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Belum ada ruangan yang disimpan.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Reverse order to match index.html latest saved on top, or standard listing
                            itemsIndexed(inspections.reversed()) { idx, item ->
                                val isLate = item.telatMenit > 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(0.75f)) {
                                        Text(
                                            text = "${idx + 1}. ${item.kelas}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Suasana: ${item.kekondusifan} | Rapi: ${item.kerapian}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        if (item.catatan.isNotBlank()) {
                                            Text(
                                                text = "Note: \"${item.catatan}\"",
                                                fontSize = 10.sp,
                                                color = EmeraldSidogiri,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.weight(0.25f),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tardiness Badge
                                        Text(
                                            text = if (isLate) "Telat ${item.telatMenit}m" else "Tepat",
                                            color = if (isLate) RedLateDark else GreenOnTimeDark,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )

                                        IconButton(
                                            onClick = { onDelete(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete",
                                                tint = RedLateDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons at bottom of Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearAll,
                        enabled = inspections.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedLateDark),
                        border = BorderStroke(1.dp, if (inspections.isNotEmpty()) RedLate else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Kosongkan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSendWhatsApp,
                        enabled = inspections.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Kirim WA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
