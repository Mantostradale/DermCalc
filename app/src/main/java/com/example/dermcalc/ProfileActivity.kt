package com.example.dermcalc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar = findViewById<Toolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)

        val txtCognome = findViewById<TextView>(R.id.txtCognome)
        val txtUsername = findViewById<TextView>(R.id.txtUsername)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        val idDottore: Long? = intent.extras?.get("DOCTOR_ID") as? Long

        if (idDottore == null) {
            Toast.makeText(this, "Errore nel recupero dei dati!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val dottore = db.DermCalcDao().getDottoreById(idDottore)
            if (dottore != null) {
                txtCognome.text = dottore.cognome
                txtUsername.text = dottore.username
            } else {
                Toast.makeText(this@ProfileActivity, "Dottore non trovato!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ProfileActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
            }
        }

        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logout effettuato correttamente.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("COMMAND_LOGOUT", true)
            }
            startActivity(intent)
        }
    }
}