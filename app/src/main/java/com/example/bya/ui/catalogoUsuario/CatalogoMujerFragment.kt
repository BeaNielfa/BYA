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


class CatalogoMujerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo_mujer, container, false)

        //Enlazamos los elementos con el diseño
        val imgCamiseta : ImageView = root.findViewById(R.id.imgCatalogoMujerCamiseta)
        val tvCamiseta : TextView = root.findViewById(R.id.tvCatalogoMujerCamiseta)
        val imgBlusa : ImageView = root.findViewById(R.id.imgCatalogoMujerBlusa)
        val tvBlusa : TextView = root.findViewById(R.id.tvCatalogoMujerBlusa)
        val imgAccesorio : ImageView = root.findViewById(R.id.imgCatalogoMujerAccesorio)
        val tvAccesorio : TextView = root.findViewById(R.id.tvCatalogoMujerAccesorio)
        val imgJeans : ImageView = root.findViewById(R.id.imgCatalogoMujerJeans)
        val tvJeans : TextView = root.findViewById(R.id.tvCatalogoMujerJeans)
        val imgVestido : ImageView = root.findViewById(R.id.imgCatalogoMujerVestido)
        val tvVestido : TextView = root.findViewById(R.id.tvCatalogoMujerVestido)
        val imgFalda : ImageView = root.findViewById(R.id.imgCatalogoMujerFalda)
        val tvFalda : TextView = root.findViewById(R.id.tvCatalogoMujerFalda)

        /**
         * Al pulsar en la imagen de la camiseta, entraremos en la lista de camisetas
         */
        imgCamiseta.setOnClickListener {
            entrarListaPrendas("0")
        }

        /**
         * Al pulsar en la imagen de la camiseta, entraremos en la lista de camisetas
         */
        tvCamiseta.setOnClickListener {
            entrarListaPrendas("0")
        }


        /**
         * Al pulsar en la imagen de la blusa, entraremos en la lista de blusas
         */
        imgBlusa.setOnClickListener {
            entrarListaPrendas("1")
        }

        /**
         * Al pulsar en la imagen de la blusa, entraremos en la lista de blusas
         */
        tvBlusa.setOnClickListener {
            entrarListaPrendas("1")
        }

        /**
         * Al pulsar en la imagen de los accesorios, entraremos en la lista de accesorios
         */
        imgAccesorio.setOnClickListener {
            entrarListaPrendas("4")
        }

        /**
         * Al pulsar en la imagen de los accesorios, entraremos en la lista de accesorios
         */
        tvAccesorio.setOnClickListener {
            entrarListaPrendas("4")
        }

        /**
         * Al pulsar en la imagen de los jeans, entraremos en la lista de jeans
         */
        imgJeans.setOnClickListener {
            entrarListaPrendas("3")
        }

        /**
         * Al pulsar en la imagen de los jeans, entraremos en la lista de jeans
         */
        tvJeans.setOnClickListener {
            entrarListaPrendas("3")
        }

        /**
         * Al pulsar en la imagen del vestido, entraremos en la lista de vestidos
         */
        imgVestido.setOnClickListener {
            entrarListaPrendas("2")
        }

        /**
         * Al pulsar en la imagen del vestido, entraremos en la lista de vestidos
         */
        tvVestido.setOnClickListener {
            entrarListaPrendas("2")
        }

        /**
         * Al pulsar en la imagen de la falda, entraremos en la lista de faldas
         */
        imgFalda.setOnClickListener {
            entrarListaPrendas("5")
        }

        /**
         * Al pulsar en la imagen de la falda, entraremos en la lista de faldas
         */
        tvFalda.setOnClickListener {
            entrarListaPrendas("5")
        }



        return root
    }

    /**
     * Metodo que nos lleva a una lista de prendas, en función  del tipo que le llegue
     */
    private fun entrarListaPrendas(tipo : String){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.mujerLayout, CatalogoUsuarioPrendasFragment(tipo,0))
        transaction.addToBackStack(null)
        transaction.commit()

    }

}
