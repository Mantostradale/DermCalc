package com.example.dermcalc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment

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

        // 1. Recuperiamo il NavHostFragment
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.reportsFragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. 🚀 TRUCCO MAGICO: Forziamo il grafico e la destinazione corretta via codice
        val navInflater = navController.navInflater
        val grafico = navInflater.inflate(R.navigation.nav_graph)

        // Specifichiamo a schermo che questa Activity deve PARTIRE dai report, non dalla selezione indici
        grafico.setStartDestination(R.id.reportsFragment)

        // Applichiamo il grafico configurato al controller
        navController.graph = grafico

        // 3. Gestione del tasto indietro sulla Toolbar
        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        toolbar.setNavigationOnClickListener {
            if (!navController.navigateUp()) {
                finish()
            }
        }
    }
}