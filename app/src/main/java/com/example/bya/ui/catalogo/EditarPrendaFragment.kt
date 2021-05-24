package com.example.bya.ui.catalogo

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentTransaction
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_editar_prenda.*
import kotlinx.android.synthetic.main.fragment_perfil.*
import java.io.IOException
import java.util.*


class EditarPrendaFragment( private val p: Prenda) : Fragment() {


    private lateinit var imgPrenda: ImageView

    private val db = FirebaseFirestore.getInstance()

    //VARIABLES
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null

    private var nombre = ""
    private var precio = ""
    private var photoUrl = ""

    private lateinit var etNombre: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etReferencia: EditText
    private lateinit var etTipo: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_editar_prenda, container, false)

        val imgX: ImageView = root.findViewById(R.id.imgEditarCerrar)
        imgPrenda = root.findViewById(R.id.imgEditarPrendaFoto)
        val btnGuardar: Button = root.findViewById(R.id.btnEditarPrendaGuardarCambios)
        etNombre = root.findViewById(R.id.etEditarPrendaNombre)
        etPrecio = root.findViewById(R.id.etEditarPrendaPrecio)
        etReferencia = root.findViewById(R.id.etEditarPrendaReferencia)
        etTipo = root.findViewById(R.id.etEditarPrendaTipo)

        cargarDatos()

        imgX.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.editarPrenda, CatalogoFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        imgPrenda.setOnClickListener {
            mostrarDialogo()
        }

        btnGuardar.setOnClickListener {

            nombre = etNombre.text.toString()
            precio = etPrecio.text.toString()

            if(nombre.isEmpty() || precio.isEmpty()){

                if(nombre.isEmpty()) {
                    tilEditarPrendaNombre.setError("El nombre es obligatorio")
                }
                if(precio.isEmpty()){
                    tilEditarPrendaPrecio.setError("El precio es obligatorio")
                }

            }else {

                tilEditarPrendaPrecio.setError(null)
                tilEditarPrendaNombre.setError(null)

                if (fotoUri == null) {//SI EL USUARIO NO HA ELEGIDO FOTO

                    db.collection("prendas").document(p.idPrenda).update("nombre", nombre)
                    db.collection("prendas").document(p.idPrenda).update("precio", precio)

                    Toast.makeText(requireContext(), "Prenda actualizada",Toast.LENGTH_SHORT).show()

                } else {

                    val filename = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("/fotosPrendas/$filename")
                    ref.putFile(fotoUri!!).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {

                            photoUrl = it.toString()

                            db.collection("prendas").document(p.idPrenda).update("nombre", nombre)
                            db.collection("prendas").document(p.idPrenda).update("precio", precio)
                            db.collection("prendas").document(p.idPrenda).update("foto", photoUrl)

                            Toast.makeText(requireContext(), "Prenda actualizada",Toast.LENGTH_SHORT).show()



                        }
                    }
                }
            }


        }



        return root
    }

    private fun cargarDatos() {

        db.collection("tipo").document(p.idTipo).get().addOnSuccessListener {

            etNombre.setText(p.nombre)
            etPrecio.setText(p.precio)
            etReferencia.setText(p.referencia)
            etTipo.setText(it.get("descripcion").toString())

            Picasso.get().load(Uri.parse(p.foto)).into(imgPrenda)

            etReferencia.isEnabled = false
            etReferencia.setBackgroundColor(resources.getColor(R.color.dark))

            etTipo.isEnabled = false
            etTipo.setBackgroundColor(resources.getColor(R.color.dark))


        }


    }

    /**
     * Metodo que abre un dialogo para elegir camara o galeria
     */
    private fun mostrarDialogo() {
        val fotoDialogo = AlertDialog.Builder(requireContext())
        fotoDialogo.setTitle("Seleccionar Acción")
        val fotoDialogoItems = arrayOf(
            "Seleccionar fotografía de galería",
            "Capturar fotografía desde la cámara"
        )
        fotoDialogo.setItems(
            fotoDialogoItems
        ) { dialog, which ->
            when (which) {
                0 -> elegirFotoGaleria()
                1 -> tomarFotoCamara()

            }
        }
        fotoDialogo.show()
    }

    /**
     * Abre un intent a la camara y le pasa una constante
     */
    private fun tomarFotoCamara() {
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Imagen")
        fotoUri =
            activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)!!
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        startActivityForResult(camaraIntent, CAMARA)
    }

    /**
     * Abre un intent a la galeria y le pasa una constante
     */
    private fun elegirFotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(
            galleryIntent,
            GALERIA
        )
    }


    /**
     * En función de la constante recibida se abre la galeria o la camara
     * Los datos obtenidos los asignamos a la variable fotoUri
     * y gracias a Picasso lo metemos en la imagen
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("FOTO", "Opción::--->$requestCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALERIA) {
            Log.d("FOTO", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                fotoUri = data.data


                //imgPrenda.setImageResource(android.R.color.transparent)
                try {
                    Picasso.get().load(fotoUri).into(imgPrenda)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            //imgPrenda.setImageResource(android.R.color.transparent)
            Picasso.get().load(fotoUri).into(imgPrenda)

        }


    }
}