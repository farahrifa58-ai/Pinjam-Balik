package com.example.pinjambalik.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pinjambalik.data.model.IdentityType
import com.example.pinjambalik.data.model.PhotoType
import com.example.pinjambalik.data.model.RentalTransaction
import com.example.pinjambalik.data.model.TransactionPhoto
import com.example.pinjambalik.ui.components.PhotoPickerModalSheet
import com.example.pinjambalik.ui.components.SensitiveIdentityDocumentBox
import com.example.pinjambalik.util.PhotoStorageManager
import com.example.pinjambalik.util.PrivacyUtils
import com.example.pinjambalik.viewmodel.RentalViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PendingPhoto(
    val id: String = UUID.randomUUID().toString(),
    var filePath: String,
    var photoType: PhotoType,
    var caption: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: RentalViewModel,
    onNavigateBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form Fields - Rental Item
    var itemName by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Elektronik / Kamera") }
    var itemSerialNumber by remember { mutableStateOf("") }
    var itemConditionBefore by remember { mutableStateOf("Normal / Mulus tanpa cacat") }

    // Form Fields - Schedule & Rates
    val now = System.currentTimeMillis()
    var startDate by remember { mutableLongStateOf(now) }
    var dueDate by remember { mutableLongStateOf(now + (24 * 3600 * 1000L)) }
    var dailyRateText by remember { mutableStateOf("100000") }
    var depositAmountText by remember { mutableStateOf("0") }
    var paymentStatus by remember { mutableStateOf("LUNAS") }

    // Form Fields - Customer Identity
    var identityType by remember { mutableStateOf(IdentityType.KTP.name) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var identityName by remember { mutableStateOf("") }
    var identityNumber by remember { mutableStateOf("") }

    // Identity photos
    var frontPhotoPath by remember { mutableStateOf<String?>(null) }
    var backPhotoPath by remember { mutableStateOf<String?>(null) }
    var customerWithItemPhotoPath by remember { mutableStateOf<String?>(null) }

    // Item documentation photos list
    val itemPhotos = remember { mutableStateListOf<PendingPhoto>() }

    // Photo picker modal sheet controls
    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var pickerTargetMode by remember { mutableStateOf("ITEM_PHOTO") } // ITEM_PHOTO, ID_FRONT, ID_BACK, ID_SELFIE
    var currentItemPhotoType by remember { mutableStateOf(PhotoType.BEFORE_HANDOVER) }
    var editingPhotoIndex by remember { mutableIntStateOf(-1) }

    // Date Pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    // Category dropdown
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "Elektronik / Kamera",
        "Gaming & Konsol",
        "Camping & Outdoor",
        "Peralatan Pesta / Event",
        "Kendaraan",
        "Alat Musik",
        "Lainnya"
    )

    // Calculate duration in days
    val diffDays = ((dueDate - startDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1L)
    val dailyRate = dailyRateText.toDoubleOrNull() ?: 0.0
    val totalRentalFee = dailyRate * diffDays
    val depositAmount = depositAmountText.toDoubleOrNull() ?: 0.0

    // Invoice code generation
    val dateCode = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date(now))
    val randomCode = remember { UUID.randomUUID().toString().take(4).uppercase() }
    val invoiceCode = remember { "PB-$dateCode-$randomCode" }

    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Form Sewa Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = invoiceCode,
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
                    Button(
                        onClick = {
                            if (itemName.isBlank()) {
                                Toast.makeText(context, "Nama barang sewaan wajib diisi", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (customerName.isBlank()) {
                                Toast.makeText(context, "Nama penyewa wajib diisi", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            val newTx = RentalTransaction(
                                invoiceCode = invoiceCode,
                                itemName = itemName.trim(),
                                itemCategory = itemCategory,
                                itemSerialNumber = itemSerialNumber.trim(),
                                itemConditionBefore = itemConditionBefore.trim(),
                                customerName = customerName.trim(),
                                customerPhone = customerPhone.trim(),
                                customerAddress = customerAddress.trim(),
                                identityType = identityType,
                                identityName = if (identityName.isNotBlank()) identityName.trim() else customerName.trim(),
                                identityNumber = identityNumber.trim(),
                                identityFrontPhotoPath = frontPhotoPath,
                                identityBackPhotoPath = backPhotoPath,
                                customerWithItemPhotoPath = customerWithItemPhotoPath,
                                rentalStartDate = startDate,
                                rentalDueDate = dueDate,
                                dailyRate = dailyRate,
                                totalRentalFee = totalRentalFee,
                                depositAmount = depositAmount,
                                paymentStatus = paymentStatus,
                                transactionStatus = "DISEWA"
                            )

                            val photosToSave = itemPhotos.map { pending ->
                                TransactionPhoto(
                                    transactionId = 0,
                                    photoType = pending.photoType.name,
                                    filePath = pending.filePath,
                                    caption = pending.caption
                                )
                            }

                            viewModel.saveTransaction(newTx, photosToSave) { createdId ->
                                isSaving = false
                                Toast.makeText(context, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                onSaved(createdId)
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_transaction_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan")
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // SECTION 1: Informasi Barang
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Informasi Barang Sewa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: Sony Alpha A7 III + Lensa 28-70mm") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_name_input"),
                        singleLine = true
                    )

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = itemCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori Barang") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        itemCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = itemSerialNumber,
                        onValueChange = { itemSerialNumber = it },
                        label = { Text("Nomor Seri / SN (Opsional)") },
                        placeholder = { Text("Contoh: S/N: 483920194") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = itemConditionBefore,
                        onValueChange = { itemConditionBefore = it },
                        label = { Text("Catatan Kondisi Awal Barang") },
                        placeholder = { Text("Mulus, fungsi tombol 100%, body ada lecet halus di bawah") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // SECTION 2: A. FOTO BARANG SEWAAN (Dokumentasi Barang)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "A. Dokumentasi Barang",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${itemPhotos.size} Foto",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Dokumentasikan kondisi fisik barang sebelum diserahkan dan kelengkapan aksesorisnya. Anda dapat mengambil dari kamera HP atau galeri.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Add Buttons for specific categories
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                pickerTargetMode = "ITEM_PHOTO"
                                currentItemPhotoType = PhotoType.BEFORE_HANDOVER
                                editingPhotoIndex = -1
                                showPhotoPickerSheet = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_condition_photo_btn")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Kondisi", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                pickerTargetMode = "ITEM_PHOTO"
                                currentItemPhotoType = PhotoType.COMPLETENESS
                                editingPhotoIndex = -1
                                showPhotoPickerSheet = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_completeness_photo_btn")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Kelengkapan", fontSize = 12.sp)
                        }
                    }

                    // Display list of added photos
                    if (itemPhotos.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Belum ada foto barang yang ditambahkan. Disarankan mengambil minimal 1 foto fisik barang & 1 foto kelengkapan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            itemPhotos.forEachIndexed { index, pending ->
                                val file = PhotoStorageManager.getFileFromRelativePath(context, pending.filePath)
                                var showEditCaption by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (file != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(file)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = pending.caption,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (pending.photoType == PhotoType.BEFORE_HANDOVER)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = pending.photoType.displayName,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (pending.photoType == PhotoType.BEFORE_HANDOVER)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = if (pending.caption.isNotBlank()) pending.caption else "(Tanpa keterangan)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        // Edit caption button
                                        IconButton(onClick = { showEditCaption = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Keterangan",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Delete photo button
                                        IconButton(onClick = {
                                            PhotoStorageManager.deleteFile(context, pending.filePath)
                                            itemPhotos.removeAt(index)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (showEditCaption) {
                                    var tempCaption by remember { mutableStateOf(pending.caption) }
                                    AlertDialog(
                                        onDismissRequest = { showEditCaption = false },
                                        title = { Text("Keterangan Foto") },
                                        text = {
                                            OutlinedTextField(
                                                value = tempCaption,
                                                onValueChange = { tempCaption = it },
                                                label = { Text("Keterangan Foto") },
                                                placeholder = { Text("Misal: Tampak samping kiri, kabel charger utuh") },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        confirmButton = {
                                            Button(onClick = {
                                                itemPhotos[index] = pending.copy(caption = tempCaption)
                                                showEditCaption = false
                                            }) {
                                                Text("Simpan")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showEditCaption = false }) {
                                                Text("Batal")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: B. IDENTITAS PELANGGAN
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "B. Identitas Penyewa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Data identitas penyewa disimpan aman di perangkat. Dokumen identitas bersifat fleksibel dan tidak wajib diisi semua.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Identity Type Tabs / Selector
                    val identityTypes = listOf(
                        IdentityType.KTP to "KTP",
                        IdentityType.SIM to "SIM",
                        IdentityType.PASPOR to "Paspor",
                        IdentityType.OTHER to "Lainnya"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(identityTypes) { (type, label) ->
                            val selected = identityType == type.name
                            FilterChip(
                                selected = selected,
                                onClick = { identityType = type.name },
                                label = { Text(label) },
                                modifier = Modifier.testTag("id_type_${type.name}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = {
                            customerName = it
                            if (identityName.isBlank()) identityName = it
                        },
                        label = { Text("Nama Lengkap Penyewa *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Nomor Telepon / WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = identityNumber,
                        onValueChange = { identityNumber = it },
                        label = { Text("Nomor Identitas (NIK/No. SIM) - Opsional") },
                        placeholder = { Text("3201xxxxxxxxxxxx") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("identity_number_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Alamat Penyewa (Opsional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload Foto Dokumen Identitas:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Front photo
                    SensitiveIdentityDocumentBox(
                        title = "Foto Identitas Bagian Depan",
                        photoPath = frontPhotoPath,
                        isOptional = true,
                        onAddOrReplace = {
                            pickerTargetMode = "ID_FRONT"
                            showPhotoPickerSheet = true
                        },
                        onDelete = {
                            PhotoStorageManager.deleteFile(context, frontPhotoPath)
                            frontPhotoPath = null
                        }
                    )

                    // 2. Back photo
                    SensitiveIdentityDocumentBox(
                        title = "Foto Identitas Bagian Belakang",
                        photoPath = backPhotoPath,
                        isOptional = true,
                        onAddOrReplace = {
                            pickerTargetMode = "ID_BACK"
                            showPhotoPickerSheet = true
                        },
                        onDelete = {
                            PhotoStorageManager.deleteFile(context, backPhotoPath)
                            backPhotoPath = null
                        }
                    )

                    // 3. Customer with item photo
                    SensitiveIdentityDocumentBox(
                        title = "Foto Pelanggan Bersama Barang",
                        photoPath = customerWithItemPhotoPath,
                        isOptional = true,
                        onAddOrReplace = {
                            pickerTargetMode = "ID_SELFIE"
                            showPhotoPickerSheet = true
                        },
                        onDelete = {
                            PhotoStorageManager.deleteFile(context, customerWithItemPhotoPath)
                            customerWithItemPhotoPath = null
                        }
                    )
                }
            }

            // SECTION 4: JADWAL & BIAYA SEWA
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Periode & Biaya Sewa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = PrivacyUtils.formatDate(startDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mulai Sewa") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showStartDatePicker = true },
                            trailingIcon = {
                                IconButton(onClick = { showStartDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = PrivacyUtils.formatDate(dueDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jatuh Tempo") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDueDatePicker = true },
                            trailingIcon = {
                                IconButton(onClick = { showDueDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dailyRateText,
                            onValueChange = { dailyRateText = it },
                            label = { Text("Tarif per Hari (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = depositAmountText,
                            onValueChange = { depositAmountText = it },
                            label = { Text("Jaminan / DP (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Total preview card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Durasi: $diffDays Hari",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Total Biaya Sewa:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = PrivacyUtils.formatCurrency(totalRentalFee),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Photo picker bottom sheet
    if (showPhotoPickerSheet) {
        val sheetTitle = when (pickerTargetMode) {
            "ID_FRONT" -> "Foto Identitas Bagian Depan"
            "ID_BACK" -> "Foto Identitas Bagian Belakang"
            "ID_SELFIE" -> "Foto Pelanggan Bersama Barang"
            else -> "Foto Barang: ${currentItemPhotoType.displayName}"
        }

        PhotoPickerModalSheet(
            title = sheetTitle,
            onDismiss = { showPhotoPickerSheet = false },
            onPhotoCaptured = { uri ->
                coroutineScope.launch {
                    val isId = pickerTargetMode.startsWith("ID_")
                    val savedPath = PhotoStorageManager.saveImageFromUri(
                        context = context,
                        sourceUri = uri,
                        isIdentityDoc = isId,
                        prefix = if (isId) "id" else "item"
                    )

                    if (savedPath != null) {
                        when (pickerTargetMode) {
                            "ID_FRONT" -> frontPhotoPath = savedPath
                            "ID_BACK" -> backPhotoPath = savedPath
                            "ID_SELFIE" -> customerWithItemPhotoPath = savedPath
                            "ITEM_PHOTO" -> {
                                itemPhotos.add(
                                    PendingPhoto(
                                        filePath = savedPath,
                                        photoType = currentItemPhotoType,
                                        caption = if (currentItemPhotoType == PhotoType.BEFORE_HANDOVER)
                                            "Kondisi sebelum diserahkan"
                                        else
                                            "Kelengkapan barang"
                                    )
                                )
                            }
                        }
                    } else {
                        Toast.makeText(context, "Gagal memproses foto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Start Date Picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = it
                        if (dueDate <= it) {
                            dueDate = it + (24 * 3600 * 1000L)
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Due Date Picker
    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = it
                    }
                    showDueDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
