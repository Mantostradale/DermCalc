package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dati_pasi",
    foreignKeys = [
        ForeignKey(
            entity = Valutazione::class,
            parentColumns = ["valutazioneId"],
            childColumns = ["valutazioneId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DatiPASI(
    @PrimaryKey val valutazioneId: Long, // ID della tabella Valutazione
    val eTesta: Int, val iTesta: Int, val dTesta: Int, val aTesta: Int,
    val eArtiSup: Int, val iArtiSup: Int, val dArtiSup: Int, val aArtiSup: Int,
    val eTronco: Int, val iTronco: Int, val dTronco: Int, val aTronco: Int,
    val eArtiInf: Int, val iArtiInf: Int, val dArtiInf: Int, val aArtiInf: Int
)