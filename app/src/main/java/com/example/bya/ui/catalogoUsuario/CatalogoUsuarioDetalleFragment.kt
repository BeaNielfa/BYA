package com.example.bya.ui.catalogoUsuario

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.example.bya.ui.favoritos.FavoritosFragment
import com.squareup.picasso.Picasso


class CatalogoUsuarioDetalleFragment (private val p: Prenda, private val tipo: Int, private val tipoPrenda: String) : Fragment() {




    private lateinit var tvNombre: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvStock: TextView
    private lateinit var imgCamiseta: ImageView
    private lateinit var btnCesta : Button


    private var talla = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo_usuario_detalle, container, false)

        val imgX : ImageView = root.findViewById(R.id.imgDetalleCerrar)
        btnCesta = root.findViewById(R.id.btnDetalleAnadirCesta)
        imgCamiseta = root.findViewById(R.id.imgDetalleFoto)
        tvNombre = root.findViewById(R.id.tvDetalleNombre)
        tvPrecio = root.findViewById(R.id.tvDetallePrecio)
        tvStock = root.findViewById(R.id.tvDetalleStock)
        val radioS : RadioButton = root.findViewById(R.id.radioDetalleS)
        val radioM : RadioButton = root.findViewById(R.id.radioDetalleM)
        val radioL : RadioButton = root.findViewById(R.id.radioDetalleL)

        imgX.setOnClickListener {
            if(tipo == 0){
                Log.e("CERRAR", tipo.toString() +" TIPOO")
                catalogo()
            }else{
                Log.e("CERRAR", tipo.toString() +" TIPOO")
                favoritos()
            }
        }


        rellenarCampos()


        return root
    }

    private fun favoritos (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.detallePrenda, FavoritosFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun catalogo (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.detallePrenda, CatalogoUsuarioPrendasFragment(tipoPrenda))
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun rellenarCampos() {

        Picasso.get().load(Uri.parse(p.foto)).into(imgCamiseta)
        tvNombre.setText(p.nombre)
        tvPrecio.setText(p.precio + " EUR")

        if(p.stock > 0){
            tvStock.setTextColor(resources.getColor(R.color.verde))
            tvStock.setText("(" + p.stock + ") en stock.")
        } else {
            tvStock.setTextColor(resources.getColor(R.color.rojo))
            tvStock.setText("Sin stock.")
            btnCesta.isEnabled = false
        }

    }


}