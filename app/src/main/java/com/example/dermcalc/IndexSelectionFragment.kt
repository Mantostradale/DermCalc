package com.example.dermcalc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class IndexSelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_index_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnPasi).setOnClickListener {
            findNavController().navigate(R.id.action_indexSelection_to_pasi)
        }

        view.findViewById<Button>(R.id.btnEasi).setOnClickListener {
            findNavController().navigate(R.id.action_indexSelection_to_easi)
        }

        view.findViewById<Button>(R.id.btnBmi).setOnClickListener {
            findNavController().navigate(R.id.action_indexSelection_to_bmi)
        }

        view.findViewById<Button>(R.id.btnBsa).setOnClickListener {
            findNavController().navigate(R.id.action_indexSelection_to_bsa)
        }
    }
}