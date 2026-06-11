package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Valutazione
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import kotlin.math.abs

class PatientStatsActivity : AppCompatActivity() {

    private var idDottoreLoggato: Long? = null
    private var idPazienteIniziale: Long? = null

    // Variabile per ricordare chi stiamo guardando in questo momento
    private var pazienteCorrenteId: Long? = null

    // Magazzino invisibile per filtrare i referti velocemente
    private var tuttiIRefertiDelMedico: List<Valutazione> = emptyList()
    private lateinit var adapterReferti: ArrayAdapter<Valutazione>

    // Riferimenti UI globali
    private lateinit var txtStatPASI: TextView
    private lateinit var txtStatEASI: TextView
    private lateinit var txtStatBMI: TextView
    private lateinit var txtStatBSA: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients_stats)

        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val listViewPatientStats = findViewById<ListView>(R.id.listViewPatientsStats)
        val cardPatientSelector = findViewById<MaterialCardView>(R.id.cardPatientSelector)
        val spinnerPazienti = findViewById<Spinner>(R.id.spinnerPazientiNascosto)
        val txtPatientName = findViewById<TextView>(R.id.txtPatientName)

        txtStatPASI = findViewById(R.id.PercentagePASI)
        txtStatEASI = findViewById(R.id.PercentageEASI)
        txtStatBMI = findViewById(R.id.PercentageBMI)
        txtStatBSA = findViewById(R.id.PercentageBSA)

        idDottoreLoggato = intent.extras?.get("DOCTOR_ID") as? Long
        idPazienteIniziale = intent.extras?.get("PATIENT_ID") as? Long

        if (idDottoreLoggato == null || idPazienteIniziale == null) {
            Toast.makeText(this, "Errore sessione o paziente non valido!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val idDocSicuro = idDottoreLoggato!!
        val db = AppDatabase.getDatabase(this)

        // Cliccando la Card, si apre lo Spinner nascosto
        cardPatientSelector.setOnClickListener {
            spinnerPazienti.performClick()
        }

        adapterReferti = object : ArrayAdapter<Valutazione>(
            this@PatientStatsActivity,
            R.layout.item_detail_report,
            ArrayList<Valutazione>()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val rowView = convertView ?: layoutInflater.inflate(R.layout.item_detail_report, parent, false)
                val report = getItem(position)!!

                rowView.findViewById<TextView>(R.id.txtSectionTitle).text =
                    "Tipo: ${report.tipologiaIndice}"
                rowView.findViewById<TextView>(R.id.txtSectionBody).text =
                    "Punteggio: ${report.punteggioFinale}\nData: ${report.dataValutazione}"

                return rowView
            }
        }
        listViewPatientStats.adapter = adapterReferti

        // Scarico e aggiorno UI
        lifecycleScope.launch {
            db.DermCalcDao().getReportValutazioniDelMedico(idDocSicuro).collect { listaReport ->
                tuttiIRefertiDelMedico = listaReport

                pazienteCorrenteId?.let { pId ->
                    val refertiFiltrati = tuttiIRefertiDelMedico.filter { it.pazienteIdVisitato == pId }
                    adapterReferti.clear()
                    adapterReferti.addAll(refertiFiltrati)
                    adapterReferti.notifyDataSetChanged()
                }
            }
        }

        val adapterSpinner = ArrayAdapter<String>(
            this@PatientStatsActivity,
            R.layout.item_patient_stats_spinner,
            ArrayList<String>()
        )
        spinnerPazienti.adapter = adapterSpinner

        // Popolo lo spinner + selezione
        lifecycleScope.launch {
            db.DermCalcDao().getPazientiDelResponsabile(idDocSicuro).collect { listaPazienti ->
                if (listaPazienti.isEmpty()) {
                    txtPatientName.text = "Nessun paziente trovato"
                    adapterSpinner.clear()
                    return@collect
                }

                val nomiPazienti = listaPazienti.map { "${it.nome} ${it.cognome}" }
                adapterSpinner.clear()
                adapterSpinner.addAll(nomiPazienti)
                adapterSpinner.notifyDataSetChanged()

                // Seleziono automaticamente il paziente di partenza (primo)
                val indicePazientePassato = listaPazienti.indexOfFirst { it.pazienteId == idPazienteIniziale }
                if (indicePazientePassato >= 0) {
                    spinnerPazienti.setSelection(indicePazientePassato)
                    txtPatientName.text = nomiPazienti[indicePazientePassato]
                }

                // Azione quando scelgo un paziente
                spinnerPazienti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val pazienteSelezionato = listaPazienti[position]

                        pazienteCorrenteId = pazienteSelezionato.pazienteId

                        txtPatientName.text = "${pazienteSelezionato.nome} ${pazienteSelezionato.cognome}"

                        calcolaStatistichePaziente(db, pazienteCorrenteId!!)

                        // Filtro, sennò si vedrebbero i referti globalmente e non per utenti
                        val refertiFiltrati = tuttiIRefertiDelMedico.filter { it.pazienteIdVisitato == pazienteCorrenteId }
                        adapterReferti.clear()
                        adapterReferti.addAll(refertiFiltrati)
                        adapterReferti.notifyDataSetChanged()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }
    }

    //Funzione per calcolare le variazioni degli indici del paziente selezionato

    private fun calcolaStatistichePaziente(db: AppDatabase, idPaziente: Long) {
        lifecycleScope.launch {
            val dao = db.DermCalcDao()

            // PASI
            if (dao.getNumValutazioniPazientePASI(idPaziente) > 1) {
                val primo = dao.getPrimoValorePASI(idPaziente) ?: 0.0
                val ultimo = dao.getUltimoValorePASI(idPaziente) ?: 0.0
                txtStatPASI.text = generaTestoVariazione("PASI", primo, ultimo)
            } else {
                txtStatPASI.text = "PASI: Dati insufficienti (almeno 2 visite)"
            }

            // EASI
            if (dao.getNumValutazioniPazienteEASI(idPaziente) > 1) {
                val primo = dao.getPrimoValoreEASI(idPaziente) ?: 0.0
                val ultimo = dao.getUltimoValoreEASI(idPaziente) ?: 0.0
                txtStatEASI.text = generaTestoVariazione("EASI", primo, ultimo)
            } else {
                txtStatEASI.text = "EASI: Dati insufficienti (almeno 2 visite)"
            }

            // BMI
            if (dao.getNumValutazioniPazienteBMI(idPaziente) > 1) {
                val primo = dao.getPrimoValoreBMI(idPaziente) ?: 0.0
                val ultimo = dao.getUltimoValoreBMI(idPaziente) ?: 0.0
                txtStatBMI.text = generaTestoVariazione("BMI", primo, ultimo)
            } else {
                txtStatBMI.text = "BMI: Dati insufficienti (almeno 2 visite)"
            }

            // BSA
            if (dao.getNumValutazioniPazienteBSA(idPaziente) > 1) {
                val primo = dao.getPrimoValoreBSA(idPaziente) ?: 0.0
                val ultimo = dao.getUltimoValoreBSA(idPaziente) ?: 0.0
                txtStatBSA.text = generaTestoVariazione("BSA", primo, ultimo)
            } else {
                txtStatBSA.text = "BSA: Dati insufficienti (almeno 2 visite)"
            }
        }
    }

    // Funzione helper per il calcolo delle percentuali
    private fun generaTestoVariazione(nomeIndice: String, valoreIniziale: Double, valoreFinale: Double): String {
        if (valoreIniziale == 0.0) {
            return "$nomeIndice: Valore iniziale 0.0 (Impossibile calcolare %)"
        }

        val variazionePercentuale = ((valoreFinale - valoreIniziale) / valoreIniziale) * 100

        return when {
            variazionePercentuale < 0 -> {
                val diffPositiva = abs(variazionePercentuale)
                "$nomeIndice: Miglioramento del ${String.format("%.1f", diffPositiva)}%"
            }
            variazionePercentuale > 0 -> {
                "$nomeIndice: Peggioramento del ${String.format("%.1f", variazionePercentuale)}%"
            }
            else -> {
                "$nomeIndice: Stabile (0% variazione)"
            }
        }
    }
}