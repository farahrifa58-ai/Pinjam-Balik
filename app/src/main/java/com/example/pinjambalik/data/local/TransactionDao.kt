package com.example.pinjambalik.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pinjambalik.data.model.RentalTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM rental_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<RentalTransaction>>

    @Query("SELECT * FROM rental_transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Long): Flow<RentalTransaction?>

    @Query("SELECT * FROM rental_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionByIdOnce(id: Long): RentalTransaction?

    @Query("SELECT * FROM rental_transactions WHERE transactionStatus = :status ORDER BY createdAt DESC")
    fun getTransactionsByStatus(status: String): Flow<List<RentalTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: RentalTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: RentalTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: RentalTransaction)

    @Query("DELETE FROM rental_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("""
        UPDATE rental_transactions 
        SET identityFrontPhotoPath = null, identityBackPhotoPath = null, customerWithItemPhotoPath = null, updatedAt = :timestamp 
        WHERE id = :id
    """)
    suspend fun clearIdentityPhotos(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        SELECT * FROM rental_transactions 
        WHERE transactionStatus = 'SELESAI' 
        AND (identityFrontPhotoPath IS NOT NULL OR identityBackPhotoPath IS NOT NULL OR customerWithItemPhotoPath IS NOT NULL)
        AND rentalActualReturnDate IS NOT NULL 
        AND rentalActualReturnDate < :cutoffTimestamp
    """)
    suspend fun getExpiredIdentityTransactions(cutoffTimestamp: Long): List<RentalTransaction>
}
