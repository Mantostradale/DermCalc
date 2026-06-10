package com.example.dermcalc

import android.R.attr.id
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.dao.DermCalcDAO
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.launch

class PatientStatsActivity : AppCompatActivity() {
    private var idDottoreLoggato: Long? = null
    private var idPaziente: Long? = null // Usiamo la variabile di classe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients_stats)

        val listViewPatientStats = findViewById<ListView>(R.id.listViewPatientsStats)
        val textViewRisultato = findViewById<TextView>(R.id.PercentagePASI) // <-- Mancava questo!

        // 1. Recupero ENTRAMBI gli ID dagli Intent extra
        idDottoreLoggato = intent.extras?.get("DOCTOR_ID") as? Long

        if (idDottoreLoggato == null) {
            Toast.makeText(this, "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)

        // COROUTINE 1: Gestione asincrona del Flow della lista (rimane sempre in ascolto)
        lifecycleScope.launch {
            db.DermCalcDao().getReportValutazioniDelMedico(idDottoreLoggato!!).collect { listaReport ->
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

        // COROUTINE 2: Calcolo del PASI (Eseguita in parallelo, non viene bloccata dal collect precedente)
        lifecycleScope.launch {
            val idSelezionato = idPaziente!! // Ora è sicuro usare !! dopo il controllo iniziale

            if (db.DermCalcDao().getNumValutazioniPazientePASI(idSelezionato) > 1) {
                val primoPasi = db.DermCalcDao().getPrimoValorePASI(idSelezionato)
                val ultimoPasi = db.DermCalcDao().getUltimoValorePASI(idSelezionato)

                if (!primoPasi.equals(0.0)) { // Evitiamo divisioni per zero se il primo PASI fosse 0
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