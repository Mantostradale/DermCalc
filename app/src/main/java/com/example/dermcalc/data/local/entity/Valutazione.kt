package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "valutazione",
    foreignKeys = [
        ForeignKey(
            entity = Paziente::class,
            parentColumns = ["pazienteId"],
            childColumns = ["pazienteIdVisitato"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Personale::class,
            parentColumns = ["personaleId"],
            childColumns = ["personaleIdResponsabile"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Valutazione(
    @PrimaryKey(autoGenerate = true)
    val valutazioneId: Long = 0,
    val pazienteIdVisitato: Long, // Prima chiave esterna
    val personaleIdResponsabile: Long, // Seconda chiave esterna
    val dataValutazione: String,
    val punteggioDermatologico: Int,
    val noteNotevoli: String
)