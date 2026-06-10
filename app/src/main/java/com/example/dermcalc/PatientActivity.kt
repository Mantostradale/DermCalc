package com.example.dermcalc

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import kotlinx.coroutines.launch

class PatientActivity : AppCompatActivity() {

    private var idDottoreLoggato: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patients)

        idDottoreLoggato = intent?.extras?.get("DOCTOR_ID") as? Long

        if (idDottoreLoggato == null) {
            Toast.makeText(this, "Errore sessione medico!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val db = AppDatabase.getDatabase(this)
        val containerPazienti = findViewById<LinearLayout>(R.id.containerPazienti)

        val btnAddPatient = findViewById<Button>(R.id.btnAddPatient)
        btnAddPatient.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_patient, null)
            val edtCognome = dialogView.findViewById<EditText>(R.id.edtCognome)
            val edtNome = dialogView.findViewById<EditText>(R.id.edtNome)
            val edtCodiceFiscale = dialogView.findViewById<EditText>(R.id.edtCodiceFiscale)

            AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Salva") { dialog, _ ->
                    val cognome = edtCognome.text.toString().trim()
                    val nome = edtNome.text.toString().trim()
                    val codiceFiscale = edtCodiceFiscale.text.toString().trim().uppercase()

                    if (cognome.isNotEmpty() && nome.isNotEmpty() && codiceFiscale.isNotEmpty()) {
                        lifecycleScope.launch {

                            val nuovoPaziente = Paziente(
                                pazienteId = 0,
                                personaleIdResponsabile = idDottoreLoggato!!,
                                nome = nome,
                                cognome = cognome,
                                codiceFiscale = codiceFiscale,
                                dataNascita = ""
                            )

                            db.DermCalcDao().inserisciPaziente(nuovoPaziente)

                            Toast.makeText(this@PatientActivity, "Paziente salvato con successo!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PatientActivity, "Compila tutti i campi!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Annulla") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        lifecycleScope.launch {
            db.DermCalcDao().getPazientiDelResponsabile(idDottoreLoggato!!).collect { pazienti ->
                containerPazienti.removeAllViews()

                pazienti.forEach { paziente ->
                    val context = this@PatientActivity

                    val rowLayout = RelativeLayout(context).apply {
                        val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

                        background = GradientDrawable().apply {
                            setColor(Color.WHITE)
                            cornerRadius = (8 * context.resources.displayMetrics.density)
                        }
                        elevation = 4f

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.setMargins(0, 0, 0, (12 * context.resources.displayMetrics.density).toInt())
                        this.layoutParams = layoutParams
                    }

                    val txtName = TextView(context).apply {
                        text = "${paziente.cognome} ${paziente.nome} (${paziente.codiceFiscale})"
                        textSize = 16f
                        setTextColor(Color.BLACK)
                        setTypeface(null, Typeface.BOLD)
                    }
                    val textParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                        addRule(RelativeLayout.LEFT_OF, android.R.id.button1)
                    }
                    rowLayout.addView(txtName, textParams)

                    val btnDelete = ImageButton(context).apply {
                        id = android.R.id.button1
                        setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                        setBackgroundColor(Color.TRANSPARENT)
                        setColorFilter(Color.parseColor("#D32F2F"))

                        setOnClickListener {
                            AlertDialog.Builder(context)
                                .setTitle("Elimina Paziente")
                                .setMessage("Sei sicuro di voler eliminare ${paziente.nome} ${paziente.cognome}?")
                                .setPositiveButton("Sì, Elimina") { dialog, _ ->
                                    lifecycleScope.launch {
                                        db.DermCalcDao().rimuoviPaziente(paziente)
                                        Toast.makeText(context, "Paziente rimosso", Toast.LENGTH_SHORT).show()
                                    }
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                    }
                    val btnParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        addRule(RelativeLayout.CENTER_VERTICAL)
                    }
                    rowLayout.addView(btnDelete, btnParams)

                    containerPazienti.addView(rowLayout)
                }
            }
        }
    }
}