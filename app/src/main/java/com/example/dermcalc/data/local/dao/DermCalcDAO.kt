package com.example.dermcalc.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dermcalc.data.local.entity.DatiBMI
import com.example.dermcalc.data.local.entity.DatiBSA
import com.example.dermcalc.data.local.entity.DatiEASI
import com.example.dermcalc.data.local.entity.DatiPASI
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Personale
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.flow.Flow

@Dao
interface DermCalcDAO {

    // ABORT: Evita duplicati in caso di conflitto primario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserisciPersonale(personale: Personale): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun inserisciPaziente(paziente: Paziente): Long

    @Delete
    suspend fun rimuoviPaziente(paziente: Paziente)

    @Insert
    suspend fun inserisciValutazione(valutazione: Valutazione): Long

    @Insert
    suspend fun inserisciDatiEasi(datiEasi: DatiEASI)

    @Insert
    suspend fun inserisciDatiPasi(datiPasi: DatiPASI)

    @Insert
    suspend fun inserisciDatiBMI(datiBMI: DatiBMI)

    @Insert
    suspend fun inserisciDatiBSA(datiBsa: DatiBSA)

    // Trova tutte le valutazioni di uno specifico paziente
    @Query("SELECT * FROM valutazione WHERE pazienteIdVisitato = :pazienteId ORDER BY dataValutazione DESC")
    fun getValutazioniDelPaziente(pazienteId: Long): Flow<List<Valutazione>>

    // Trova tutti i pazienti di uno specifico responsabile
    @Query("SELECT * FROM paziente WHERE personaleIdResponsabile = :responsabileId")
    fun getPazientiDelResponsabile(responsabileId: Long): Flow<List<Paziente>>

    // Query per login. Se non trova l'utente restituisce null
    @Query("SELECT * FROM personale WHERE username = :user AND passwordCifrata = :pass LIMIT 1")
    suspend fun verificaLogin(user: String, pass: String): Personale?

    // Dato un ID, trova le informazioni di quel personale
    @Query("SELECT * FROM personale WHERE personaleId = :id LIMIT 1")
    suspend fun getDottoreById(id: Long): Personale?

    // Query per estrarre i dettagli (allineate con i nomi usati nel frammento)
    @Query("SELECT * FROM dati_easi WHERE valutazioneId = :id LIMIT 1")
    suspend fun getDettagliEasiPerId(id: Long): DatiEASI?

    @Query("SELECT * FROM dati_pasi WHERE valutazioneId = :id LIMIT 1")
    suspend fun getDettagliPasiPerId(id: Long): DatiPASI?

    @Query("SELECT * FROM dati_bmi WHERE valutazioneId = :id LIMIT 1")
    suspend fun getDettagliBMIPerId(id: Long): DatiBMI?

    @Query("SELECT * FROM dati_bsa WHERE valutazioneId = :id LIMIT 1")
    suspend fun getDettagliBSAPerId(id: Long): DatiBSA?

    @Query("SELECT * FROM valutazione WHERE personaleIdResponsabile = :medicoId ORDER BY dataValutazione DESC")
    fun getReportValutazioniDelMedico(medicoId: Long): Flow<List<Valutazione>>

    @Query("DELETE FROM valutazione WHERE valutazioneId = :id")
    suspend fun cancellaValutazionePerId(id: Long)

    // Query per le Statistiche
    // --- SEZIONE PASI ---
    @Query("SELECT COUNT(*) FROM valutazione WHERE tipologiaIndice = 'PASI' AND pazienteIdVisitato = :id")
    suspend fun getNumValutazioniPazientePASI(id: Long): Int

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'PASI' AND pazienteIdVisitato = :id ORDER BY dataValutazione ASC LIMIT 1")
    suspend fun getPrimoValorePASI(id: Long): Double? // <- Aggiunto ?

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'PASI' AND pazienteIdVisitato = :id ORDER BY dataValutazione DESC LIMIT 1")
    suspend fun getUltimoValorePASI(id: Long): Double? // <- Aggiunto ?

    // --- SEZIONE EASI ---
    @Query("SELECT COUNT(*) FROM valutazione WHERE tipologiaIndice = 'EASI' AND pazienteIdVisitato = :id")
    suspend fun getNumValutazioniPazienteEASI(id: Long): Int

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'EASI' AND pazienteIdVisitato = :id ORDER BY dataValutazione ASC LIMIT 1")
    suspend fun getPrimoValoreEASI(id: Long): Double? // <- Aggiunto ?

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'EASI' AND pazienteIdVisitato = :id ORDER BY dataValutazione DESC LIMIT 1")
    suspend fun getUltimoValoreEASI(id: Long): Double? // <- Aggiunto ?

    // --- SEZIONE BMI ---
    @Query("SELECT COUNT(*) FROM valutazione WHERE tipologiaIndice = 'BMI' AND pazienteIdVisitato = :id")
    suspend fun getNumValutazioniPazienteBMI(id: Long): Int

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'BMI' AND pazienteIdVisitato = :id ORDER BY dataValutazione ASC LIMIT 1")
    suspend fun getPrimoValoreBMI(id: Long): Double? // <- Aggiunto ?

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'BMI' AND pazienteIdVisitato = :id ORDER BY dataValutazione DESC LIMIT 1")
    suspend fun getUltimoValoreBMI(id: Long): Double? // <- Aggiunto ?

    // --- SEZIONE BSA ---
    @Query("SELECT COUNT(*) FROM valutazione WHERE tipologiaIndice = 'BSA' AND pazienteIdVisitato = :id")
    suspend fun getNumValutazioniPazienteBSA(id: Long): Int

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'BSA' AND pazienteIdVisitato = :id ORDER BY dataValutazione ASC LIMIT 1")
    suspend fun getPrimoValoreBSA(id: Long): Double? // <- Aggiunto ?

    @Query("SELECT punteggioFinale FROM valutazione WHERE tipologiaIndice = 'BSA' AND pazienteIdVisitato = :id ORDER BY dataValutazione DESC LIMIT 1")
    suspend fun getUltimoValoreBSA(id: Long): Double? // <- Aggiunto ?

    @Query("SELECT nome FROM Paziente WHERE pazienteId = :id")
    suspend fun getNomeByIdPaziente(id: Long): String?

    @Query("SELECT cognome FROM Paziente WHERE pazienteId = :id")
    suspend fun getCognomeByIdPaziente(id: Long): String?
}