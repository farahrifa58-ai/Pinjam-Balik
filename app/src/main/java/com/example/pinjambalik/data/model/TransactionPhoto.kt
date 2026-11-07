package com.example.pinjambalik.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_photos",
    foreignKeys = [
        ForeignKey(
            entity = RentalTransaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"])]
)
data class TransactionPhoto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val photoType: String, // BEFORE_HANDOVER, COMPLETENESS, UPON_RETURN
    val filePath: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
