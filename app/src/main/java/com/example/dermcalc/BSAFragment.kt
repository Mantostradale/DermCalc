package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import com.example.dermcalc.data.local.entity.Valutazione
import com.example.dermcalc.data.local.entity.DatiBSA
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class BSAFragment : Fragment(R.layout.fragment_index_bsa) {

    private var idDottoreLoggato: Long? = null
    private var pazienteSelezionato: Paziente? = null
    private var listaPazientiOrdinata: List<Paziente> = emptyList()
    private var punteggioFinaleCalcolato: Double? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardPatientSelector = view.findViewById<MaterialCardView>(R.id.cardPatientSelector)
        val txtPatientName = view.findViewById<TextView>(R.id.txtPatientName)
        val spinnerPazienti = view.findViewById<Spinner>(R.id.spinnerPazientiNascosto)

        val spinnerBsaTesta = view.findViewById<Spinner>(R.id.spinnerBsaTesta)
        val spinnerBsaArtoSupDx = view.findViewById<Spinner>(R.id.spinnerBsaArtoSupDx)
        val spinnerBsaArtoSupSx = view.findViewById<Spinner>(R.id.spinnerBsaArtoSupSx)
        val spinnerBsaTroncoAnt = view.findViewById<Spinner>(R.id.spinnerBsaTroncoAnt)
        val spinnerBsaTroncoPost = view.findViewById<Spinner>(R.id.spinnerBsaTroncoPost)
        val spinnerBsaArtoInfDx = view.findViewById<Spinner>(R.id.spinnerBsaArtoInfDx)
        val spinnerBsaArtoInfSx = view.findViewById<Spinner>(R.id.spinnerBsaArtoInfSx)
        val spinnerBsaGenitali = view.findViewById<Spinner>(R.id.spinnerBsaGenitali)

        val btnCalculateBsa = view.findViewById<Button>(R.id.btnCalculateBsa)
        val btnSaveAssessment = view.findViewById<Button>(R.id.btnSaveAssessment)
        val txtBsaResultScore = view.findViewById<TextView>(R.id.txtBsaResultScore)
        val txtBsaResultSeverity = view.findViewById<TextView>(R.id.txtBsaResultSeverity)

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

        btnCalculateBsa.setOnClickListener {
            val pTesta = convertiIndiceAreaInPercentuale(spinnerBsaTesta.selectedItemPosition)
            val pArtoSupDx = convertiIndiceAreaInPercentuale(spinnerBsaArtoSupDx.selectedItemPosition)
            val pArtoSupSx = convertiIndiceAreaInPercentuale(spinnerBsaArtoSupSx.selectedItemPosition)
            val pTroncoAnt = convertiIndiceAreaInPercentuale(spinnerBsaTroncoAnt.selectedItemPosition)
            val pTroncoPost = convertiIndiceAreaInPercentuale(spinnerBsaTroncoPost.selectedItemPosition)
            val pArtoInfDx = convertiIndiceAreaInPercentuale(spinnerBsaArtoInfDx.selectedItemPosition)
            val pArtoInfSx = convertiIndiceAreaInPercentuale(spinnerBsaArtoInfSx.selectedItemPosition)
            val pGenitali = convertiIndiceGenitaliInPercentuale(spinnerBsaGenitali.selectedItemPosition)

            var bsaTotale = (pTesta / 100.0 * 9.0) +
                    (pArtoSupDx / 100.0 * 9.0) +
                    (pArtoSupSx / 100.0 * 9.0) +
                    (pTroncoAnt / 100.0 * 18.0) +
                    (pTroncoPost / 100.0 * 18.0) +
                    (pArtoInfDx / 100.0 * 18.0) +
                    (pArtoInfSx / 100.0 * 18.0) +
                    (pGenitali / 100.0 * 1.0)

            bsaTotale = Math.round(bsaTotale * 10.0) / 10.0
            if (bsaTotale > 100.0) bsaTotale = 100.0

            val formattedScore = String.format(Locale.US, "%.1f", bsaTotale)
            punteggioFinaleCalcolato = formattedScore.toDouble()

            txtBsaResultScore.text = "$formattedScore%"
            txtBsaResultSeverity.text = getBsaSeverityCategory(bsaTotale)
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
                val dataFormattata = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date())

                val nuovaValutazione = Valutazione(
                    valutazioneId = 0,
                    pazienteIdVisitato = paziente.pazienteId,
                    personaleIdResponsabile = idDottoreLoggato!!,
                    dataValutazione = dataFormattata,
                    tipologiaIndice = "BSA",
                    punteggioFinale = punteggio
                )

                val idInserito = db.DermCalcDao().inserisciValutazione(nuovaValutazione)

                if (idInserito > 0) {
                    val datiBSA = DatiBSA(
                        valutazioneId = idInserito,
                        testaCollo = convertiIndiceAreaInPercentuale(spinnerBsaTesta.selectedItemPosition),
                        artoSupDx = convertiIndiceAreaInPercentuale(spinnerBsaArtoSupDx.selectedItemPosition),
                        artoSupSx = convertiIndiceAreaInPercentuale(spinnerBsaArtoSupSx.selectedItemPosition),
                        troncoAnt = convertiIndiceAreaInPercentuale(spinnerBsaTroncoAnt.selectedItemPosition),
                        troncoPost = convertiIndiceAreaInPercentuale(spinnerBsaTroncoPost.selectedItemPosition),
                        artoInfDx = convertiIndiceAreaInPercentuale(spinnerBsaArtoInfDx.selectedItemPosition),
                        artoInfSx = convertiIndiceAreaInPercentuale(spinnerBsaArtoInfSx.selectedItemPosition),
                        // MODIFICATO: Usa la funzione specifica per i genitali anche per il database
                        genitali = convertiIndiceGenitaliInPercentuale(spinnerBsaGenitali.selectedItemPosition)
                    )

                    db.DermCalcDao().inserisciDatiBSA(datiBSA)

                    Toast.makeText(requireContext(), "Dati BSA salvati con successo!", Toast.LENGTH_SHORT).show()

                    punteggioFinaleCalcolato = null
                    txtBsaResultScore.text = "0.0%"
                    txtBsaResultSeverity.text = "-"

                    spinnerBsaTesta.setSelection(0)
                    spinnerBsaArtoSupDx.setSelection(0)
                    spinnerBsaArtoSupSx.setSelection(0)
                    spinnerBsaTroncoAnt.setSelection(0)
                    spinnerBsaTroncoPost.setSelection(0)
                    spinnerBsaArtoInfDx.setSelection(0)
                    spinnerBsaArtoInfSx.setSelection(0)
                    spinnerBsaGenitali.setSelection(0)
                } else {
                    Toast.makeText(requireContext(), "Errore durante il salvataggio!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertiIndiceAreaInPercentuale(posizione: Int): Double {
        return when (posizione) {
            1 -> 25.0
            2 -> 50.0
            3 -> 75.0
            4 -> 100.0
            else -> 0.0
        }
    }

    private fun convertiIndiceGenitaliInPercentuale(posizione: Int): Double {
        return when (posizione) {
            1 -> 100.0
            else -> 0.0
        }
    }

    private fun getBsaSeverityCategory(bsa: Double): String {
        return when {
            bsa == 0.0 -> "Sano"
            bsa < 3.0 -> "Lieve"
            bsa <= 10.0 -> "Moderato"
            else -> "Grave"
        }
    }
}