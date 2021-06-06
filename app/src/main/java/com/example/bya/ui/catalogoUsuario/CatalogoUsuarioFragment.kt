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

        //Enlazamos los elementos con el diseño
        val imgHombre : ImageView = root.findViewById(R.id.imgCatalogoUsuarioHombre)
        val imgMujer : ImageView = root.findViewById(R.id.imgCatalogoUsuarioMujer)
        val tvHombre : TextView = root.findViewById(R.id.tvCatalogoUsuarioHombre)
        val tvMujer : TextView = root.findViewById(R.id.tvCatalogoUsuarioMujer)

        /**
         * Al pulsar en la imagen de hombre, entraremos en la lista para elegir el tipo de prenda de hombre
         */
        imgHombre.setOnClickListener{
            entrarPrendasHombre()
        }

        /**
         * Al pulsar en el texto de hombre, entraremos en la lista para elegir el tipo de prenda de hombre
         */
        tvHombre.setOnClickListener{
            entrarPrendasHombre()
        }

        /**
         * Al pulsar en la imagen de la mujer, entraremos en la lista para elegir el tipo de prenda de la mujer
         */
        imgMujer.setOnClickListener{
            entrarPrendasMujer()
        }

        /**
         * Al pulsar en la texto de la mujer, entraremos en la lista para elegir el tipo de prenda de la mujer
         */
        tvMujer.setOnClickListener{
            entrarPrendasMujer()
        }


        return root

    }

    /**
     * Metodo que nos lleva al fragment donde elegir el tipo de prenda que buscamos de hombre
     */
    private fun entrarPrendasHombre(){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.catalogoUsuario, CatalogoHombreFragment())
        transaction.addToBackStack(null)
        transaction.commit()

    }

    /**
     * Metodo que nos lleva al fragment donde elegir el tipo de prenda que buscamos de mujer
     */
    private fun entrarPrendasMujer(){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.catalogoUsuario, CatalogoMujerFragment())
        transaction.addToBackStack(null)
        transaction.commit()

    }


}