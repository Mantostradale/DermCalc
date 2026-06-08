package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.DatiEASI
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Valutazione
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class EasiFragment : Fragment(R.layout.fragment_index_easi) {

    private var idDottoreLoggato: Long? = null
    private var pazienteSelezionato: Paziente? = null
    private var listaPazientiOrdinata: List<Paziente> = emptyList()
    private var punteggioFinaleCalcolato: Double? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardPatientSelector = view.findViewById<MaterialCardView>(R.id.cardPatientSelector)
        val txtPatientName = view.findViewById<TextView>(R.id.txtPatientName)
        val spinnerPazienti = view.findViewById<Spinner>(R.id.spinnerPazientiNascosto)
        val btnCalculateEasi = view.findViewById<Button>(R.id.btnCalculateEasi)
        val btnSaveAssessment = view.findViewById<Button>(R.id.btnSaveAssessment)
        val txtEasiResultScore = view.findViewById<TextView>(R.id.txtEasiResultScore)
        val txtEasiResultSeverity = view.findViewById<TextView>(R.id.txtEasiResultSeverity)

        EsenzioniCheckBox(view)

        idDottoreLoggato = activity?.intent?.extras?.get("DOCTOR_ID") as? Long
        if (idDottoreLoggato == null) {
            Toast.makeText(requireContext(), "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            db.DermCalcDao().getPazientiDelResponsabile(idDottoreLoggato!!).collect { pazienti ->
                listaPazientiOrdinata = pazienti
                val elencoNomi = pazienti.map { "${it.cognome} ${it.nome} (${it.codiceFiscale})" }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    elencoNomi
                )
                spinnerPazienti.adapter = adapter
            }
        }

        cardPatientSelector.setOnClickListener {
            if (listaPazientiOrdinata.isEmpty()) {
                Toast.makeText(requireContext(), "Nessun paziente assegnato a questo medico!", Toast.LENGTH_SHORT).show()
            } else {
                spinnerPazienti.performClick()
            }
        }

        spinnerPazienti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val pazienteScelto = listaPazientiOrdinata[position]
                pazienteSelezionato = pazienteScelto
                txtPatientName.text = "${pazienteScelto.cognome} ${pazienteScelto.nome}"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCalculateEasi.setOnClickListener {
            val headExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkHeadExempt).isChecked
            val trunkExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkTrunkExempt).isChecked
            val upperExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkUpperExempt).isChecked
            val lowerExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkLowerExempt).isChecked

            val ptHead = calcolaDistretto(
                view.findViewById(R.id.spinnerHeadErythema), view.findViewById(R.id.spinnerHeadInduration),
                view.findViewById(R.id.spinnerHeadLichenification), view.findViewById(R.id.spinnerHeadExcoriation),
                view.findViewById(R.id.spinnerHeadArea), headExempt
            ) * 0.1

            val ptTrunk = calcolaDistretto(
                view.findViewById(R.id.spinnerTrunkErythema), view.findViewById(R.id.spinnerTrunkInduration),
                view.findViewById(R.id.spinnerTrunkLichenification), view.findViewById(R.id.spinnerTrunkExcoriation),
                view.findViewById(R.id.spinnerTrunkArea), trunkExempt
            ) * 0.3

            val ptUpper = calcolaDistretto(
                view.findViewById(R.id.spinnerUpperErythema), view.findViewById(R.id.spinnerUpperInduration),
                view.findViewById(R.id.spinnerUpperLichenification), view.findViewById(R.id.spinnerUpperExcoriation),
                view.findViewById(R.id.spinnerUpperArea), upperExempt
            ) * 0.2

            val ptLower = calcolaDistretto(
                view.findViewById(R.id.spinnerLowerErythema), view.findViewById(R.id.spinnerLowerInduration),
                view.findViewById(R.id.spinnerLowerLichenification), view.findViewById(R.id.spinnerLowerExcoriation),
                view.findViewById(R.id.spinnerLowerArea), lowerExempt
            ) * 0.4

            val totaleEasi = ptHead + ptTrunk + ptUpper + ptLower
            val formattedScore = String.format(Locale.US, "%.1f", totaleEasi)
            punteggioFinaleCalcolato = formattedScore.toDouble()

            txtEasiResultScore.text = punteggioFinaleCalcolato.toString()
            txtEasiResultSeverity.text = when {
                totaleEasi == 0.0 -> "Assente"
                totaleEasi <= 1.0 -> "Lieve"
                totaleEasi <= 7.0 -> "Moderato"
                else -> "Grave"
            }
        }

        btnSaveAssessment.setOnClickListener {
            if (pazienteSelezionato == null) {
                Toast.makeText(requireContext(), "Errore: Seleziona prima un paziente!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (punteggioFinaleCalcolato == null) {
                Toast.makeText(requireContext(), "Clicca su 'Calcola' prima di salvare!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val dataFormattata = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())

                val nuovaValutazione = Valutazione(
                    valutazioneId = 0,
                    pazienteIdVisitato = pazienteSelezionato!!.pazienteId,
                    personaleIdResponsabile = idDottoreLoggato!!,
                    dataValutazione = dataFormattata,
                    tipologiaIndice = "EASI",
                    punteggioFinale = punteggioFinaleCalcolato!!
                )

                val nuovaValutazioneId = db.DermCalcDao().inserisciValutazione(nuovaValutazione)

                if (nuovaValutazioneId > 0) {
                    val headExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkHeadExempt).isChecked
                    val trunkExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkTrunkExempt).isChecked
                    val upperExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkUpperExempt).isChecked
                    val lowerExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkLowerExempt).isChecked

                    fun getSpinnerVal(id: Int, isExempt: Boolean): Int {
                        return if (isExempt) 0 else view.findViewById<Spinner>(id).selectedItemPosition
                    }

                    val dettagliEasi = DatiEASI(
                        valutazioneId = nuovaValutazioneId,
                        eTesta = getSpinnerVal(R.id.spinnerHeadErythema, headExempt),
                        iTesta = getSpinnerVal(R.id.spinnerHeadInduration, headExempt),
                        lTesta = getSpinnerVal(R.id.spinnerHeadLichenification, headExempt),
                        escoriazioneTesta = getSpinnerVal(R.id.spinnerHeadExcoriation, headExempt),
                        aTesta = getSpinnerVal(R.id.spinnerHeadArea, headExempt),
                        eArtiSup = getSpinnerVal(R.id.spinnerUpperErythema, upperExempt),
                        iArtiSup = getSpinnerVal(R.id.spinnerUpperInduration, upperExempt),
                        lArtiSup = getSpinnerVal(R.id.spinnerUpperLichenification, upperExempt),
                        escoriazioneArtiSup = getSpinnerVal(R.id.spinnerUpperExcoriation, upperExempt),
                        aArtiSup = getSpinnerVal(R.id.spinnerUpperArea, upperExempt),
                        eTronco = getSpinnerVal(R.id.spinnerTrunkErythema, trunkExempt),
                        iTronco = getSpinnerVal(R.id.spinnerTrunkInduration, trunkExempt),
                        lTronco = getSpinnerVal(R.id.spinnerTrunkLichenification, trunkExempt),
                        escoriazioneTronco = getSpinnerVal(R.id.spinnerTrunkExcoriation, trunkExempt),
                        aTronco = getSpinnerVal(R.id.spinnerTrunkArea, trunkExempt),
                        eArtiInf = getSpinnerVal(R.id.spinnerLowerErythema, lowerExempt),
                        iArtiInf = getSpinnerVal(R.id.spinnerLowerInduration, lowerExempt),
                        lArtiInf = getSpinnerVal(R.id.spinnerLowerLichenification, lowerExempt),
                        escoriazioneArtiInf = getSpinnerVal(R.id.spinnerLowerExcoriation, lowerExempt),
                        aArtiInf = getSpinnerVal(R.id.spinnerLowerArea, lowerExempt)
                    )

                    db.DermCalcDao().inserisciDatiEasi(dettagliEasi)

                    Toast.makeText(requireContext(), "Valutazione e dettagli EASI salvati nel DB!", Toast.LENGTH_LONG).show()

                    punteggioFinaleCalcolato = null
                    txtEasiResultScore.text = getString(R.string.easi_result_score_default)
                    txtEasiResultSeverity.text = getString(R.string.easi_result_severity_default)

                } else {
                    Toast.makeText(requireContext(), "Errore durante il salvataggio della Valutazione!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun EsenzioniCheckBox(view: View) {
        val checkHead = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkHeadExempt)
        val layoutHead = view.findViewById<LinearLayout>(R.id.layoutHeadContent)
        val checkTrunk = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkTrunkExempt)
        val layoutTrunk = view.findViewById<LinearLayout>(R.id.layoutTrunkContent)
        val checkUpper = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkUpperExempt)
        val layoutUpper = view.findViewById<LinearLayout>(R.id.layoutUpperContent)
        val checkLower = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkLowerExempt)
        val layoutLower = view.findViewById<LinearLayout>(R.id.layoutLowerContent)

        checkHead.setOnCheckedChangeListener { _, isChecked -> layoutHead.visibility = if (isChecked) View.GONE else View.VISIBLE }
        checkTrunk.setOnCheckedChangeListener { _, isChecked -> layoutTrunk.visibility = if (isChecked) View.GONE else View.VISIBLE }
        checkUpper.setOnCheckedChangeListener { _, isChecked -> layoutUpper.visibility = if (isChecked) View.GONE else View.VISIBLE }
        checkLower.setOnCheckedChangeListener { _, isChecked -> layoutLower.visibility = if (isChecked) View.GONE else View.VISIBLE }
    }

    private fun convertiAreaMoltiplicatore(position: Int): Double {
        return when (position) {
            1 -> 0.1
            2 -> 0.2
            3 -> 0.3
            4 -> 0.4
            5 -> 0.5
            6 -> 0.6
            else -> 0.0
        }
    }

    private fun calcolaDistretto(spinEry: Spinner, spinInd: Spinner, spinLic: Spinner, spinExc: Spinner, spinArea: Spinner, isExempt: Boolean): Double {
        if (isExempt) return 0.0
        val sommaSegni = spinEry.selectedItemPosition + spinInd.selectedItemPosition + spinLic.selectedItemPosition + spinExc.selectedItemPosition
        return sommaSegni * convertiAreaMoltiplicatore(spinArea.selectedItemPosition)
    }
}