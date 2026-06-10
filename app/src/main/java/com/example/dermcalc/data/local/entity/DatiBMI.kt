package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dati_bmi",
    foreignKeys = [
        ForeignKey(
            entity = Valutazione::class,
            parentColumns = ["valutazioneId"],
            childColumns = ["valutazioneId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DatiBMI(
    @PrimaryKey val valutazioneId: Long,
    val altezza: Double,
    val peso: Double
)