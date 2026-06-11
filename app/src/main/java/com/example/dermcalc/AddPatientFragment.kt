package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import kotlinx.coroutines.launch

class AddPatientFragment : Fragment(R.layout.fragment_add_patient) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupero dinamico dall'activity senza passare per gli argomenti del NavGraph
        val activityGuscio = activity as? PatientsActivity
        val idDottore = activityGuscio?.idDottoreLoggato ?: -1L

        val edtNome = view.findViewById<EditText>(R.id.edtNome)
        val edtCognome = view.findViewById<EditText>(R.id.edtCognome)
        val edtCF = view.findViewById<EditText>(R.id.edtCF)
        val btnAnnulla = view.findViewById<Button>(R.id.btnAnnulla)
        val btnSalva = view.findViewById<Button>(R.id.btnSalva)

        val db = AppDatabase.getDatabase(requireContext())

        btnAnnulla.setOnClickListener {
            findNavController().navigateUp()
        }

        btnSalva.setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val cognome = edtCognome.text.toString().trim()
            val cf = edtCF.text.toString().trim().uppercase()

            if (nome.isEmpty() || cognome.isEmpty() || cf.isEmpty()) {
                Toast.makeText(requireContext(), "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            } else if (idDottore == -1L) {
                Toast.makeText(requireContext(), "Errore: Sessione medico non valida", Toast.LENGTH_SHORT).show()
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    val nuovoPaziente = Paziente(
                        personaleIdResponsabile = idDottore,
                        nome = nome,
                        cognome = cognome,
                        dataNascita = "",
                        codiceFiscale = cf
                    )
                    db.DermCalcDao().inserisciPaziente(nuovoPaziente)
                    Toast.makeText(requireContext(), "Paziente $nome $cognome salvato!", Toast.LENGTH_SHORT).show()

                    findNavController().navigateUp()
                }
            }
        }
    }
}