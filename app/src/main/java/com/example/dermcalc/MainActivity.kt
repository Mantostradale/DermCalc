package com.example.dermcalc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private var isLogged = false
    private var loggedDoctorId: Long? = null
    private var doctorSurname: String? = null

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            val idRicevuto = data?.extras?.get("DOCTOR_ID_RESULT") as? Long
            val cognomeRicevuto = data?.getStringExtra("DOCTOR_SURNAME_RESULT")

            if (idRicevuto != null) {
                loggedDoctorId = idRicevuto
                isLogged = true
                doctorSurname = cognomeRicevuto
                Toast.makeText(this, "Login verificato! Benvenuto Dottore.", Toast.LENGTH_SHORT).show()

                val txtNomeDottore = findViewById<TextView>(R.id.txtNomeDottoreToolbar)
                if (!cognomeRicevuto.isNullOrEmpty()) {
                    txtNomeDottore.text = cognomeRicevuto
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val deveDisconnettere = intent.getBooleanExtra("COMMAND_LOGOUT", false)
        if (deveDisconnettere) {
            loggedDoctorId = null
            isLogged = false
            doctorSurname = null

            val txtNomeDottore = findViewById<TextView>(R.id.txtNomeDottoreToolbar)
            txtNomeDottore.text = getString(R.string.guest_user)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtNomeDottore = findViewById<TextView>(R.id.txtNomeDottoreToolbar)

        if (savedInstanceState != null) {
            isLogged = savedInstanceState.getBoolean("IS_LOGGED", false)
            doctorSurname = savedInstanceState.getString("DOCTOR_SURNAME")
            if (savedInstanceState.containsKey("LOGGED_DOCTOR_ID")) {
                loggedDoctorId = savedInstanceState.getLong("LOGGED_DOCTOR_ID")
            }

            // Ripristina il testo della toolbar se loggato
            if (isLogged && !doctorSurname.isNullOrEmpty()) {
                txtNomeDottore.text = doctorSurname
            }
        }

        val btnCalcola = findViewById<Button>(R.id.btnCalcola)
        val btnPatientStats = findViewById<Button>(R.id.btnPatientStats)
        val imgProfilo = findViewById<ImageView>(R.id.accountCircle)

        imgProfilo.setOnClickListener {
            if (!isLogged || loggedDoctorId == null) {
                val intentLogin = Intent(this, LoginActivity::class.java)
                loginLauncher.launch(intentLogin)
            } else {
                val intentProfilo = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("DOCTOR_ID", loggedDoctorId)
                }
                startActivity(intentProfilo)
            }
        }

        btnCalcola.setOnClickListener {
            if (!isLogged || loggedDoctorId == null) {
                Toast.makeText(this, "Per poter utilizzare gli strumenti, è necessario essere loggati.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, IndexSelectionActivity::class.java).apply {
                    putExtra("DOCTOR_ID", loggedDoctorId)
                }
                startActivity(intent)
            }
        }

        val btnNavPatients = findViewById<LinearLayout>(R.id.btnNavPatients)
        btnNavPatients.setOnClickListener {
            if (!isLogged || loggedDoctorId == null) {
                Toast.makeText(this, "Per poter utilizzare gli strumenti, è necessario essere loggati.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, PatientsActivity::class.java).apply {
                    putExtra("DOCTOR_ID", loggedDoctorId)
                }
                startActivity(intent)
            }
        }

        val btnReports = findViewById<LinearLayout>(R.id.btnReports)
        btnReports.setOnClickListener {
            if (!isLogged || loggedDoctorId == null) {
                Toast.makeText(this, "Per poter utilizzare gli strumenti, è necessario essere loggati.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, ReportsActivity::class.java).apply {
                    putExtra("DOCTOR_ID", loggedDoctorId)
                }
                startActivity(intent)
            }
        }

        btnPatientStats.setOnClickListener {
            if (!isLogged || loggedDoctorId == null) {
                Toast.makeText(this, "Per poter utilizzare gli strumenti, è necessario essere loggati.", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, PatientStatsActivity::class.java).apply {
                    putExtra("DOCTOR_ID", loggedDoctorId!!)
                    putExtra("PATIENT_ID", 1L)
                }
                startActivity(intent)
            }
        }

        val btnChangeTheme = findViewById<LinearLayout>(R.id.btnChangeTheme)
        btnChangeTheme.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_LOGGED", isLogged)
        outState.putString("DOCTOR_SURNAME", doctorSurname)
        loggedDoctorId?.let { outState.putLong("LOGGED_DOCTOR_ID", it) }
    }
}