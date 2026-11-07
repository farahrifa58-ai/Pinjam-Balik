package com.example.pinjambalik.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.pinjambalik.ui.theme.EmeraldSuccess
import com.example.pinjambalik.util.AppConfig
import com.example.pinjambalik.util.AppStrings
import com.example.pinjambalik.viewmodel.RentalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RentalViewModel,
    onNavigateBack: () -> Unit
) {
    // BackHandler ensures back button returns safely without crashing
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isFullVersion by viewModel.isFullVersion.collectAsState()
    val remainingSeconds by viewModel.trialRemainingSeconds.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()

    val strings = AppStrings.get(language)

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }

    var themeMenuExpanded by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }
    var retentionMenuExpanded by remember { mutableStateOf(false) }

    val themeOptions = listOf(
        "LIGHT" to strings.themeLight,
        "DARK" to strings.themeDark,
        "SYSTEM" to strings.themeSystem
    )

    val languageOptions = listOf(
        "id" to strings.langId,
        "en" to strings.langEn
    )

    val retentionOptions = listOf(
        30L to if (AppStrings.isIndonesian(language)) "30 Hari setelah transaksi selesai" else "30 Days after transaction completed",
        60L to if (AppStrings.isIndonesian(language)) "60 Hari setelah transaksi selesai" else "60 Days after transaction completed",
        90L to if (AppStrings.isIndonesian(language)) "90 Hari setelah transaksi selesai" else "90 Days after transaction completed",
        0L to if (AppStrings.isIndonesian(language)) "Simpan Selamanya (Tanpa Penghapusan Otomatis)" else "Keep Forever (No Auto Purge)"
    )

    fun sendEmailToDeveloper() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "Upgrade Lisensi Full Version PinjamBalik - ${AppConfig.DEVELOPER_NAME}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Halo ${AppConfig.DEVELOPER_NAME},\n\nSaya ingin menanyakan aktivasi lisensi PinjamBalik ke Full Version.\n\nTerima kasih."
            )
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Kirim Email"))
        } catch (_: Exception) {
            Toast.makeText(context, "Email: ${AppConfig.CONTACT_EMAIL}", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SECTION: TEMA TAMPILAN (TERANG DAN GELAP, DEFAULT TERANG)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.themeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val currentThemeLabel = themeOptions.find { it.first.equals(themeMode, ignoreCase = true) }?.second
                        ?: strings.themeLight

                    ExposedDropdownMenuBox(
                        expanded = themeMenuExpanded,
                        onExpandedChange = { themeMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentThemeLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.themeTitle) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = themeMenuExpanded,
                            onDismissRequest = { themeMenuExpanded = false }
                        ) {
                            themeOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontWeight = if (key.equals(themeMode, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.setThemeMode(key)
                                        themeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. SECTION: PILIHAN BAHASA (INDONESIA & ENGLISH)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.languageTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val currentLangLabel = languageOptions.find { it.first == language }?.second ?: strings.langId

                    ExposedDropdownMenuBox(
                        expanded = languageMenuExpanded,
                        onExpandedChange = { languageMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentLangLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.languageTitle) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            languageOptions.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontWeight = if (code == language) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        viewModel.setLanguage(code)
                                        languageMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. SECTION: KEAMANAN & PRIVASI DOKUMEN IDENTITAS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.retentionHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = strings.retentionDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val currentRetentionLabel = retentionOptions.find { it.first == retentionDays }?.second ?: "$retentionDays Hari"
                    ExposedDropdownMenuBox(
                        expanded = retentionMenuExpanded,
                        onExpandedChange = { retentionMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentRetentionLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.retentionLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = retentionMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = retentionMenuExpanded,
                            onDismissRequest = { retentionMenuExpanded = false }
                        ) {
                            retentionOptions.forEach { (days, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.updateRetentionDays(days)
                                        retentionMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showPurgeConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("purge_expired_docs_btn")
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.purgeButton)
                    }
                }
            }

            // 4. SECTION: KONTAK DEVELOPER RAFLINE DEV
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppConfig.DEVELOPER_NAME,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = AppConfig.CONTACT_EMAIL,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { sendEmailToDeveloper() }) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. BUTTON TENTANG APLIKASI DI BAGIAN BAWAH PENGATURAN (Sesuai Permintaan!)
            Button(
                onClick = { showAboutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("about_app_bottom_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.aboutAppButton,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // DIALOG: TENTANG APLIKASI (Full info, Rafline Dev, Status Lisensi: Full Version / Trial Version 60 Menit)
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Logo
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(90.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_pinjambalik_logo),
                                contentDescription = "Pinjam Balik Logo",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }

                    Text(
                        text = AppConfig.APP_NAME,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "by ${AppConfig.DEVELOPER_NAME}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    HorizontalDivider()

                    // Seputar Aplikasi
                    Text(
                        text = strings.appSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider()

                    // Details Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${strings.versionLabel}:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(AppConfig.APP_VERSION, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${strings.developerLabel}:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(AppConfig.DEVELOPER_NAME, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${strings.licenseStatus}:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFullVersion) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFullVersion) Icons.Default.Verified else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isFullVersion) EmeraldSuccess else Color(0xFFB45309),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFullVersion) "Full Version" else "Trial Version (60 Menit)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isFullVersion) EmeraldSuccess else Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    if (!isFullVersion) {
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7).copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sisa waktu trial: $mins menit $secs detik.\nSetelah habis, silakan hubungi Rafline Dev untuk aktivasi Full Version.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Button(
                            onClick = {
                                showAboutDialog = false
                                sendEmailToDeveloper()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upgrade ke Full Version", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { showAboutDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.close, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Purge confirmation dialog
    if (showPurgeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            icon = {
                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text("Bersihkan Dokumen Kedaluwarsa?") },
            text = {
                Text("Semua foto KTP dan SIM pada transaksi yang telah selesai lebih dari $retentionDays hari akan dihapus secara permanen dari perangkat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurgeConfirmDialog = false
                        viewModel.purgeExpiredDocuments { count ->
                            Toast.makeText(context, "Berhasil membersihkan dokumen dari $count transaksi", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Bersihkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
