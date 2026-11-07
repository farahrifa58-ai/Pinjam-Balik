package com.example.pinjambalik.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pinjambalik.data.model.TransactionPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM transaction_photos WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    fun getPhotosForTransaction(transactionId: Long): Flow<List<TransactionPhoto>>

    @Query("SELECT * FROM transaction_photos WHERE transactionId = :transactionId ORDER BY createdAt ASC")
    suspend fun getPhotosForTransactionOnce(transactionId: Long): List<TransactionPhoto>

    @Query("SELECT * FROM transaction_photos WHERE transactionId = :transactionId AND photoType = :type ORDER BY createdAt ASC")
    fun getPhotosByType(transactionId: Long, type: String): Flow<List<TransactionPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: TransactionPhoto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<TransactionPhoto>): List<Long>

    @Update
    suspend fun updatePhoto(photo: TransactionPhoto)

    @Delete
    suspend fun deletePhoto(photo: TransactionPhoto)

    @Query("DELETE FROM transaction_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)

    @Query("DELETE FROM transaction_photos WHERE transactionId = :transactionId")
    suspend fun deletePhotosForTransaction(transactionId: Long)
}
