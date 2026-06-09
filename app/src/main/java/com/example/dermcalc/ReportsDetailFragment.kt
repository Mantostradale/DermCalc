package com.example.dermcalc

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import kotlinx.coroutines.launch

class ReportsDetailFragment : Fragment(R.layout.fragment_reports_details) {

    companion object {
        private const val KEY_VAL_ID = "VALUTAZIONE_ID"
        private const val KEY_TYPE = "TIPOLOGIA_INDICE"

        fun newInstance(valutazioneId: Long, tipologiaIndice: String): ReportsDetailFragment {
            val fragment = ReportsDetailFragment()
            fragment.arguments = Bundle().apply {
                putLong(KEY_VAL_ID, valutazioneId)
                putString(KEY_TYPE, tipologiaIndice)
            }
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val idValutazione = arguments?.getLong(KEY_VAL_ID) ?: return
        val tipoIndice = arguments?.getString(KEY_TYPE) ?: return

        val txtTitle = view.findViewById<TextView>(R.id.txtDetailTitle)
        val dynamicContainer = view.findViewById<LinearLayout>(R.id.layoutDynamicContent)
        val btnDeleteAssessment = view.findViewById<Button>(R.id.btnDeleteAssessment)

        txtTitle.text = "Referto #$idValutazione ($tipoIndice)"

        val db = AppDatabase.getDatabase(requireContext())

        // 1. Logica di popolamento delle Card cliniche
        viewLifecycleOwner.lifecycleScope.launch {
            dynamicContainer.removeAllViews()

            when (tipoIndice) {
                "EASI" -> {
                    val dettagli = db.DermCalcDao().getDettagliEasiPerId(idValutazione)
                    if (dettagli != null) {
                        iniettaCardClinica(dynamicContainer, "TESTA (EASI)", "Eritema: ${dettagli.eTesta} • Edema: ${dettagli.iTesta}\nLichenificazione: ${dettagli.lTesta} • Escoriazione: ${dettagli.escoriazioneTesta}\nArea: ${convertiArea(dettagli.aTesta)}")
                        iniettaCardClinica(dynamicContainer, "TRONCO (EASI)", "Eritema: ${dettagli.eTronco} • Edema: ${dettagli.iTronco}\nLichenificazione: ${dettagli.lTronco} • Escoriazione: ${dettagli.escoriazioneTronco}\nArea: ${convertiArea(dettagli.aTronco)}")
                        iniettaCardClinica(dynamicContainer, "ARTI SUPERIORI (EASI)", "Eritema: ${dettagli.eArtiSup} • Edema: ${dettagli.iArtiSup}\nLichenificazione: ${dettagli.lArtiSup} • Escoriazione: ${dettagli.escoriazioneArtiSup}\nArea: ${convertiArea(dettagli.aArtiSup)}")
                        iniettaCardClinica(dynamicContainer, "ARTI INFERIORI (EASI)", "Eritema: ${dettagli.eArtiInf} • Edema: ${dettagli.iArtiInf}\nLichenificazione: ${dettagli.lArtiInf} • Escoriazione: ${dettagli.escoriazioneArtiInf}\nArea: ${convertiArea(dettagli.aArtiInf)}")
                    } else {
                        Toast.makeText(requireContext(), "Dati di dettaglio EASI non trovati!", Toast.LENGTH_SHORT).show()
                    }
                }
                "PASI" -> {
                    iniettaCardClinica(dynamicContainer, "DETTAGLIO PASI", "Visualizzazione parametri specifici PASI in fase di sviluppo.")
                }
                "BMI" -> {
                    iniettaCardClinica(dynamicContainer, "BODY MASS INDEX (BMI)", "Calcolo eseguito in base a peso e altezza del paziente.")
                }
                "BSA" -> {
                    iniettaCardClinica(dynamicContainer, "BODY SURFACE AREA (BSA)", "Visualizzazione della superficie corporea totale interessata.")
                }
                else -> {
                    iniettaCardClinica(dynamicContainer, "ERRORE", "Tipologia indice sconosciuta o non supportata.")
                }
            }
        }

        // 2. Logica del bottone Elimina
        btnDeleteAssessment.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                db.DermCalcDao().cancellaValutazionePerId(idValutazione)
                Toast.makeText(requireContext(), "Valutazione eliminata", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun iniettaCardClinica(container: LinearLayout, titoloSezione: String, corpoDati: String) {
        val cardView = layoutInflater.inflate(R.layout.item_detail_report, container, false)
        cardView.findViewById<TextView>(R.id.txtSectionTitle).text = titoloSezione
        cardView.findViewById<TextView>(R.id.txtSectionBody).text = corpoDati
        container.addView(cardView)
    }

    private fun convertiArea(pos: Int): String {
        return when (pos) {
            1 -> "1-9%"
            2 -> "10-29%"
            3 -> "30-49%"
            4 -> "50-69%"
            5 -> "70-89%"
            6 -> "90-100%"
            else -> "0% (Esentata)"
        }
    }
}