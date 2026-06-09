package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Valutazione
import kotlinx.coroutines.launch

class ReportsFragment : Fragment(R.layout.fragment_reports) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listViewReports = view.findViewById<ListView>(R.id.listViewReports)
        val activityGuscio = activity as? ReportsActivity
        val idDottore = activityGuscio?.idDottoreLoggato ?: return // Se è null ferma l'esecuzione

        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            db.DermCalcDao().getReportValutazioniDelMedico(idDottore).collect { listaReport ->

                val adapter = object : ArrayAdapter<Valutazione>(
                    requireContext(),
                    R.layout.item_detail_report,
                    listaReport
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val rowView = convertView ?: layoutInflater.inflate(R.layout.item_detail_report, parent, false)
                        val report = getItem(position)!!

                        rowView.findViewById<TextView>(R.id.txtSectionTitle).text = "Paziente ID: #${report.pazienteIdVisitato}"
                        rowView.findViewById<TextView>(R.id.txtSectionBody).text = "Indice: ${report.tipologiaIndice} • Punteggio: ${report.punteggioFinale}\nData: ${report.dataValutazione}"

                        return rowView
                    }
                }
                listViewReports.adapter = adapter
            }
        }

        listViewReports.setOnItemClickListener { parent, _, position, _ ->
            val report = parent.getItemAtPosition(position) as Valutazione
            activityGuscio.apriDettaglio(report.valutazioneId, report.tipologiaIndice)
        }
    }
}