package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dati_bsa",
    foreignKeys = [
        ForeignKey(
            entity = Valutazione::class,
            parentColumns = ["valutazioneId"],
            childColumns = ["valutazioneId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DatiBSA(
    @PrimaryKey val valutazioneId: Long,
    val testaCollo: Double,
    val artoSupDx: Double,
    val artoSupSx: Double,
    val troncoAnt: Double,
    val troncoPost: Double,
    val artoInfDx: Double,
    val artoInfSx: Double,
    val genitali: Double
)