package com.example.dermcalc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "personale",
    indices = [Index(value = ["username"], unique = true)] //username senza duplicati
)
data class Personale(
    @PrimaryKey(autoGenerate = true)
    val personaleId: Long = 0,
    val nome: String,
    val cognome: String,
    val ruolo: String,
    val username: String,
    val passwordCifrata: String
)