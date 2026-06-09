package com.example.dermcalc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReportsActivity : AppCompatActivity() {

    var idDottoreLoggato: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        idDottoreLoggato = intent.extras?.get("DOCTOR_ID") as? Long

        if (idDottoreLoggato == null) {
            Toast.makeText(this, "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        toolbar.setNavigationOnClickListener {
            // Gestione del backstack per i frammenti
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.reportsFragmentContainer, ReportsFragment())
                .commit()
        }
    }

    fun apriDettaglio(valutazioneId: Long, tipologiaIndice: String) {
        val fragmentDettaglio = ReportsDetailFragment.newInstance(valutazioneId, tipologiaIndice)
        supportFragmentManager.beginTransaction()
            .replace(R.id.reportsFragmentContainer, fragmentDettaglio)
            .addToBackStack(null)
            .commit()
    }
}