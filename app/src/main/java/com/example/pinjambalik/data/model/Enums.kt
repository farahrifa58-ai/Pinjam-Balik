package com.example.pinjambalik.data.model

enum class PhotoType(val displayName: String) {
    BEFORE_HANDOVER("Kondisi Sebelum Diserahkan"),
    COMPLETENESS("Kelengkapan / Aksesoris"),
    UPON_RETURN("Kondisi Saat Dikembalikan")
}

enum class IdentityType(val displayName: String) {
    KTP("KTP"),
    SIM("SIM"),
    PASPOR("Paspor"),
    OTHER("Kartu Identitas Lainnya")
}
