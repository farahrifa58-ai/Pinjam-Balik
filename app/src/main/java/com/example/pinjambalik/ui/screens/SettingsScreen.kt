package com.example.pinjambalik.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pinjambalik.ui.theme.EmeraldSuccess
import com.example.pinjambalik.util.AppConfig
import com.example.pinjambalik.viewmodel.RentalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RentalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val retentionDays by viewModel.retentionDays.collectAsState()
    val isTrialActive by viewModel.isTrialActive.collectAsState()
    val remainingSeconds by viewModel.trialRemainingSeconds.collectAsState()

    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    var retentionMenuExpanded by remember { mutableStateOf(false) }

    val retentionOptions = listOf(
        30L to "30 Hari setelah transaksi selesai",
        60L to "60 Hari setelah transaksi selesai",
        90L to "90 Hari setelah transaksi selesai",
        0L to "Simpan Selamanya (Tanpa Penghapusan Otomatis)"
    )

    fun sendEmailToDeveloper() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
            putExtra(Intent.EXTRA_SUBJECT, "Pertanyaan / Upgrade Lisensi PinjamBalik")
            putExtra(
                Intent.EXTRA_TEXT,
                "Halo Mas Rendy,\n\nSaya ingin menanyakan lisensi / upgrade PinjamBalik ke Full Version.\n\nTerima kasih."
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
                title = { Text("Pengaturan & Privasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
            // SECTION: KEAMANAN & PRIVASI DOKUMEN IDENTITAS
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
                            text = "Masa Retensi Dokumen Identitas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Untuk melindungi privasi pelanggan, Anda dapat mengatur masa penyimpanan foto KTP/SIM. Dokumen identitas transaksi yang telah selesai akan dibersihkan setelah periode tersebut.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Retention Dropdown
                    val currentLabel = retentionOptions.find { it.first == retentionDays }?.second ?: "$retentionDays Hari"
                    ExposedDropdownMenuBox(
                        expanded = retentionMenuExpanded,
                        onExpandedChange = { retentionMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = currentLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Masa Simpan Dokumen") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = retentionMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
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
                        Text("Bersihkan Dokumen Kedaluwarsa Sekarang")
                    }
                }
            }

            // SECTION: TENTANG APLIKASI & LISENSI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tentang Aplikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nama Aplikasi:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("PinjamBalik", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Versi:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0.0 (Build Stable)", fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Lisensi:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTrialActive) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (isTrialActive) "Trial (60 Menit)" else "Full Version (Aktif Selamanya)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isTrialActive) Color(0xFF92400E) else EmeraldSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Divider()

                    Text(
                        text = "Kontak Pengembang / Upgrade Lisensi:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = AppConfig.CONTACT_EMAIL,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Hubungi untuk dukungan & aktivasi",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { sendEmailToDeveloper() }) {
                                Icon(Icons.Default.Email, contentDescription = "Kirim Email", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Developer / Mode Switcher Card (Convenient tool to preview both modes!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Mode Lisensi (Pengujian):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Simulasi Mode Trial 60 Menit",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isTrialActive) "Sisa ${remainingSeconds / 60}m ${remainingSeconds % 60}s" else "Full Version aktif (tanpa banner atas)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isTrialActive,
                            onCheckedChange = { checked ->
                                viewModel.toggleForceTrial(checked)
                            },
                            modifier = Modifier.testTag("toggle_trial_switch")
                        )
                    }

                    if (isTrialActive) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetTrial()
                                Toast.makeText(context, "Timer trial di-reset ke 60 menit", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Timer Trial (60 Menit)")
                        }
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
