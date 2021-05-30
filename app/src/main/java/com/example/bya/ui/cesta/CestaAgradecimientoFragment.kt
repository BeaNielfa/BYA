package com.example.bya.ui.cesta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.ui.catalogoUsuario.CatalogoUsuarioFragment

class CestaAgradecimientoFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_cesta_agradecimiento, container, false)

        val btnVolver : Button = root.findViewById(R.id.btnCestaAgradecimientosVolver)

        btnVolver.setOnClickListener {
            btnVolver.visibility = View.INVISIBLE
            btnVolver.isClickable = false
            catalogo()
        }

        return root
    }

    private fun catalogo (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaAgradecimientoLayout, CatalogoUsuarioFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }


}