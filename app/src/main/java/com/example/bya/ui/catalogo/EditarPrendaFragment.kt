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

    //VARIABLES de galeria y camara
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null

    //Instanciamos la bbdd
    private val db = FirebaseFirestore.getInstance()

    private var nombre = ""
    private var precio = ""
    private var photoUrl = ""
    private lateinit var imgPrenda: ImageView
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

        //Enlazamos los elementos con el diseño
        val imgX: ImageView = root.findViewById(R.id.imgEditarCerrar)
        imgPrenda = root.findViewById(R.id.imgEditarPrendaFoto)
        val btnGuardar: Button = root.findViewById(R.id.btnEditarPrendaGuardarCambios)
        etNombre = root.findViewById(R.id.etEditarPrendaNombre)
        etPrecio = root.findViewById(R.id.etEditarPrendaPrecio)
        etReferencia = root.findViewById(R.id.etEditarPrendaReferencia)
        etTipo = root.findViewById(R.id.etEditarPrendaTipo)

        cargarDatos()//Cargamos los datos de la prenda a editar

        /**
         * Al pulsar el botón X volvemos al catalogo
         */
        imgX.setOnClickListener {
            volverCatalogo()
        }

        /**
         * Al pulsar en la imagen, se nos abrirá un dialogo
         */
        imgPrenda.setOnClickListener {
            mostrarDialogo()
        }

        /**
         * Al pulsar en el botón guardar, actualizaremos la prenda
         */
        btnGuardar.setOnClickListener {

            //Rescatamos los valores
            nombre = etNombre.text.toString()
            precio = etPrecio.text.toString()

            //Comprobamos que no intruce vacíos
            if(nombre.isEmpty() || precio.isEmpty()){

                if(nombre.isEmpty()) {
                    tilEditarPrendaNombre.setError("El nombre es obligatorio")
                }
                if(precio.isEmpty()){
                    tilEditarPrendaPrecio.setError("El precio es obligatorio")
                }

            }else {//Si estan rellenos

                //Quitamos los errores
                tilEditarPrendaPrecio.setError(null)
                tilEditarPrendaNombre.setError(null)

                if (fotoUri == null) {//SI EL USUARIO NO HA ELEGIDO FOTO DE LA PRENDA

                    //Se actualizan todos los campos menos la imagen
                    db.collection("prendas").document(p.idPrenda).update("nombre", nombre)
                    db.collection("prendas").document(p.idPrenda).update("precio", precio)

                    Toast.makeText(requireContext(), "Prenda actualizada",Toast.LENGTH_SHORT).show()

                } else {//SI HA CAMBIADO LA FOO DE LA PRENDA

                    val filename = UUID.randomUUID().toString()//Le damos un id a la imagen
                    val ref = FirebaseStorage.getInstance().getReference("/fotosPrendas/$filename")//Le damos una rura
                    ref.putFile(fotoUri!!).addOnSuccessListener {//Metemos la imagen en el storage
                        ref.downloadUrl.addOnSuccessListener {//Descargamos la imagen

                            photoUrl = it.toString()//Recogemos la url, para introducirla en el campo foto

                            //Actualizamos todos los campos de la prenda, incluida la foto
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

    /**
     * Metodo que nos lleva al fragment de catalogo
     */
    private fun volverCatalogo(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.editarPrenda, CatalogoFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Metodo que carga la información de la prenda
     */
    private fun cargarDatos() {

        //Consultamos el tipo que tiene la prenda
        db.collection("tipo").document(p.idTipo).get().addOnSuccessListener {

            etNombre.setText(p.nombre)
            etPrecio.setText(p.precio)
            etReferencia.setText(p.referencia)
            etTipo.setText(it.get("descripcion").toString())

            Picasso.get().load(Uri.parse(p.foto)).into(imgPrenda)

            //La referencia no se puede editar
            etReferencia.isEnabled = false
            etReferencia.setBackgroundColor(resources.getColor(R.color.dark))

            //El tipo no se puede editar
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

                try {
                    Picasso.get().load(fotoUri).into(imgPrenda)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {
            Picasso.get().load(fotoUri).into(imgPrenda)
        }


    }
}