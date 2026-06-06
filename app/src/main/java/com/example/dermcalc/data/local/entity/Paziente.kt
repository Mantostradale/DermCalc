package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "paziente",
    foreignKeys = [
        ForeignKey(
            entity = Personale::class,
            parentColumns = ["personaleId"],
            childColumns = ["personaleIdResponsabile"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Paziente(
    @PrimaryKey(autoGenerate = true)
    val pazienteId: Long = 0,
    val personaleIdResponsabile: Long, // Chiave esterna verso Personale
    val nome: String,
    val cognome: String,
    val codiceFiscale: String,
    val dataNascita: String
)