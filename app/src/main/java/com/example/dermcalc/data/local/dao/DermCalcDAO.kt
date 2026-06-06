package com.example.dermcalc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Personale
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.flow.Flow


@Dao
interface DermCalcDAO {
    // ABORT: Evita duplicati
    // Serve per forza questa query, per creare almeno un Utente durante l'inizializzazione del DB
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserisciPersonale(personale: Personale): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserisciPaziente(paziente: Paziente): Long

    @Insert
    suspend fun inserisciValutazione(valutazione: Valutazione): Long

    // Trova tutte le valutazioni di un specifico paziente
    @Query("SELECT * FROM valutazione WHERE pazienteIdVisitato = :pazienteId ORDER BY dataValutazione DESC")
    fun getValutazioniDelPaziente(pazienteId: Long): Flow<List<Valutazione>>

    // Trova tutti i pazienti di un specifico responsabile
    @Query("SELECT * FROM paziente WHERE personaleIdResponsabile = :responsabileId")
    fun getPazientiDelResponsabile(responsabileId: Long): Flow<List<Paziente>>

    // Query per login. Se non trova l'utente restituisce null
    @Query("SELECT * FROM personale WHERE username = :user AND passwordCifrata = :pass LIMIT 1")
    suspend fun verificaLogin(user: String, pass: String): Personale?
}