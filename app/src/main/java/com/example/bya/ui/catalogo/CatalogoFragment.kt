package com.example.bya.ui.catalogo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogoFragment : Fragment() {

    private lateinit var anadirPrenda : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo, container, false)

        anadirPrenda  = root.findViewById(R.id.fabCatalogoAnadir)
        var dialog = Dialog(requireActivity())

        anadirPrenda.setOnClickListener {

            //Abrimos un dialog con las 2 opciones (camara o galeria)
            dialog.setContentView(R.layout.anadir_prenda_existente_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan las imágenes del layout de la cámara (si no se rescatan no funciona)
            var imgExistente: ImageView = dialog.findViewById(R.id.imgPrendaExistente)
            var imgNueva: ImageView = dialog.findViewById(R.id.imgPrendaNueva)
            var tvExistente: TextView = dialog.findViewById(R.id.tvPrendaExistente)
            var tvNueva: TextView = dialog.findViewById(R.id.tvPrendaNueva)

            imgExistente.setOnClickListener(){
                dialog.dismiss()
            }
            imgNueva.setOnClickListener(){
                entrarAnadirPrenda()
                dialog.dismiss()
            }
            tvExistente.setOnClickListener(){

                dialog.dismiss()
            }
            tvNueva.setOnClickListener(){
                entrarAnadirPrenda()
                dialog.dismiss()
            }

            dialog.show()
        }






        return root
    }

    private fun entrarAnadirPrenda(){
        anadirPrenda.hide()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, AnadirPrendaFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }
   
}