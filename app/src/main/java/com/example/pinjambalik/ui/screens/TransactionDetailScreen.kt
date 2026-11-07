package com.example.pinjambalik.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pinjambalik.data.model.PhotoType
import com.example.pinjambalik.data.model.RentalTransaction
import com.example.pinjambalik.data.model.TransactionPhoto
import com.example.pinjambalik.ui.components.ImageViewerDialog
import com.example.pinjambalik.ui.components.MaskedIdentityRow
import com.example.pinjambalik.ui.components.PhotoPickerModalSheet
import com.example.pinjambalik.ui.components.PhotoThumbnailCard
import com.example.pinjambalik.ui.components.SensitiveIdentityDocumentBox
import com.example.pinjambalik.ui.theme.EmeraldSuccess
import com.example.pinjambalik.util.PhotoStorageManager
import com.example.pinjambalik.util.PrivacyUtils
import com.example.pinjambalik.viewmodel.RentalViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    viewModel: RentalViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val transaction by viewModel.repository.getTransaction(transactionId).collectAsState(initial = null)
    val photos by viewModel.repository.getPhotos(transactionId).collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Ringkasan", "Identitas", "Foto Barang", "Pengembalian", "Pembayaran")

    var showReturnDialog by remember { mutableStateOf(false) }
    var showDeleteTxConfirm by remember { mutableStateOf(false) }
    var showClearIdentityPhotosConfirm by remember { mutableStateOf(false) }

    // Full screen image viewer state
    var viewingPhotoFile by remember { mutableStateOf<File?>(null) }
    var viewingPhotoCaption by remember { mutableStateOf<String?>(null) }

    // Add extra photo state
    var showAddPhotoSheet by remember { mutableStateOf(false) }
    var extraPhotoType by remember { mutableStateOf(PhotoType.BEFORE_HANDOVER) }

    if (transaction == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detail Transaksi") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Memuat data transaksi...")
            }
        }
        return
    }

    val tx = transaction!!
    val isCompleted = tx.transactionStatus == "SELESAI"

    fun shareWhatsAppReceipt() {
        val text = PrivacyUtils.generateWhatsAppReceipt(tx)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Nota Sewa via WhatsApp")
        context.startActivity(shareIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = tx.itemName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${tx.invoiceCode} • ${tx.customerName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { shareWhatsAppReceipt() },
                        modifier = Modifier.testTag("share_whatsapp_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan Nota")
                    }
                    IconButton(
                        onClick = { showDeleteTxConfirm = true },
                        modifier = Modifier.testTag("delete_tx_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Transaksi", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("detail_tab_$index")
                    )
                }
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> TabSummary(
                        tx = tx,
                        photos = photos,
                        onOpenReturn = { showReturnDialog = true },
                        onShare = { shareWhatsAppReceipt() },
                        onViewPhoto = { file, caption ->
                            viewingPhotoFile = file
                            viewingPhotoCaption = caption
                        }
                    )
                    1 -> TabCustomerIdentity(
                        tx = tx,
                        viewModel = viewModel,
                        onClearAllIdentity = { showClearIdentityPhotosConfirm = true },
                        onViewPhoto = { file, caption ->
                            viewingPhotoFile = file
                            viewingPhotoCaption = caption
                        }
                    )
                    2 -> TabItemPhotos(
                        tx = tx,
                        photos = photos,
                        onAddPhoto = { type ->
                            extraPhotoType = type
                            showAddPhotoSheet = true
                        },
                        onDeletePhoto = { photo ->
                            viewModel.deletePhoto(photo)
                        },
                        onViewPhoto = { file, caption ->
                            viewingPhotoFile = file
                            viewingPhotoCaption = caption
                        }
                    )
                    3 -> TabReturnDocumentation(
                        tx = tx,
                        photos = photos,
                        onOpenReturn = { showReturnDialog = true },
                        onViewPhoto = { file, caption ->
                            viewingPhotoFile = file
                            viewingPhotoCaption = caption
                        }
                    )
                    4 -> TabPayment(
                        tx = tx,
                        viewModel = viewModel,
                        onShare = { shareWhatsAppReceipt() }
                    )
                }
            }
        }
    }

    // Return process dialog
    if (showReturnDialog) {
        ReturnProcessDialog(
            transaction = tx,
            viewModel = viewModel,
            onDismiss = { showReturnDialog = false },
            onSuccess = {
                showReturnDialog = false
                selectedTabIndex = 3 // Jump to Return tab
            }
        )
    }

    // Delete Entire Transaction confirmation dialog
    if (showDeleteTxConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteTxConfirm = false },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Hapus Transaksi Sewa?") },
            text = {
                Text("Semua data transaksi, termasuk seluruh foto kondisi barang dan dokumen identitas penyewa akan dihapus permanen dari perangkat.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteTxConfirm = false
                        viewModel.deleteTransaction(tx) {
                            Toast.makeText(context, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Transaksi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTxConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Clear Customer Identity Photos confirmation dialog (Privasi)
    if (showClearIdentityPhotosConfirm) {
        AlertDialog(
            onDismissRequest = { showClearIdentityPhotosConfirm = false },
            icon = {
                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text("Hapus Semua Dokumen Identitas?") },
            text = {
                Text("Fitur ini dirancang untuk menjaga privasi pelanggan. Foto KTP/SIM akan dihapus permanen dari penyimpanan perangkat, namun riwayat transaksi sewa tetap tersimpan rapi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearIdentityPhotosConfirm = false
                        viewModel.clearCustomerIdentityPhotos(tx.id) {
                            Toast.makeText(context, "Dokumen identitas telah dibersihkan", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Bersihkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearIdentityPhotosConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add Extra Photo Bottom Sheet
    if (showAddPhotoSheet) {
        PhotoPickerModalSheet(
            title = "Tambah Foto: ${extraPhotoType.displayName}",
            onDismiss = { showAddPhotoSheet = false },
            onPhotoCaptured = { uri ->
                coroutineScope.launch {
                    val savedPath = PhotoStorageManager.saveImageFromUri(
                        context = context,
                        sourceUri = uri,
                        isIdentityDoc = false,
                        prefix = "extra"
                    )
                    if (savedPath != null) {
                        viewModel.addPhotoToTransaction(
                            TransactionPhoto(
                                transactionId = tx.id,
                                photoType = extraPhotoType.name,
                                filePath = savedPath,
                                caption = extraPhotoType.displayName
                            )
                        )
                    }
                }
            }
        )
    }

    // Full Screen Zoomable Lightbox
    if (viewingPhotoFile != null) {
        ImageViewerDialog(
            photoFile = viewingPhotoFile,
            caption = viewingPhotoCaption,
            onDismiss = {
                viewingPhotoFile = null
                viewingPhotoCaption = null
            }
        )
    }
}

// TAB 1: RINGKASAN
@Composable
fun TabSummary(
    tx: RentalTransaction,
    photos: List<TransactionPhoto>,
    onOpenReturn: () -> Unit,
    onShare: () -> Unit,
    onViewPhoto: (File, String) -> Unit
) {
    val context = LocalContext.current
    val isCompleted = tx.transactionStatus == "SELESAI"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCompleted) Color(0xFFD1FAE5) else Color(0xFFEFF6FF)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "STATUS: SELESAI / DIKEMBALIKAN" else "STATUS: SEDANG DISEWA",
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (tx.paymentStatus == "LUNAS") EmeraldSuccess else Color(0xFFD97706)
                    ) {
                        Text(
                            text = tx.paymentStatus,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tx.itemName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (tx.itemSerialNumber.isNotBlank()) {
                    Text(
                        text = "Serial Number: ${tx.itemSerialNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isCompleted) {
                        Button(
                            onClick = onOpenReturn,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Proses Kembali")
                        }
                    }

                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Nota WA")
                    }
                }
            }
        }

        // Section: Info Penyewa
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Informasi Penyewa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nama: ${tx.customerName}",
                    fontWeight = FontWeight.Medium
                )
                if (tx.customerPhone.isNotBlank()) {
                    Text(text = "WhatsApp: ${tx.customerPhone}")
                }
                if (tx.identityNumber.isNotBlank()) {
                    MaskedIdentityRow(
                        identityType = tx.identityType,
                        identityNumber = tx.identityNumber
                    )
                }
            }
        }

        // Section: Jadwal & Durasi
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Jadwal & Durasi Sewa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Mulai Sewa:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = PrivacyUtils.formatDate(tx.rentalStartDate), fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Jatuh Tempo:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = PrivacyUtils.formatDate(tx.rentalDueDate), fontWeight = FontWeight.SemiBold)
                }
                if (tx.rentalActualReturnDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tanggal Kembali Aktual:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = PrivacyUtils.formatDateTime(tx.rentalActualReturnDate),
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldSuccess
                        )
                    }
                }
            }
        }

        // Preview Foto Dokumentasi
        val previewPhotos = photos.take(4)
        if (previewPhotos.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dokumentasi Foto (${photos.size} Foto Tersimpan):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    previewPhotos.forEach { photo ->
                        val file = PhotoStorageManager.getFileFromRelativePath(context, photo.filePath)
                        if (file != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onViewPhoto(file, photo.caption) }
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(file).crossfade(true).build(),
                                    contentDescription = photo.caption,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 2: IDENTITAS PENYEWA (E. Keamanan dan Privasi)
@Composable
fun TabCustomerIdentity(
    tx: RentalTransaction,
    viewModel: RentalViewModel,
    onClearAllIdentity: () -> Unit,
    onViewPhoto: (File, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hasAnyPhoto = tx.identityFrontPhotoPath != null ||
            tx.identityBackPhotoPath != null ||
            tx.customerWithItemPhotoPath != null

    var showPickerForSlot by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Privacy Notice Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Data Privasi Pelanggan Terlindungi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Foto identitas (KTP/SIM) disimpan di penyimpanan internal perangkat dan terlindung dari akses publik.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Identity Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Data Identitas Pelanggan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jenis Identitas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tx.identityType, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nama Sesuai Identitas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(tx.identityName.ifBlank { tx.customerName }, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nomor Identitas:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    MaskedIdentityRow(
                        identityType = tx.identityType,
                        identityNumber = tx.identityNumber
                    )
                }
            }
        }

        // Sensitive Photos
        Text(
            text = "Dokumen Foto Identitas:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Front Photo
        SensitiveIdentityDocumentBox(
            title = "Foto Identitas Bagian Depan (${tx.identityType})",
            photoPath = tx.identityFrontPhotoPath,
            onAddOrReplace = { showPickerForSlot = "FRONT" },
            onDelete = {
                val updated = tx.copy(identityFrontPhotoPath = null)
                viewModel.updateTransaction(updated)
                PhotoStorageManager.deleteFile(context, tx.identityFrontPhotoPath)
            }
        )

        // Back Photo
        SensitiveIdentityDocumentBox(
            title = "Foto Identitas Bagian Belakang",
            photoPath = tx.identityBackPhotoPath,
            isOptional = true,
            onAddOrReplace = { showPickerForSlot = "BACK" },
            onDelete = {
                val updated = tx.copy(identityBackPhotoPath = null)
                viewModel.updateTransaction(updated)
                PhotoStorageManager.deleteFile(context, tx.identityBackPhotoPath)
            }
        )

        // Customer with item photo
        SensitiveIdentityDocumentBox(
            title = "Foto Pelanggan Bersama Barang",
            photoPath = tx.customerWithItemPhotoPath,
            isOptional = true,
            onAddOrReplace = { showPickerForSlot = "SELFIE" },
            onDelete = {
                val updated = tx.copy(customerWithItemPhotoPath = null)
                viewModel.updateTransaction(updated)
                PhotoStorageManager.deleteFile(context, tx.customerWithItemPhotoPath)
            }
        )

        // Action: Purge / Delete all customer ID photos
        if (hasAnyPhoto) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onClearAllIdentity,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purge_customer_id_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hapus Semua Dokumen Identitas (Privasi)")
            }
        }
    }

    if (showPickerForSlot != null) {
        val slot = showPickerForSlot!!
        PhotoPickerModalSheet(
            title = "Upload Foto Dokumen",
            onDismiss = { showPickerForSlot = null },
            onPhotoCaptured = { uri ->
                coroutineScope.launch {
                    val saved = PhotoStorageManager.saveImageFromUri(
                        context = context,
                        sourceUri = uri,
                        isIdentityDoc = true,
                        prefix = "id_${slot.lowercase()}"
                    )
                    if (saved != null) {
                        val updated = when (slot) {
                            "FRONT" -> tx.copy(identityFrontPhotoPath = saved)
                            "BACK" -> tx.copy(identityBackPhotoPath = saved)
                            else -> tx.copy(customerWithItemPhotoPath = saved)
                        }
                        viewModel.updateTransaction(updated)
                    }
                }
            }
        )
    }
}

// TAB 3: FOTO BARANG (A. Foto Barang Sewaan)
@Composable
fun TabItemPhotos(
    tx: RentalTransaction,
    photos: List<TransactionPhoto>,
    onAddPhoto: (PhotoType) -> Unit,
    onDeletePhoto: (TransactionPhoto) -> Unit,
    onViewPhoto: (File, String) -> Unit
) {
    val context = LocalContext.current
    val beforePhotos = photos.filter { it.photoType == PhotoType.BEFORE_HANDOVER.name }
    val completenessPhotos = photos.filter { it.photoType == PhotoType.COMPLETENESS.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Section: Kondisi Sebelum Diserahkan
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "1. Kondisi Sebelum Diserahkan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catatan: ${tx.itemConditionBefore}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { onAddPhoto(PhotoType.BEFORE_HANDOVER) },
                        modifier = Modifier.testTag("add_before_photo_btn")
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }

                if (beforePhotos.isEmpty()) {
                    Text(
                        text = "Belum ada foto kondisi awal yang disimpan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        beforePhotos.forEach { photo ->
                            val file = PhotoStorageManager.getFileFromRelativePath(context, photo.filePath)
                            PhotoThumbnailCard(
                                photo = photo,
                                onView = { if (file != null) onViewPhoto(file, photo.caption) },
                                onDelete = { onDeletePhoto(photo) }
                            )
                        }
                    }
                }
            }
        }

        // Section: Kelengkapan Barang
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Kelengkapan / Aksesoris",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FilledTonalButton(
                        onClick = { onAddPhoto(PhotoType.COMPLETENESS) },
                        modifier = Modifier.testTag("add_completeness_photo_btn")
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", fontSize = 12.sp)
                    }
                }

                if (completenessPhotos.isEmpty()) {
                    Text(
                        text = "Belum ada foto kelengkapan barang yang disimpan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        completenessPhotos.forEach { photo ->
                            val file = PhotoStorageManager.getFileFromRelativePath(context, photo.filePath)
                            PhotoThumbnailCard(
                                photo = photo,
                                onView = { if (file != null) onViewPhoto(file, photo.caption) },
                                onDelete = { onDeletePhoto(photo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// TAB 4: PENGEMBALIAN & PERBANDINGAN AWAL VS AKHIR (C. Dokumentasi Pengembalian)
@Composable
fun TabReturnDocumentation(
    tx: RentalTransaction,
    photos: List<TransactionPhoto>,
    onOpenReturn: () -> Unit,
    onViewPhoto: (File, String) -> Unit
) {
    val context = LocalContext.current
    val isCompleted = tx.transactionStatus == "SELESAI"
    val returnPhotos = photos.filter { it.photoType == PhotoType.UPON_RETURN.name }
    val beforePhotos = photos.filter { it.photoType == PhotoType.BEFORE_HANDOVER.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isCompleted) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Barang Masih Sedang Disewa",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tekan tombol di bawah untuk mendokumentasikan pengembalian barang, upload foto kondisi kembali, dan menghitung biaya perbaikan jika ada kerusakan.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onOpenReturn,
                        modifier = Modifier.testTag("start_return_flow_btn")
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mulai Proses Pengembalian")
                    }
                }
            }
        } else {
            // Return Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Catatan Pengembalian Barang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tanggal Pengembalian:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = PrivacyUtils.formatDateTime(tx.rentalActualReturnDate),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider()

                    Text(
                        text = "Kondisi Saat Kembali:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tx.returnConditionNotes ?: "Baik dan lengkap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (!tx.returnDamageNotes.isNullOrBlank()) {
                        Divider()
                        Text(
                            text = "Catatan Kerusakan / Kehilangan:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = tx.returnDamageNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (tx.repairFee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Biaya Perbaikan / Denda:", color = MaterialTheme.colorScheme.error)
                            Text(
                                text = PrivacyUtils.formatCurrency(tx.repairFee),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // PERBANDINGAN KONDISI AWAL VS AKHIR (Side-by-side comparison!)
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
                            imageVector = Icons.Default.Compare,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Perbandingan Kondisi Awal vs Akhir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left: Awal
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sebelum Sewa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            val firstBefore = beforePhotos.firstOrNull()
                            if (firstBefore != null) {
                                val file = PhotoStorageManager.getFileFromRelativePath(context, firstBefore.filePath)
                                if (file != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(file).crossfade(true).build(),
                                        contentDescription = "Sebelum",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onViewPhoto(file, "Kondisi Sebelum Diserahkan") }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada foto awal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tx.itemConditionBefore,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )
                        }

                        // Right: Saat Kembali
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (!tx.returnDamageNotes.isNullOrBlank()) Color(0xFFFFE4E6) else Color(0xFFD1FAE5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Saat Kembali",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!tx.returnDamageNotes.isNullOrBlank()) Color(0xFF9F1239) else Color(0xFF065F46),
                                    modifier = Modifier.padding(4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            val firstReturn = returnPhotos.firstOrNull()
                            if (firstReturn != null) {
                                val file = PhotoStorageManager.getFileFromRelativePath(context, firstReturn.filePath)
                                if (file != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(file).crossfade(true).build(),
                                        contentDescription = "Kembali",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onViewPhoto(file, "Kondisi Saat Dikembalikan") }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada foto kembali", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tx.returnConditionNotes ?: "Baik",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            // Return Photos List
            if (returnPhotos.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Galeri Foto Pengembalian (${returnPhotos.size} Foto)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            returnPhotos.forEach { photo ->
                                val file = PhotoStorageManager.getFileFromRelativePath(context, photo.filePath)
                                PhotoThumbnailCard(
                                    photo = photo,
                                    onView = { if (file != null) onViewPhoto(file, photo.caption) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TAB 5: PEMBAYARAN & NOTA WHATSAPP
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabPayment(
    tx: RentalTransaction,
    viewModel: RentalViewModel,
    onShare: () -> Unit
) {
    var paymentStatusExpanded by remember { mutableStateOf(false) }
    val paymentStatuses = listOf("LUNAS", "DP", "BELUM_LUNAS")
    val grandTotal = tx.totalRentalFee + tx.repairFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rincian Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tarif Sewa (${tx.dailyRate.toInt()} / hari):")
                    Text(PrivacyUtils.formatCurrency(tx.totalRentalFee), fontWeight = FontWeight.SemiBold)
                }

                if (tx.depositAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jaminan / Uang Deposit:")
                        Text(PrivacyUtils.formatCurrency(tx.depositAmount), fontWeight = FontWeight.SemiBold)
                    }
                }

                if (tx.repairFee > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Denda Kerusakan / Kehilangan:")
                        Text(
                            PrivacyUtils.formatCurrency(tx.repairFee),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Pembayaran:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = PrivacyUtils.formatCurrency(grandTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Payment Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = paymentStatusExpanded,
                    onExpandedChange = { paymentStatusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = tx.paymentStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status Pembayaran") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentStatusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = paymentStatusExpanded,
                        onDismissRequest = { paymentStatusExpanded = false }
                    ) {
                        paymentStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    val updated = tx.copy(paymentStatus = status)
                                    viewModel.updateTransaction(updated)
                                    paymentStatusExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // WhatsApp Share Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bagikan Bukti Sewa (WhatsApp)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Text(
                    text = "Nota transaksi diformat secara bersih dan rapi. Sesuai standar keamanan data pribadi, foto identitas/KTP dan nomor NIK tidak akan disertakan dalam pesan nota yang dibagikan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_whatsapp_receipt_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan Nota WhatsApp")
                }
            }
        }
    }
}
