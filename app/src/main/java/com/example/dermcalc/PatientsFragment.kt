package com.example.dermcalc

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dermcalc.data.local.database.AppDatabase
import com.example.dermcalc.data.local.entity.Paziente
import kotlinx.coroutines.launch

class PatientsFragment : Fragment(R.layout.fragment_patient) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listViewPatients = view.findViewById<ListView>(R.id.listViewPatients)
        val btnAddPatient = view.findViewById<Button>(R.id.btnAddPatient)

        val activityGuscio = activity as? PatientsActivity
        val idDottore = activityGuscio?.idDottoreLoggato ?: return

        val db = AppDatabase.getDatabase(requireContext())

        // 1. Inizializzazione Adapter con il nuovo layout XML personalizzato
        val listaPazientiLocale = ArrayList<Paziente>()
        val adapter = object : ArrayAdapter<Paziente>(
            requireContext(),
            R.layout.item_patient, // 👈 Usiamo il nuovo layout della riga appena creato
            listaPazientiLocale
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val rowView = convertView ?: layoutInflater.inflate(R.layout.item_patient, parent, false)
                val paziente = getItem(position)!!

                val txtDatiPaziente = rowView.findViewById<TextView>(R.id.txtDatiPaziente)
                val btnEliminaPaziente = rowView.findViewById<ImageView>(R.id.btnEliminaPaziente)

                // Impostiamo i dati testuali nel layout della riga
                txtDatiPaziente.text = "${paziente.nome} ${paziente.cognome}\nCF: ${paziente.codiceFiscale}"

                // 🗑️ Il cestino dell'XML ora è direttamente cliccabile!
                btnEliminaPaziente.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.DermCalcDao().rimuoviPaziente(paziente)
                        Toast.makeText(requireContext(), "Paziente eliminato", Toast.LENGTH_SHORT).show()
                    }
                }

                return rowView
            }

            // Blocca il click sul resto della riga
            override fun isEnabled(position: Int): Boolean {
                return false
            }
        }
        listViewPatients.adapter = adapter

        // 2. Navigazione al form di inserimento
        btnAddPatient.setOnClickListener {
            findNavController().navigate(R.id.action_patients_to_add_patient)
        }

        // 3. Osservazione reattiva e dinamica della lista
        viewLifecycleOwner.lifecycleScope.launch {
            db.DermCalcDao().getPazientiDelResponsabile(idDottore).collect { nuovaLista ->
                adapter.clear()
                adapter.addAll(nuovaLista)
                adapter.notifyDataSetChanged()
            }
        }
    }
}