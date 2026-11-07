package com.example.pinjambalik.data.repository

import android.content.Context
import com.example.pinjambalik.data.local.PhotoDao
import com.example.pinjambalik.data.local.TransactionDao
import com.example.pinjambalik.data.model.PhotoType
import com.example.pinjambalik.data.model.RentalTransaction
import com.example.pinjambalik.data.model.TransactionPhoto
import com.example.pinjambalik.util.PhotoStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RentalRepository(
    private val context: Context,
    private val transactionDao: TransactionDao,
    private val photoDao: PhotoDao
) {

    val allTransactions: Flow<List<RentalTransaction>> = transactionDao.getAllTransactions()

    fun getTransaction(id: Long): Flow<RentalTransaction?> = transactionDao.getTransactionById(id)

    fun getPhotos(transactionId: Long): Flow<List<TransactionPhoto>> =
        photoDao.getPhotosForTransaction(transactionId)

    suspend fun saveNewTransaction(
        transaction: RentalTransaction,
        photos: List<TransactionPhoto>
    ): Long = withContext(Dispatchers.IO) {
        val newId = transactionDao.insertTransaction(transaction)
        val linkedPhotos = photos.map { it.copy(transactionId = newId) }
        if (linkedPhotos.isNotEmpty()) {
            photoDao.insertPhotos(linkedPhotos)
        }
        newId
    }

    suspend fun updateTransaction(transaction: RentalTransaction) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun addPhoto(photo: TransactionPhoto) = withContext(Dispatchers.IO) {
        photoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: TransactionPhoto) = withContext(Dispatchers.IO) {
        PhotoStorageManager.deleteFile(context, photo.filePath)
        photoDao.deletePhoto(photo)
    }

    suspend fun completeReturn(
        transactionId: Long,
        returnCondition: String,
        damageNotes: String?,
        repairFee: Double,
        returnPhotos: List<TransactionPhoto>
    ) = withContext(Dispatchers.IO) {
        val existing = transactionDao.getTransactionByIdOnce(transactionId) ?: return@withContext
        val updated = existing.copy(
            transactionStatus = "SELESAI",
            rentalActualReturnDate = System.currentTimeMillis(),
            returnConditionNotes = returnCondition,
            returnDamageNotes = damageNotes,
            repairFee = repairFee,
            updatedAt = System.currentTimeMillis()
        )
        transactionDao.updateTransaction(updated)

        val linkedPhotos = returnPhotos.map {
            it.copy(
                transactionId = transactionId,
                photoType = PhotoType.UPON_RETURN.name
            )
        }
        if (linkedPhotos.isNotEmpty()) {
            photoDao.insertPhotos(linkedPhotos)
        }
    }

    suspend fun clearIdentityPhotos(transactionId: Long) = withContext(Dispatchers.IO) {
        val existing = transactionDao.getTransactionByIdOnce(transactionId) ?: return@withContext
        PhotoStorageManager.deleteFile(context, existing.identityFrontPhotoPath)
        PhotoStorageManager.deleteFile(context, existing.identityBackPhotoPath)
        PhotoStorageManager.deleteFile(context, existing.customerWithItemPhotoPath)
        transactionDao.clearIdentityPhotos(transactionId)
    }

    suspend fun deleteTransaction(transaction: RentalTransaction) = withContext(Dispatchers.IO) {
        // Delete identity photos
        PhotoStorageManager.deleteFile(context, transaction.identityFrontPhotoPath)
        PhotoStorageManager.deleteFile(context, transaction.identityBackPhotoPath)
        PhotoStorageManager.deleteFile(context, transaction.customerWithItemPhotoPath)

        // Delete all item photos
        val photos = photoDao.getPhotosForTransactionOnce(transaction.id)
        for (photo in photos) {
            PhotoStorageManager.deleteFile(context, photo.filePath)
        }
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun purgeExpiredIdentityDocuments(retentionDays: Long): Int = withContext(Dispatchers.IO) {
        if (retentionDays <= 0) return@withContext 0 // 0 means keep forever
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 3600 * 1000L)
        val expiredTransactions = transactionDao.getExpiredIdentityTransactions(cutoff)
        var purgedCount = 0

        for (tx in expiredTransactions) {
            PhotoStorageManager.deleteFile(context, tx.identityFrontPhotoPath)
            PhotoStorageManager.deleteFile(context, tx.identityBackPhotoPath)
            PhotoStorageManager.deleteFile(context, tx.customerWithItemPhotoPath)
            transactionDao.clearIdentityPhotos(tx.id)
            purgedCount++
        }
        purgedCount
    }
}
