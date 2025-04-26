package com.control.porcinochl

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cerdas")
data class Cerda(
    @PrimaryKey val id: String,
    val fechaPrenez: Date,
    val fechaCelo: Date = Date(fechaPrenez.time + 21 * 86400000L), // 21 días después
    val fechaParto: Date = Date(fechaPrenez.time + 114 * 86400000L), // 114 días después
    val fechaDestete: Date = Date(fechaParto.time + 21 * 86400000L) // 21 días después del parto
) {
    init {
        require(id.isNotBlank()) { "El ID no puede estar vacío" }
    }
}