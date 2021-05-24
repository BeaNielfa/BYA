package com.example.bya.ui.catalogoUsuario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R



class CatalogoUsuarioFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo_usuario, container, false)

        val imgHombre : ImageView = root.findViewById(R.id.imgCatalogoUsuarioHombre)
        val imgMujer : ImageView = root.findViewById(R.id.imgCatalogoUsuarioMujer)
        val tvHombre : TextView = root.findViewById(R.id.tvCatalogoUsuarioHombre)
        val tvMujer : TextView = root.findViewById(R.id.tvCatalogoUsuarioMujer)

        imgHombre.setOnClickListener{
            entrarPrendasHombre()
        }

        tvHombre.setOnClickListener{
            entrarPrendasHombre()
        }

        imgMujer.setOnClickListener{
            entrarPrendasMujer()
        }

        tvMujer.setOnClickListener{
            entrarPrendasMujer()
        }


        return root

    }

    private fun entrarPrendasHombre(){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.catalogoUsuario, CatalogoHombreFragment())
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun entrarPrendasMujer(){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.catalogoUsuario, CatalogoMujerFragment())
        transaction.addToBackStack(null)
        transaction.commit()

    }


}