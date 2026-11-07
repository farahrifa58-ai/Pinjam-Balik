package com.example.pinjambalik.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinjambalik.data.local.AppDatabase
import com.example.pinjambalik.data.model.RentalTransaction
import com.example.pinjambalik.data.model.TransactionPhoto
import com.example.pinjambalik.data.repository.RentalRepository
import com.example.pinjambalik.util.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RentalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = RentalRepository(
        context = application,
        transactionDao = database.transactionDao(),
        photoDao = database.photoDao()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("SEMUA") // SEMUA, DISEWA, SELESAI
    val statusFilter = _statusFilter.asStateFlow()

    val allTransactions: StateFlow<List<RentalTransaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredTransactions: StateFlow<List<RentalTransaction>> = combine(
        allTransactions,
        _searchQuery,
        _statusFilter
    ) { list, query, filter ->
        list.filter { item ->
            val matchQuery = query.isBlank() ||
                    item.itemName.contains(query, ignoreCase = true) ||
                    item.customerName.contains(query, ignoreCase = true) ||
                    item.invoiceCode.contains(query, ignoreCase = true)

            val matchFilter = when (filter) {
                "DISEWA" -> item.transactionStatus == "DISEWA"
                "SELESAI" -> item.transactionStatus == "SELESAI"
                else -> true
            }

            matchQuery && matchFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Trial Mode & Timer
    private val _isTrialActive = MutableStateFlow(false)
    val isTrialActive: StateFlow<Boolean> = _isTrialActive.asStateFlow()

    private val _trialRemainingSeconds = MutableStateFlow(AppConfig.TRIAL_DURATION_SECONDS)
    val trialRemainingSeconds: StateFlow<Long> = _trialRemainingSeconds.asStateFlow()

    private val _isTrialExpired = MutableStateFlow(false)
    val isTrialExpired: StateFlow<Boolean> = _isTrialExpired.asStateFlow()

    // Retention Days
    private val _retentionDays = MutableStateFlow(60L)
    val retentionDays: StateFlow<Long> = _retentionDays.asStateFlow()

    init {
        viewModelScope.launch {
            AppConfig.isTrialModeActive(application).collect { active ->
                _isTrialActive.value = active
            }
        }

        viewModelScope.launch {
            AppConfig.getRetentionDays(application).collect { days ->
                _retentionDays.value = days
            }
        }

        viewModelScope.launch {
            AppConfig.getTrialUsedSeconds(application).collect { used ->
                val remaining = (AppConfig.TRIAL_DURATION_SECONDS - used).coerceAtLeast(0L)
                _trialRemainingSeconds.value = remaining
                _isTrialExpired.value = remaining <= 0L
            }
        }

        // Timer ticking loop for trial mode
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                if (_isTrialActive.value && !_isTrialExpired.value) {
                    val used = AppConfig.incrementTrialTime(application, 1L)
                    val remaining = (AppConfig.TRIAL_DURATION_SECONDS - used).coerceAtLeast(0L)
                    _trialRemainingSeconds.value = remaining
                    if (remaining <= 0L) {
                        _isTrialExpired.value = true
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun saveTransaction(
        transaction: RentalTransaction,
        photos: List<TransactionPhoto>,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.saveNewTransaction(transaction, photos)
            onComplete(id)
        }
    }

    fun updateTransaction(transaction: RentalTransaction, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(transaction: RentalTransaction, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            onComplete()
        }
    }

    fun completeReturn(
        transactionId: Long,
        returnCondition: String,
        damageNotes: String?,
        repairFee: Double,
        returnPhotos: List<TransactionPhoto>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.completeReturn(
                transactionId = transactionId,
                returnCondition = returnCondition,
                damageNotes = damageNotes,
                repairFee = repairFee,
                returnPhotos = returnPhotos
            )
            onComplete()
        }
    }

    fun clearCustomerIdentityPhotos(transactionId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearIdentityPhotos(transactionId)
            onComplete()
        }
    }

    fun addPhotoToTransaction(photo: TransactionPhoto, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addPhoto(photo)
            onComplete()
        }
    }

    fun deletePhoto(photo: TransactionPhoto, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
            onComplete()
        }
    }

    fun purgeExpiredDocuments(onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.purgeExpiredIdentityDocuments(_retentionDays.value)
            onResult(count)
        }
    }

    fun updateRetentionDays(days: Long) {
        viewModelScope.launch {
            AppConfig.setRetentionDays(getApplication(), days)
        }
    }

    fun toggleForceTrial(enabled: Boolean) {
        viewModelScope.launch {
            AppConfig.setForceTrialMode(getApplication(), enabled)
        }
    }

    fun resetTrial() {
        viewModelScope.launch {
            AppConfig.resetTrialTimer(getApplication())
        }
    }
}
