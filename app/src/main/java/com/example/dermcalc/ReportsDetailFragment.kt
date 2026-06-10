package com.example.dermcalc

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
import kotlin.Double

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
                        Toast.makeText(requireContext(), "Dati EASI non trovati!", Toast.LENGTH_SHORT).show()
                    }
                }
                "PASI" -> {
                    // 🚀 Recupero dei dati reali PASI dal DB (Assicurati di avere questo metodo nel DAO, es: getDettagliPasiPerId)
                    val dettagli = db.DermCalcDao().getDettagliPasiPerId(idValutazione)
                    if (dettagli != null) {
                        iniettaCardClinica(dynamicContainer, "TESTA (PASI)", "Eritema: ${dettagli.eTesta} • Infiltrazione: ${dettagli.iTesta}\nDesquamazione: ${dettagli.dTesta}\nArea: ${convertiArea(dettagli.aTesta)}")
                        iniettaCardClinica(dynamicContainer, "TRONCO (PASI)", "Eritema: ${dettagli.eTronco} • Infiltrazione: ${dettagli.iTronco}\nDesquamazione: ${dettagli.dTronco}\nArea: ${convertiArea(dettagli.aTronco)}")
                        iniettaCardClinica(dynamicContainer, "ARTI SUPERIORI (PASI)", "Eritema: ${dettagli.eArtiSup} • Infiltrazione: ${dettagli.iArtiSup}\nDesquamazione: ${dettagli.dArtiSup}\nArea: ${convertiArea(dettagli.aArtiSup)}")
                        iniettaCardClinica(dynamicContainer, "ARTI INFERIORI (PASI)", "Eritema: ${dettagli.eArtiInf} • Infiltrazione: ${dettagli.iArtiInf}\nDesquamazione: ${dettagli.dArtiInf}\nArea: ${convertiArea(dettagli.aArtiInf)}")
                    } else {
                        Toast.makeText(requireContext(), "Dati PASI non trovati!", Toast.LENGTH_SHORT).show()
                    }
                }
                "BMI" -> {
                    val bmi = db.DermCalcDao().getDettagliBMIPerId(idValutazione)
                    if (bmi != null) {
                        iniettaCardClinica(dynamicContainer, "DATI BMI", "Altezza: ${bmi.altezza} cm\nPeso: ${bmi.peso} kg")
                    } else {
                        Toast.makeText(requireContext(), "Dati BMI non trovati!", Toast.LENGTH_SHORT).show()
                    }
                }
                "BSA" -> {
                    val bsa = db.DermCalcDao().getDettagliBSAPerId(idValutazione)
                    if (bsa != null) {
                        iniettaCardClinica(dynamicContainer, "TESTA E COLLO (BSA)", "Estensione: ${bsa.testaCollo}%")
                        iniettaCardClinica(dynamicContainer, "TRONCO (BSA)", "Anteriore: ${bsa.troncoAnt}% • Posteriore: ${bsa.troncoPost}%")
                        iniettaCardClinica(dynamicContainer, "ARTI SUPERIORI (BSA)", "Destro: ${bsa.artoSupDx}% • Sinistro: ${bsa.artoSupSx}%")
                        iniettaCardClinica(dynamicContainer, "ARTI INFERIORI (BSA)", "Destro: ${bsa.artoInfDx}% • Sinistro: ${bsa.artoInfSx}%")
                        iniettaCardClinica(dynamicContainer, "GENITALI (BSA)", "Estensione: ${bsa.genitali}%")
                    } else {
                        Toast.makeText(requireContext(), "Dati BSA non trovati!", Toast.LENGTH_SHORT).show()
                    }
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