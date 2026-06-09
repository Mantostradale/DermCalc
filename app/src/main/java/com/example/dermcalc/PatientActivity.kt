package com.example.dermcalc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import kotlinx.coroutines.launch

class PatientActivity : AppCompatActivity() {

    private var idDottoreLoggato: Long? = null
    private var listaPazientiOrdinata: List<Paziente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)

        val idDottoreLoggato = intent?.extras?.get("DOCTOR_ID") as? Long

        if (idDottoreLoggato == null) {
            Toast.makeText(this, "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val spinnerPazienti = findViewById<Spinner>(R.id.spinnerPazientiNascosto)

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            db.DermCalcDao().getPazientiDelResponsabile(idDottoreLoggato).collect { pazienti ->
                listaPazientiOrdinata = pazienti

                val elencoNomi = pazienti.map { "${it.cognome} ${it.nome} (${it.codiceFiscale})" }

                val adapter = ArrayAdapter(
                    this@PatientActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    elencoNomi
                )
                spinnerPazienti.adapter = adapter
            }
        }
    }
}