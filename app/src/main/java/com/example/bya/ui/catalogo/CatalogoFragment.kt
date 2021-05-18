package com.example.bya.ui.catalogo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo, container, false)

        val anadirPrenda : FloatingActionButton = root.findViewById(R.id.fabCatalogoAnadir)

        anadirPrenda.setOnClickListener {
            anadirPrenda.hide()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.fragmentCatalogo, AnadirPrendaFragment())
            transaction.addToBackStack(null)
            transaction.commit()


        }


        return root
    }

   
}