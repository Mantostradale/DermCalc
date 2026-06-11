package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Valutazione
import com.example.dermcalc.data.local.entity.DatiBMI
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class BMIFragment : Fragment(R.layout.fragment_index_bmi) {

    private var idDottoreLoggato: Long? = null
    private var pazienteSelezionato: Paziente? = null
    private var listaPazientiOrdinata: List<Paziente> = emptyList()
    private var punteggioFinaleCalcolato: Double? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardPatientSelector = view.findViewById<MaterialCardView>(R.id.cardPatientSelector)
        val txtPatientName = view.findViewById<TextView>(R.id.txtPatientName)
        val spinnerPazienti = view.findViewById<Spinner>(R.id.spinnerPazientiNascosto)

        val etWeight = view.findViewById<EditText>(R.id.etWeight) // in kg
        val etHeight = view.findViewById<EditText>(R.id.etHeight) // in cm

        val btnCalculateBmi = view.findViewById<Button>(R.id.btnCalculateBmi)
        val btnSaveAssessment = view.findViewById<Button>(R.id.btnSaveAssessment)
        val txtBmiResultScore = view.findViewById<TextView>(R.id.txtBmiResultScore)
        val txtBmiResultSeverity = view.findViewById<TextView>(R.id.txtBmiResultSeverity)

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
                spinnerPazienti.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    elencoNomi
                )
            }
        }

        cardPatientSelector.setOnClickListener {
            if (listaPazientiOrdinata.isEmpty()) {
                Toast.makeText(requireContext(), "Nessun paziente assegnato!", Toast.LENGTH_SHORT).show()
            } else {
                spinnerPazienti.performClick()
            }
        }

        spinnerPazienti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                pazienteSelezionato = listaPazientiOrdinata[position]
                txtPatientName.text = "${pazienteSelezionato!!.cognome} ${pazienteSelezionato!!.nome}"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCalculateBmi.setOnClickListener {
            val weightStr = etWeight.text.toString()
            val heightStr = etHeight.text.toString()

            if (weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci peso e altezza!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val peso = weightStr.toDouble()
            val altezzaMetri = heightStr.toDouble() / 100.0 // Converte cm in metri

            if (altezzaMetri == 0.0) return@setOnClickListener

            val bmi = peso / (altezzaMetri * altezzaMetri)
            val formattedScore = String.format(Locale.US, "%.1f", bmi) //aggiunge i decimali a un numero intero
            punteggioFinaleCalcolato = formattedScore.toDouble()

            txtBmiResultScore.text = formattedScore
            txtBmiResultSeverity.text = getBmiCategory(bmi)
        }

        btnSaveAssessment.setOnClickListener {
            val paziente = pazienteSelezionato
            val punteggio = punteggioFinaleCalcolato

            if (paziente == null) {
                Toast.makeText(requireContext(), "Seleziona prima un paziente!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (punteggio == null) {
                Toast.makeText(requireContext(), "Clicca su 'Calcola' prima di salvare!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val weightStr = etWeight.text.toString()
                val heightStr = etHeight.text.toString()

                if (weightStr.isEmpty() || heightStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Dati incompleti per il salvataggio!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val dataFormattata = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date())

                val nuovaValutazione = Valutazione(
                    valutazioneId = 0,
                    pazienteIdVisitato = paziente.pazienteId,
                    personaleIdResponsabile = idDottoreLoggato!!,
                    dataValutazione = dataFormattata,
                    tipologiaIndice = "BMI",
                    punteggioFinale = punteggio
                )

                val idInserito = db.DermCalcDao().inserisciValutazione(nuovaValutazione)

                if (idInserito > 0) {
                    val datiBMI = DatiBMI(
                        valutazioneId = idInserito,
                        altezza = heightStr.toDouble(),
                        peso = weightStr.toDouble()
                    )

                    db.DermCalcDao().inserisciDatiBMI(datiBMI)

                    Toast.makeText(requireContext(), "dati BMI salvati!", Toast.LENGTH_SHORT).show()

                    punteggioFinaleCalcolato = null
                    txtBmiResultScore.text = "0.0"
                    txtBmiResultSeverity.text = "-"
                    etWeight.text.clear()
                    etHeight.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Errore durante il salvataggio!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Sottopeso"
            bmi < 25.0 -> "Normopeso"
            bmi < 30.0 -> "Sovrappeso"
            else -> "Obesità"
        }
    }
}