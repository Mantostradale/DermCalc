package com.example.dermcalc

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment

class PatientsActivity : AppCompatActivity() {

    internal var idDottoreLoggato: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)

        idDottoreLoggato = intent.getLongExtra("DOCTOR_ID", -1L)

        if (idDottoreLoggato == -1L) {
            Toast.makeText(this, "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navInflater = navController.navInflater
        val grafico = navInflater.inflate(R.navigation.nav_graph)

        grafico.setStartDestination(R.id.patientsFragment)
        navController.setGraph(grafico, intent.extras)

        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        toolbar.setNavigationOnClickListener {
            if (!navController.navigateUp()) {
                finish()
            }
        }
    }
}