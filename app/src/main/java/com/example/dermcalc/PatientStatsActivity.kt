package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.launch

class PatientStatsActivity : AppCompatActivity() {
    // Inizializzati a -1L per evitare la gestione del Null (Long?)
    private var idDottoreLoggato: Long = -1L
    private var idPaziente: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients_stats)

        val listViewPatientStats = findViewById<ListView>(R.id.listViewPatientsStats)
        val textViewRisultato = findViewById<TextView>(R.id.PercentagePASI)

        // 1. Recupero robusto dei dati tramite getLongExtra con valore di default
        idDottoreLoggato = intent.getLongExtra("DOCTOR_ID", -1L)
        idPaziente = intent.getLongExtra("PATIENT_ID", -1L)

        // 2. Controllo di sicurezza: se uno dei due ID è fallito, interrompiamo senza crash
        if (idDottoreLoggato == -1L || idPaziente == -1L) {
            Toast.makeText(this, "Errore sessione o paziente non valido!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)

        // COROUTINE 1: Gestione della lista report del medico
        lifecycleScope.launch {
            db.DermCalcDao().getReportValutazioniDelMedico(idDottoreLoggato).collect { listaReport ->
                val adapter = object : ArrayAdapter<Valutazione>(
                    this@PatientStatsActivity,
                    R.layout.item_detail_report,
                    listaReport
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val rowView = convertView ?: layoutInflater.inflate(R.layout.item_detail_report, parent, false)
                        val report = getItem(position)!!

                        rowView.findViewById<TextView>(R.id.txtSectionTitle).text = "Paziente ID: #${report.pazienteIdVisitato}"
                        rowView.findViewById<TextView>(R.id.txtSectionBody).text = "Punteggio: ${report.punteggioFinale}\nData: ${report.dataValutazione}"

                        return rowView
                    }
                }
                listViewPatientStats.adapter = adapter
            }
        }

        // COROUTINE 2: Calcolo della variazione PASI del singolo paziente passato
        lifecycleScope.launch {
            if (db.DermCalcDao().getNumValutazioniPazientePASI(idPaziente) > 1) {
                val primoPasi = db.DermCalcDao().getPrimoValorePASI(idPaziente)
                val ultimoPasi = db.DermCalcDao().getUltimoValorePASI(idPaziente)

                if (!primoPasi.equals(0.0)) { // Confronto matematico pulito senza .equals()
                    val variazionePercentuale = ((ultimoPasi - primoPasi) / primoPasi) * 100

                    if (variazionePercentuale < 0) {
                        val percentualePositiva = Math.abs(variazionePercentuale)
                        textViewRisultato.text = "Miglioramento del ${String.format("%.1f", percentualePositiva)}%"
                    } else if (variazionePercentuale > 0) {
                        textViewRisultato.text = "Peggioramento del ${String.format("%.1f", variazionePercentuale)}%"
                    } else {
                        textViewRisultato.text = "Situazione stabile (0% di variazione)"
                    }
                }
            } else {
                textViewRisultato.text = "Dati insufficienti (necessarie almeno 2 valutazioni)"
            }
        }
    }
}