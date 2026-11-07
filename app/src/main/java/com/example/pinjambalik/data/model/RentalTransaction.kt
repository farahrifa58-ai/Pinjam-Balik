package com.example.pinjambalik.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rental_transactions")
data class RentalTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceCode: String = "",
    val itemName: String = "",
    val itemCategory: String = "Elektronik",
    val itemSerialNumber: String = "",
    val itemConditionBefore: String = "Baik / Normal",
    
    // Identitas Pelanggan
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val identityType: String = IdentityType.KTP.name,
    val identityName: String = "",
    val identityNumber: String = "",
    val identityFrontPhotoPath: String? = null,
    val identityBackPhotoPath: String? = null,
    val customerWithItemPhotoPath: String? = null,
    
    // Jadwal & Biaya Sewa
    val rentalStartDate: Long = System.currentTimeMillis(),
    val rentalDueDate: Long = System.currentTimeMillis() + (24 * 3600 * 1000), // Default 1 day
    val rentalActualReturnDate: Long? = null,
    val dailyRate: Double = 0.0,
    val totalRentalFee: Double = 0.0,
    val depositAmount: Double = 0.0,
    val paymentStatus: String = "LUNAS", // LUNAS, DP, BELUM_LUNAS
    val transactionStatus: String = "DISEWA", // DISEWA, SELESAI
    
    // Dokumentasi Pengembalian
    val returnConditionNotes: String? = null,
    val returnDamageNotes: String? = null,
    val repairFee: Double = 0.0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val durationDays: Int
        get() = (((rentalDueDate - rentalStartDate) / (24 * 3600 * 1000L)).toInt()).coerceAtLeast(1)
}
