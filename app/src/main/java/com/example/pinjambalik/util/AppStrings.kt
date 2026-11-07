package com.example.pinjambalik.util

object AppStrings {
    fun isIndonesian(lang: String): Boolean = lang.lowercase().startsWith("id")

    fun get(lang: String) = if (isIndonesian(lang)) Id else En

    interface Dictionary {
        val appName: String
        val appSubtitle: String
        val byDeveloper: String
        val searchHint: String
        val newRental: String
        val activeRentals: String
        val completedRentals: String
        val all: String
        val emptyTitle: String
        val emptyDesc: String
        val pressBackAgainToExit: String
        val trialActiveBanner: String
        val trialExpiredTitle: String
        val trialExpiredDesc: String
        val upgradeFull: String
        val contactDeveloper: String
        val settingsTitle: String
        val appearanceHeader: String
        val themeTitle: String
        val themeLight: String
        val themeDark: String
        val themeSystem: String
        val languageTitle: String
        val langId: String
        val langEn: String
        val retentionHeader: String
        val retentionDesc: String
        val retentionLabel: String
        val purgeButton: String
        val aboutAppButton: String
        val aboutAppDesc: String
        val licenseStatus: String
        val licenseTrial: String
        val licenseFull: String
        val activateFull: String
        val activateDialogTitle: String
        val activateDialogDesc: String
        val licenseKeyPlaceholder: String
        val activateBtn: String
        val cancelBtn: String
        val resetTrialTimer: String
        val aboutTitle: String
        val versionLabel: String
        val developerLabel: String
        val appSummary: String
        val close: String
        val discardTitle: String
        val discardDesc: String
        val discardYes: String
        val discardNo: String
        val back: String
    }

    object Id : Dictionary {
        override val appName = "PinjamBalik"
        override val appSubtitle = "Dokumentasi Sewa & Identitas"
        override val byDeveloper = "by Rafline Dev"
        override val searchHint = "Cari barang, nama penyewa, no. nota..."
        override val newRental = "Sewa Baru"
        override val activeRentals = "Aktif Disewa"
        override val completedRentals = "Selesai"
        override val all = "Semua"
        override val emptyTitle = "Belum Ada Transaksi"
        override val emptyDesc = "Tekan tombol + Sewa Baru untuk mulai mencatat transaksi penyewaan barang"
        override val pressBackAgainToExit = "Tekan sekali lagi untuk keluar dari aplikasi"
        override val trialActiveBanner = "Mode Trial Aktif (Sisa %s)"
        override val trialExpiredTitle = "Waktu Trial 60 Menit Telah Habis"
        override val trialExpiredDesc = "Terima kasih telah mencoba PinjamBalik. Hubungi Rafline Dev untuk mengaktifkan Full Version tanpa batas waktu."
        override val upgradeFull = "Upgrade Full"
        override val contactDeveloper = "Hubungi Rafline Dev"
        override val settingsTitle = "Pengaturan & Privasi"
        override val appearanceHeader = "Tampilan & Tema"
        override val themeTitle = "Tema Aplikasi"
        override val themeLight = "Terang (Default)"
        override val themeDark = "Gelap"
        override val themeSystem = "Ikuti Sistem"
        override val languageTitle = "Bahasa Aplikasi"
        override val langId = "Bahasa Indonesia"
        override val langEn = "English"
        override val retentionHeader = "Keamanan & Privasi Dokumen"
        override val retentionDesc = "Untuk melindungi privasi pelanggan, foto KTP/SIM pada transaksi yang telah selesai akan dibersihkan otomatis setelah periode ini."
        override val retentionLabel = "Masa Simpan Dokumen"
        override val purgeButton = "Bersihkan Dokumen Kedaluwarsa Sekarang"
        override val aboutAppButton = "Tentang Aplikasi"
        override val aboutAppDesc = "Informasi seputar aplikasi, developer Rafline Dev, dan status lisensi"
        override val licenseStatus = "Status Lisensi"
        override val licenseTrial = "Trial (60 Menit)"
        override val licenseFull = "Full Version (Aktif Selamanya)"
        override val activateFull = "Aktivasi Full Version"
        override val activateDialogTitle = "Aktivasi Lisensi Full Version"
        override val activateDialogDesc = "Masukkan kode lisensi aktivasi atau klik Aktifkan untuk membuka seluruh fitur permanen."
        override val licenseKeyPlaceholder = "Masukkan kode lisensi..."
        override val activateBtn = "Aktifkan Sekarang"
        override val cancelBtn = "Batal"
        override val resetTrialTimer = "Reset Timer Trial (60 Menit)"
        override val aboutTitle = "Tentang Aplikasi PinjamBalik"
        override val versionLabel = "Versi"
        override val developerLabel = "Pengembang (Developer)"
        override val appSummary = "PinjamBalik adalah aplikasi cerdas dokumentasi serah terima barang sewaan secara akurat dan terpercaya. Dilengkapi foto kondisi fisik barang awal & pengembalian, checklist kelengkapan aksesoris, keamanan retensi dokumen KTP/SIM pelanggan, dan rekap keuangan."
        override val close = "Tutup"
        override val discardTitle = "Batalkan Transaksi?"
        override val discardDesc = "Data yang telah diisi belum disimpan. Apakah Anda yakin ingin kembali?"
        override val discardYes = "Ya, Batalkan"
        override val discardNo = "Lanjutkan Isi"
        override val back = "Kembali"
    }

    object En : Dictionary {
        override val appName = "PinjamBalik"
        override val appSubtitle = "Rental & Identity Docs"
        override val byDeveloper = "by Rafline Dev"
        override val searchHint = "Search item, renter name, invoice..."
        override val newRental = "New Rental"
        override val activeRentals = "Active Rentals"
        override val completedRentals = "Completed"
        override val all = "All"
        override val emptyTitle = "No Transactions Yet"
        override val emptyDesc = "Tap + New Rental button to record rental transactions"
        override val pressBackAgainToExit = "Press back once more to exit app"
        override val trialActiveBanner = "Trial Mode Active (%s left)"
        override val trialExpiredTitle = "60-Minute Trial Has Expired"
        override val trialExpiredDesc = "Thank you for using PinjamBalik. Contact Rafline Dev to activate permanent Full Version."
        override val upgradeFull = "Upgrade Full"
        override val contactDeveloper = "Contact Rafline Dev"
        override val settingsTitle = "Settings & Privacy"
        override val appearanceHeader = "Appearance & Theme"
        override val themeTitle = "App Theme"
        override val themeLight = "Light (Default)"
        override val themeDark = "Dark"
        override val themeSystem = "System Default"
        override val languageTitle = "App Language"
        override val langId = "Bahasa Indonesia"
        override val langEn = "English"
        override val retentionHeader = "Document Privacy & Security"
        override val retentionDesc = "To protect customer privacy, ID documents on completed rentals are automatically cleaned up after this period."
        override val retentionLabel = "Document Retention Period"
        override val purgeButton = "Purge Expired Documents Now"
        override val aboutAppButton = "About Application"
        override val aboutAppDesc = "App details, developer Rafline Dev, and license status"
        override val licenseStatus = "License Status"
        override val licenseTrial = "Trial (60 Minutes)"
        override val licenseFull = "Full Version (Permanent)"
        override val activateFull = "Activate Full Version"
        override val activateDialogTitle = "Activate Full Version License"
        override val activateDialogDesc = "Enter activation key or tap Activate to unlock permanent full features."
        override val licenseKeyPlaceholder = "Enter license key..."
        override val activateBtn = "Activate Now"
        override val cancelBtn = "Cancel"
        override val resetTrialTimer = "Reset Trial Timer (60 Mins)"
        override val aboutTitle = "About PinjamBalik App"
        override val versionLabel = "Version"
        override val developerLabel = "Developer"
        override val appSummary = "PinjamBalik is a professional rental documentation app for equipment handovers. Features condition photos before & after rental, item accessories checklist, encrypted customer ID retention protection, and revenue tracking."
        override val close = "Close"
        override val discardTitle = "Discard Rental?"
        override val discardDesc = "Entered information has not been saved. Are you sure you want to go back?"
        override val discardYes = "Yes, Discard"
        override val discardNo = "Keep Editing"
        override val back = "Back"
    }
}
