package com.example.dermcalc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.dermcalc.data.local.database.AppDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val db = AppDatabase.getDatabase(this)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Errore: Inserisci sia l'Username che la Password!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val dottoreIdTrovato = db.DermCalcDao().verificaLogin(username, password)

                if (dottoreIdTrovato != null) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("DOCTOR_ID_RESULT", dottoreIdTrovato.personaleId as Long)
                    resultIntent.putExtra("DOCTOR_SURNAME_RESULT", dottoreIdTrovato.cognome) //per il label di benvenuto

                    setResult(Activity.RESULT_OK, resultIntent)
                    Toast.makeText(this@LoginActivity, "Accesso eseguito!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Errore: Credenziali errate. Nessun medico trovato con questo account!",
                        Toast.LENGTH_LONG
                    ).show()
                    etPassword.setText("")
                    etUsername.setText("")
                }
            }
        }
    }
}