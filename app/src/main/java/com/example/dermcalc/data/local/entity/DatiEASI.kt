package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dati_easi",
    foreignKeys = [
        ForeignKey(
            entity = Valutazione::class,
            parentColumns = ["valutazioneId"],
            childColumns = ["valutazioneId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DatiEASI(
    @PrimaryKey val valutazioneId: Long,
    val eTesta: Int, val iTesta: Int, val escoriazioneTesta: Int, val lTesta: Int, val aTesta: Int,
    val eArtiSup: Int, val iArtiSup: Int, val escoriazioneArtiSup: Int, val lArtiSup: Int, val aArtiSup: Int,
    val eTronco: Int, val iTronco: Int, val escoriazioneTronco: Int, val lTronco: Int, val aTronco: Int,
    val eArtiInf: Int, val iArtiInf: Int, val escoriazioneArtiInf: Int, val lArtiInf: Int, val aArtiInf: Int
)