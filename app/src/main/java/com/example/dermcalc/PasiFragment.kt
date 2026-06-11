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
import com.example.dermcalc.data.local.entity.DatiPASI
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Valutazione
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class PasiFragment : Fragment(R.layout.fragment_index_pasi) {

    private var idDottoreLoggato: Long? = null
    private var pazienteSelezionato: Paziente? = null
    private var listaPazientiOrdinata: List<Paziente> = emptyList()
    private var punteggioFinaleCalcolato: Double? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardPatientSelector = view.findViewById<MaterialCardView>(R.id.cardPatientSelector)
        val txtPatientName = view.findViewById<TextView>(R.id.txtPatientName)
        val spinnerPazienti = view.findViewById<Spinner>(R.id.spinnerPazientiNascosto)
        val btnCalculatePasi = view.findViewById<Button>(R.id.btnCalculatePasi)
        val btnSaveAssessment = view.findViewById<Button>(R.id.btnSaveAssessment)
        val txtPasiResultScore = view.findViewById<TextView>(R.id.txtPasiResultScore)
        val txtPasiResultSeverity = view.findViewById<TextView>(R.id.txtPasiResultSeverity)

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

        btnCalculatePasi.setOnClickListener {
            val headExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkHeadExempt).isChecked
            val trunkExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkTrunkExempt).isChecked
            val upperExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkUpperExempt).isChecked
            val lowerExempt = view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkLowerExempt).isChecked

            val ptHead = calcolaDistretto(
                view.findViewById(R.id.spinnerHeadErythema),
                view.findViewById(R.id.spinnerHeadInduration),
                view.findViewById(R.id.spinnerHeadDesquamation),
                view.findViewById(R.id.spinnerHeadArea),
                headExempt,
                0.1
            )

            val ptTrunk = calcolaDistretto(
                view.findViewById(R.id.spinnerTrunkErythema),
                view.findViewById(R.id.spinnerTrunkInduration),
                view.findViewById(R.id.spinnerTrunkDesquamation),
                view.findViewById(R.id.spinnerTrunkArea),
                trunkExempt,
                0.3
            )

            val ptUpper = calcolaDistretto(
                view.findViewById(R.id.spinnerUpperErythema),
                view.findViewById(R.id.spinnerUpperInduration),
                view.findViewById(R.id.spinnerUpperDesquamation),
                view.findViewById(R.id.spinnerUpperArea),
                upperExempt,
                0.2
            )

            val ptLower = calcolaDistretto(
                view.findViewById(R.id.spinnerLowerErythema),
                view.findViewById(R.id.spinnerLowerInduration),
                view.findViewById(R.id.spinnerLowerDesquamation),
                view.findViewById(R.id.spinnerLowerArea),
                lowerExempt,
                0.4
            )

            val totalePasi = ptHead + ptTrunk + ptUpper + ptLower
            val formattedScore = String.format(Locale.US, "%.1f", totalePasi)
            punteggioFinaleCalcolato = formattedScore.toDouble()

            txtPasiResultScore.text = formattedScore

            txtPasiResultSeverity.text = when {
                totalePasi == 0.0 -> "Assente"
                totalePasi < 5.0 -> "Lieve"
                totalePasi <= 10.0 -> "Moderato"
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
                    tipologiaIndice = "PASI",
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

                    val dettagliPasi = DatiPASI(
                        valutazioneId = nuovaValutazioneId,
                        eTesta = getSpinnerVal(R.id.spinnerHeadErythema, headExempt),
                        iTesta = getSpinnerVal(R.id.spinnerHeadInduration, headExempt),
                        dTesta = getSpinnerVal(R.id.spinnerHeadDesquamation, headExempt),
                        aTesta = getSpinnerVal(R.id.spinnerHeadArea, headExempt),
                        eArtiSup = getSpinnerVal(R.id.spinnerUpperErythema, upperExempt),
                        iArtiSup = getSpinnerVal(R.id.spinnerUpperInduration, upperExempt),
                        dArtiSup = getSpinnerVal(R.id.spinnerUpperDesquamation, upperExempt),
                        aArtiSup = getSpinnerVal(R.id.spinnerUpperArea, upperExempt),
                        eTronco = getSpinnerVal(R.id.spinnerTrunkErythema, trunkExempt),
                        iTronco = getSpinnerVal(R.id.spinnerTrunkInduration, trunkExempt),
                        dTronco = getSpinnerVal(R.id.spinnerTrunkDesquamation, trunkExempt),
                        aTronco = getSpinnerVal(R.id.spinnerTrunkArea, trunkExempt),
                        eArtiInf = getSpinnerVal(R.id.spinnerLowerErythema, lowerExempt),
                        iArtiInf = getSpinnerVal(R.id.spinnerLowerInduration, lowerExempt),
                        dArtiInf = getSpinnerVal(R.id.spinnerLowerDesquamation, lowerExempt),
                        aArtiInf = getSpinnerVal(R.id.spinnerLowerArea, lowerExempt)
                    )

                    db.DermCalcDao().inserisciDatiPasi(dettagliPasi)

                    Toast.makeText(requireContext(), "Valutazione e dettagli PASI salvati nel DB!", Toast.LENGTH_LONG).show()

                    // Reset dei campi
                    punteggioFinaleCalcolato = null
                    txtPasiResultScore.text = "0.0"
                    txtPasiResultSeverity.text = "Lieve"

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

        checkHead.setOnCheckedChangeListener { _, isChecked ->
            layoutHead.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        checkTrunk.setOnCheckedChangeListener { _, isChecked ->
            layoutTrunk.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        checkUpper.setOnCheckedChangeListener { _, isChecked ->
            layoutUpper.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
        checkLower.setOnCheckedChangeListener { _, isChecked ->
            layoutLower.visibility = if (isChecked) View.GONE else View.VISIBLE
        }
    }

    private fun calcolaDistretto(
        spinEry: Spinner,
        spinInd: Spinner,
        spinDesq: Spinner,
        spinArea: Spinner,
        isExempt: Boolean,
        pesoAnatomico: Double
    ): Double {
        if (isExempt) return 0.0

        val eritema = spinEry.selectedItemPosition
        val infiltrazione = spinInd.selectedItemPosition
        val desquamazione = spinDesq.selectedItemPosition
        val areaPunteggio = spinArea.selectedItemPosition
        val sommaSegni = eritema + infiltrazione + desquamazione
        return areaPunteggio * sommaSegni * pesoAnatomico
    }
}