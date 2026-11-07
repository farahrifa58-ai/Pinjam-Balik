package com.example.pinjambalik.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pinjambalik.data.model.PhotoType
import com.example.pinjambalik.data.model.RentalTransaction
import com.example.pinjambalik.data.model.TransactionPhoto
import com.example.pinjambalik.ui.components.PhotoPickerModalSheet
import com.example.pinjambalik.util.PhotoStorageManager
import com.example.pinjambalik.viewmodel.RentalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnProcessDialog(
    transaction: RentalTransaction,
    viewModel: RentalViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var returnConditionNotes by remember { mutableStateOf("Barang kembali lengkap dan dalam kondisi baik.") }
    var returnDamageNotes by remember { mutableStateOf("") }
    var repairFeeText by remember { mutableStateOf("0") }
    val returnPhotos = remember { mutableStateListOf<PendingPhoto>() }

    var showPhotoPicker by remember { mutableStateOf(false) }
    var currentPickerLabel by remember { mutableStateOf("Kondisi Saat Kembali") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Proses Pengembalian Barang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transaction.itemName} (${transaction.invoiceCode})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Catatan kondisi barang saat dikembalikan
            OutlinedTextField(
                value = returnConditionNotes,
                onValueChange = { returnConditionNotes = it },
                label = { Text("Catatan Kondisi Barang Saat Dikembalikan *") },
                placeholder = { Text("Fungsi normal, kelengkapan utuh...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("return_condition_input"),
                minLines = 2
            )

            // Catatan kerusakan atau kehilangan
            OutlinedTextField(
                value = returnDamageNotes,
                onValueChange = { returnDamageNotes = it },
                label = { Text("Catatan Kerusakan atau Kehilangan (Jika Ada)") },
                placeholder = { Text("Misal: Tutup lensa hilang, lecet di bodi...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("return_damage_input"),
                minLines = 2
            )

            // Biaya perbaikan
            OutlinedTextField(
                value = repairFeeText,
                onValueChange = { repairFeeText = it },
                label = { Text("Biaya Perbaikan / Ganti Rugi (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("repair_fee_input")
            )

            // Upload return photos section
            Text(
                text = "Foto Dokumentasi Pengembalian:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        currentPickerLabel = "Foto Kondisi Barang Saat Kembali"
                        showPhotoPicker = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Foto Kondisi", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        currentPickerLabel = "Foto Kelengkapan Saat Kembali"
                        showPhotoPicker = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Kelengkapan", fontSize = 12.sp)
                }
            }

            // Display added return photos
            if (returnPhotos.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    returnPhotos.forEachIndexed { index, photo ->
                        val file = PhotoStorageManager.getFileFromRelativePath(context, photo.filePath)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (file != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(file)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = photo.caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = photo.caption,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = {
                                    PhotoStorageManager.deleteFile(context, photo.filePath)
                                    returnPhotos.removeAt(index)
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = {
                        val repairFee = repairFeeText.toDoubleOrNull() ?: 0.0
                        val photos = returnPhotos.map { pending ->
                            TransactionPhoto(
                                transactionId = transaction.id,
                                photoType = PhotoType.UPON_RETURN.name,
                                filePath = pending.filePath,
                                caption = pending.caption
                            )
                        }

                        viewModel.completeReturn(
                            transactionId = transaction.id,
                            returnCondition = returnConditionNotes.trim(),
                            damageNotes = returnDamageNotes.trim().ifBlank { null },
                            repairFee = repairFee,
                            returnPhotos = photos
                        ) {
                            Toast.makeText(context, "Pengembalian berhasil dicatat!", Toast.LENGTH_SHORT).show()
                            onSuccess()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("confirm_return_btn")
                ) {
                    Text("Konfirmasi Pengembalian")
                }
            }
        }
    }

    if (showPhotoPicker) {
        PhotoPickerModalSheet(
            title = currentPickerLabel,
            onDismiss = { showPhotoPicker = false },
            onPhotoCaptured = { uri ->
                coroutineScope.launch {
                    val savedPath = PhotoStorageManager.saveImageFromUri(
                        context = context,
                        sourceUri = uri,
                        isIdentityDoc = false,
                        prefix = "return"
                    )
                    if (savedPath != null) {
                        returnPhotos.add(
                            PendingPhoto(
                                filePath = savedPath,
                                photoType = PhotoType.UPON_RETURN,
                                caption = currentPickerLabel
                            )
                        )
                    }
                }
            }
        )
    }
}
