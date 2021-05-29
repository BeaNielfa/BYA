package com.example.bya.ui.catalogoUsuario

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.example.bya.clases.Favorito
import com.example.bya.clases.Prenda
import com.example.bya.ui.favoritos.FavoritosFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*


class CatalogoUsuarioDetalleFragment (private val p: Prenda, private val tipo: Int, private val tipoPrenda: String) : Fragment() {

    private lateinit var tvNombre: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvStock: TextView
    private lateinit var imgCamiseta: ImageView
    private lateinit var btnCesta : Button
    private lateinit var imgFav : ImageView

    private var idUsuario = ""
    private var idRadio = -1
    private var talla = ""

    private val db = FirebaseFirestore.getInstance()

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
        val radioGroup : RadioGroup = root.findViewById(R.id.radioGroup)
        imgFav = root.findViewById(R.id.imgDetalleFav)

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()
        Log.e("PERFIL ",idUsuario)

        imgX.setOnClickListener {
            if(tipo == 0){
                Log.e("CERRAR", tipo.toString() +" TIPOO")
                catalogo()
            }else{
                Log.e("CERRAR", tipo.toString() +" TIPOO")
                favoritos()
            }
        }

        btnCesta.setOnClickListener {

            idRadio = radioGroup.checkedRadioButtonId
            if (idRadio == -1){
                Toast.makeText(requireContext(), "Â¡Introduzca la talla!", Toast.LENGTH_SHORT).show()
            } else {



                if (radioS.isChecked){
                    talla = "S"
                } else if(radioM.isChecked){
                    talla = "M"
                } else {
                    talla = "L"
                }

                val idCesta = UUID.randomUUID().toString()

                val c = Cesta (idCesta, idUsuario, p.idPrenda, talla)

                db.collection("cesta").document(idCesta).set(c)
                Toast.makeText(requireContext(), "Se ha añadido a la cesta", Toast.LENGTH_SHORT).show()

            }

        }

        imgFav.setOnClickListener {

            var idFoto = imgFav.tag
            var idFavorito = idUsuario + p.idPrenda

            if(idFoto == R.drawable.ic_heart_rojo){
                imgFav.setImageResource(R.drawable.ic_heart)
                imgFav.setTag(R.drawable.ic_heart)
                db.collection("favoritos").document(idFavorito).delete()

            } else {
                imgFav.setImageResource(R.drawable.ic_heart_rojo)
                imgFav.setTag(R.drawable.ic_heart_rojo)

                val f = Favorito (idFavorito,idUsuario,p.idPrenda)
                db.collection("favoritos").document(idFavorito).set(f)

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

        db.collection("favoritos")
            .whereEqualTo("idUsuario",  idUsuario)
            .whereEqualTo("idPrenda", p.idPrenda )
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    imgFav.setImageResource(R.drawable.ic_heart_rojo)
                    imgFav.setTag(R.drawable.ic_heart_rojo)

                }
            }

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
            btnCesta.setBackgroundColor(resources.getColor(R.color.browser_actions_bg_grey))
            Toast.makeText(requireContext(), "No hay stock", Toast.LENGTH_SHORT).show()
        }

    }


}