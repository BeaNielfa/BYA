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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.airbnb.lottie.LottieAnimationView
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.example.bya.clases.Favorito
import com.example.bya.clases.Prenda
import com.example.bya.ui.favoritos.FavoritosFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*


class CatalogoUsuarioDetalleFragment (private val p: Prenda, private val tipo: Int) : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var tvNombre: TextView
    private lateinit var tvPrecio: TextView
    private lateinit var tvStock: TextView
    private lateinit var imgCamiseta: ImageView
    private lateinit var btnCesta : Button
    private lateinit var imgFav : LottieAnimationView
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

        //Enlazamos los elementos con el diseño
        val imgX : ImageView = root.findViewById(R.id.imgDetalleCerrar)
        btnCesta = root.findViewById(R.id.btnDetalleAnadirCesta)
        imgCamiseta = root.findViewById(R.id.imgDetalleFoto)
        tvNombre = root.findViewById(R.id.tvDetalleNombre)
        tvPrecio = root.findViewById(R.id.tvDetallePrecio)
        tvStock = root.findViewById(R.id.tvDetalleStock)
        val radioS : RadioButton = root.findViewById(R.id.radioDetalleS)
        val radioM : RadioButton = root.findViewById(R.id.radioDetalleM)
        val radioGroup : RadioGroup = root.findViewById(R.id.radioGroup)
        imgFav = root.findViewById(R.id.imgDetalleFav)

        //Recogemos el idUsuario del usuario activo
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()


        /**
         * Al pulsar en la X
         */
        imgX.setOnClickListener {

            if(tipo == 0){//Si es 0 volvemos al catalogo
                requireActivity().supportFragmentManager.popBackStack("catalogo", FragmentManager.POP_BACK_STACK_INCLUSIVE)

            }else{//si no, volvemos a los favoritos
                requireActivity().supportFragmentManager.popBackStack("favoritos", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

        }

        /**
         * Al pulsar en el boton de añadir a la cesta
         */
        btnCesta.setOnClickListener {

            idRadio = radioGroup.checkedRadioButtonId//recogemos  el id del radioGroup

            if (idRadio == -1){//Si es -1, no ha seleccionado nada
                Toast.makeText(requireContext(), "¡Introduzca la talla!", Toast.LENGTH_SHORT).show()
            } else {

                //recogemos el valor de la talla seleccionada
                if (radioS.isChecked){
                    talla = "S"
                } else if(radioM.isChecked){
                    talla = "M"
                } else {
                    talla = "L"
                }

                //Añadimos la cesta en la bbdd
                val idCesta = UUID.randomUUID().toString()
                val c = Cesta (idCesta, idUsuario, p.idPrenda, talla)
                db.collection("cesta").document(idCesta).set(c)
                Toast.makeText(requireContext(), "Se ha añadido a la cesta", Toast.LENGTH_SHORT).show()

            }

        }

        /**
         * Al pulsar en el corazon de favoritos
         */
        imgFav.setOnClickListener {

            //Recogemos el tag de la imagen (corazon) de favoritos
            var idFoto = imgFav.tag
            var idFavorito = idUsuario + p.idPrenda//Creamos un id al favorito


            if(idFoto == R.drawable.twitter_like_rojo){//si idFoto esta en rojo
                imgFav.setImageResource(R.drawable.twitter_like)//cambiamos el icono
                imgFav.setTag(R.drawable.twitter_like)//cambiamos el tag
                db.collection("favoritos").document(idFavorito).delete()//Eliminamos de la bbdd el favorito

            } else {
                imgFav.setAnimation(R.raw.black_joy)//Asignamos la animacion
                imgFav.playAnimation()//La reproducimos
                imgFav.setTag(R.drawable.twitter_like_rojo)//Cambiamos el tag

                //Añadimos el favorito a la bbdd
                val f = Favorito (idFavorito,idUsuario,p.idPrenda)
                db.collection("favoritos").document(idFavorito).set(f)

            }
        }

        //Rellenamos los campos
        rellenarCampos()


        return root
    }

    /**
     * Metodo que rellena los campos de una prenda
     */
    private fun rellenarCampos() {

        //Consultamos los favoritos del usuario activo, para marcar los corazones
        db.collection("favoritos")
            .whereEqualTo("idUsuario",  idUsuario)
            .whereEqualTo("idPrenda", p.idPrenda )
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {
                    imgFav.setImageResource(R.drawable.twitter_like_rojo)
                    imgFav.setTag(R.drawable.twitter_like_rojo)

                }
            }

        //Rellenamos los campos de la prenda
        Picasso.get().load(Uri.parse(p.foto)).into(imgCamiseta)
        tvNombre.setText(p.nombre)
        tvPrecio.setText(p.precio + " EUR")

        //Comprobamos si hay stock para indicarlo en el detalle
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