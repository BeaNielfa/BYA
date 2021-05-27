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

        imgCamiseta.setOnClickListener {
            entrarListaPrendas("0")
        }

        tvCamiseta.setOnClickListener {
            entrarListaPrendas("0")
        }

        imgBlusa.setOnClickListener {
            entrarListaPrendas("1")
        }

        tvBlusa.setOnClickListener {
            entrarListaPrendas("1")
        }

        imgAccesorio.setOnClickListener {
            entrarListaPrendas("4")
        }

        tvAccesorio.setOnClickListener {
            entrarListaPrendas("4")
        }

        imgJeans.setOnClickListener {
            entrarListaPrendas("3")
        }

        tvJeans.setOnClickListener {
            entrarListaPrendas("3")
        }

        imgVestido.setOnClickListener {
            entrarListaPrendas("2")
        }

        tvVestido.setOnClickListener {
            entrarListaPrendas("2")
        }

        imgFalda.setOnClickListener {
            entrarListaPrendas("5")
        }

        tvFalda.setOnClickListener {
            entrarListaPrendas("5")
        }



        return root
    }


    private fun entrarListaPrendas(tipo : String){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.mujerLayout, CatalogoUsuarioPrendasFragment(tipo))
        transaction.addToBackStack(null)
        transaction.commit()

    }

}
