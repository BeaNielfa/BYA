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


class CatalogoHombreFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_catalogo_hombre, container, false)

        //Enlazamos los elementos con el diseño
        val imgCamiseta : ImageView = root.findViewById(R.id.imgCatalogoHombreCamiseta)
        val tvCamiseta : TextView = root.findViewById(R.id.tvCatalogoHombreCamiseta)
        val imgCamisa : ImageView = root.findViewById(R.id.imgCatalogoHombreCamisa)
        val tvCamisa : TextView = root.findViewById(R.id.tvCatalogoHombreCamisa)
        val imgAccesorio : ImageView = root.findViewById(R.id.imgCatalogoHombreAccesorio)
        val tvAccesorio : TextView = root.findViewById(R.id.tvCatalogoHombreAccesorio)
        val imgJeans : ImageView = root.findViewById(R.id.imgCatalogoHombreJeans)
        val tvJeans : TextView = root.findViewById(R.id.tvCatalogoHombreJeans)

        /**
         * Al pulsar en la imagen de la camiseta, entraremos en la lista de camisetas
         */
        imgCamiseta.setOnClickListener {
            entrarListaPrendas("6")
        }

        /**
         * Al pulsar en la imagen de la camiseta, entraremos en la lista de camisetas
         */
        tvCamiseta.setOnClickListener {
            entrarListaPrendas("6")
        }

        /**
         * Al pulsar en la imagen de la camisa, entraremos en la lista de camisa
         */
        imgCamisa.setOnClickListener {
            entrarListaPrendas("7")
        }

        /**
         * Al pulsar en la imagen de la camisa, entraremos en la lista de camisa
         */
        tvCamisa.setOnClickListener {
            entrarListaPrendas("7")
        }

        /**
         * Al pulsar en la imagen del accesorio, entraremos en la lista de accesorios
         */
        imgAccesorio.setOnClickListener {
            entrarListaPrendas("8")
        }

        /**
         * Al pulsar en la imagen del accesorio, entraremos en la lista de accesorios
         */
        tvAccesorio.setOnClickListener {
            entrarListaPrendas("8")
        }

        /**
         * Al pulsar en la imagen de los jeans, entraremos en la lista de jeans
         */
        imgJeans.setOnClickListener {
            entrarListaPrendas("9")
        }

        /**
         * Al pulsar en la imagen de los jeans, entraremos en la lista de jeans
         */
        tvJeans.setOnClickListener {
            entrarListaPrendas("9")
        }

        return root
    }


    /**
     * Metodo que nos lleva a una lista de prendas, en función  del tipo que le llegue
     */
    private fun entrarListaPrendas(tipo : String){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.hombreLayout, CatalogoUsuarioPrendasFragment(tipo,1))
        transaction.addToBackStack(null)
        transaction.commit()

    }


}
