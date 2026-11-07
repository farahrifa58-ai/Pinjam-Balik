package com.example.pinjambalik.util

import com.example.pinjambalik.data.model.RentalTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrivacyUtils {

    fun maskIdentityNumber(number: String?): String {
        if (number.isNullOrBlank()) return "-"
        val trimmed = number.trim()
        if (trimmed.length <= 4) return "****"
        if (trimmed.length <= 8) {
            val prefix = trimmed.take(2)
            val suffix = trimmed.takeLast(2)
            return "$prefix****$suffix"
        }
        val prefix = trimmed.take(6)
        val suffix = trimmed.takeLast(4)
        val maskLength = (trimmed.length - 10).coerceAtLeast(4)
        val mask = "*".repeat(maskLength)
        return "$prefix$mask$suffix"
    }

    fun formatCurrency(amount: Double): String {
        val localeID = Locale.forLanguageTag("id-ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        format.maximumFractionDigits = 0
        return format.format(amount).replace("Rp", "Rp ")
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return "-"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        return sdf.format(Date(timestamp))
    }

    fun generateWhatsAppReceipt(tx: RentalTransaction): String {
        val sb = StringBuilder()
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📦 *NOTA BUKTI SEWA PINJAMBALIK* 📦")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("No. Transaksi : *${tx.invoiceCode}*")
        sb.appendLine("Tanggal Buat  : ${formatDateTime(tx.createdAt)}")
        sb.appendLine("Status Sewa   : *${tx.transactionStatus}*")
        sb.appendLine("")
        sb.appendLine("📋 *DETAIL BARANG:*")
        sb.appendLine("• Nama Barang : *${tx.itemName}*")
        sb.appendLine("• Kategori    : ${tx.itemCategory}")
        if (tx.itemSerialNumber.isNotBlank()) {
            sb.appendLine("• No. Seri    : ${tx.itemSerialNumber}")
        }
        sb.appendLine("• Kondisi Awal: ${tx.itemConditionBefore}")
        sb.appendLine("")
        sb.appendLine("👤 *PENYEWA:*")
        sb.appendLine("• Nama        : *${tx.customerName}*")
        if (tx.customerPhone.isNotBlank()) {
            sb.appendLine("• No. HP      : ${tx.customerPhone}")
        }
        sb.appendLine("")
        sb.appendLine("⏱️ *PERIODE SEWA:*")
        sb.appendLine("• Mulai Sewa  : ${formatDate(tx.rentalStartDate)}")
        sb.appendLine("• Jatuh Tempo : ${formatDate(tx.rentalDueDate)}")
        if (tx.rentalActualReturnDate != null) {
            sb.appendLine("• Kembali     : ${formatDateTime(tx.rentalActualReturnDate)}")
        }
        sb.appendLine("")
        sb.appendLine("💰 *RINCIAN BIAYA:*")
        sb.appendLine("• Total Sewa  : ${formatCurrency(tx.totalRentalFee)}")
        if (tx.depositAmount > 0) {
            sb.appendLine("• Jaminan/DP  : ${formatCurrency(tx.depositAmount)}")
        }
        if (tx.repairFee > 0) {
            sb.appendLine("• Denda/Perbaikan: ${formatCurrency(tx.repairFee)}")
            if (!tx.returnDamageNotes.isNullOrBlank()) {
                sb.appendLine("  (Keterangan: ${tx.returnDamageNotes})")
            }
        }
        val grandTotal = tx.totalRentalFee + tx.repairFee
        sb.appendLine("• *Total Akhir* : *${formatCurrency(grandTotal)}*")
        sb.appendLine("• Pembayaran  : *${tx.paymentStatus}*")
        sb.appendLine("")
        sb.appendLine("⚠️ *CATATAN:*")
        sb.appendLine("Harap kembalikan barang tepat waktu beserta seluruh kelengkapannya. Dokumentasi kondisi barang telah tercatat aman di sistem PinjamBalik.")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Terima kasih atas kepercayaan Anda!")
        return sb.toString()
    }
}
