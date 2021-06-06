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
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_anadir_prenda.*
import java.io.IOException
import java.util.*


class AnadirPrendaFragment : Fragment() {

    //VARIABLES de galeria y fotos
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null

    //Variables layout
    private var nombre = ""
    private var precio= ""
    private var referencia = ""
    private var idPrenda = ""
    private var idTipo = ""
    private var foto = ""
    private lateinit var imgPrenda: ImageView

    //Variables de firestore y storage
    private lateinit var Storage: FirebaseStorage
    private val db = FirebaseFirestore.getInstance()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_anadir_prenda, container, false);

        //Enlazamos los elementos con el diseño
        val imgX : ImageView = root.findViewById(R.id.imgAnadirCerrar)
        imgPrenda = root.findViewById(R.id.imgAnadirPrendaFoto)
        val btnGuardar : Button = root.findViewById(R.id.btnAnadirPrendaGuardarCambios)
        val etNombre: EditText = root.findViewById(R.id.etAnadirPrendaNombre)
        val etPrecio: EditText = root.findViewById(R.id.etAnadirPrendaPrecio)
        val etReferencia: EditText = root.findViewById(R.id.etAnadirPrendaReferencia)
        val spiTipo : Spinner = root.findViewById(R.id.spiAnadirPrendaTipo)

        //Creamos una instancia del storage
        Storage = FirebaseStorage.getInstance()


        //Al pulsar la X, volvemos al catalogo
        imgX.setOnClickListener {
            volverCatalogo()
        }

        //Al pulsar en la imagen, se nos abrira un dialogo para elegir la imagen
        imgPrenda.setOnClickListener {
            mostrarDialogo()
        }

        //Al pulsar en el botón guardar
        btnGuardar.setOnClickListener {

            //DAMOS UN ID A LA PRENDA
            idPrenda = UUID.randomUUID().toString()

            //En función de la prenda seleccionada, le damos el idTipo de la bbdd
            when (spiTipo.selectedItem.toString()) {

                "Camiseta M" -> idTipo = "0"
                "Blusa M" -> idTipo = "1"
                "Vestido M" -> idTipo = "2"
                "Jeans M" -> idTipo = "3"
                "Accesorio M" -> idTipo = "4"
                "Falda M" -> idTipo = "5"
                "Camiseta H" -> idTipo = "6"
                "Camisa H" -> idTipo = "7"
                "Accesorio H" -> idTipo = "8"
                "Jeans H" -> idTipo = "9"

                else -> idTipo = "No encontrado"

            }

            var existe: Boolean = false

            //recogemos el valor intrucido en el layout
            nombre = etNombre.text.toString()
            precio = etPrecio.text.toString()
            referencia = etReferencia.text.toString()


            //COMPROBAMOS QUE LOS CAMPOS DEL REGISTRO DE LA PRENDA ESTÁN RELLENADOS
            if (nombre.isEmpty() || precio.isEmpty() || referencia.isEmpty() || fotoUri == null) {
                if (nombre.isEmpty()) {
                    tilAnadirPrendaNombre.setError("El nombre es obligatorio")
                }

                if (precio.isEmpty()) {
                    tilAnadirPrendaPrecio.setError("El precio es obligatorio")
                }

                if (referencia.isEmpty()) {
                    tilAnadirPrendaReferencia.setError("La referencia es obligatoria")
                }

                if(fotoUri == null){
                    Toast.makeText(requireContext(), "La foto es obligatoria", Toast.LENGTH_SHORT).show()
                }
            } else {//Si estan rellenados los campos
                //Marcamos los errores a null
                tilAnadirPrendaNombre.setError(null)
                tilAnadirPrendaPrecio.setError(null)
                tilAnadirPrendaReferencia.setError(null)

                //Hacemos una consulta para comprobar que no se introduzca una referencia existente
                db.collection("prendas")
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {

                            if (prenda.get("referencia").toString().equals(referencia)) {

                                tilAnadirPrendaReferencia.setError("La referencia ya existe")

                                existe = true//Indicamos que la referencia existe
                            }

                        }

                        if (!existe) {//Si la referencia no existe, insertamos la prenda

                            tilAnadirPrendaReferencia.setError(null)
                            val img = UUID.randomUUID().toString()//DAMOS UN NOMBRE A LA IMAGEN
                            val ref = Storage.getReference("/fotosPrendas/$img")//Le damos una ruta a la imagen

                            ref.putFile(fotoUri!!).addOnSuccessListener { //Subimos la foto
                                ref.downloadUrl.addOnSuccessListener { //descargamos su url
                                    foto = it.toString()  //lo asignamos a la variable

                                    //Finalmente subimos la prenda
                                    val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, 1)
                                    db.collection("prendas").document(idPrenda).set(p)

                                }
                            }


                            Toast.makeText(requireContext(), "Prenda insertada correctamente",Toast.LENGTH_SHORT).show()

                            //TODO REVISAAAAAAAAAAAAAAAR

                            etNombre.setText("")
                            etPrecio.setText("")
                            etReferencia.setText("")
                            fotoUri = null


                            Picasso.get().
                            load(R.drawable.ic_menu_camera).
                            into(imgPrenda)



                        }

                    }
                }


        }


        // Inflate the layout for this fragment
        return root
    }

    /**
     * Metodo que vuelve al catalogo
     */
    private fun volverCatalogo(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.anadirPrenda, CatalogoFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Metodo que abre un dialogo para elegir camara o galeria
     */
    private fun mostrarDialogo(){
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
    private fun tomarFotoCamara(){
        val value = ContentValues()
        value.put(MediaStore.Images.Media.TITLE, "Imagen")
        fotoUri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)!!
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        startActivityForResult(camaraIntent, CAMARA)
    }

    /**
     * Abre un intent a la galeria y le pasa una constante
     */
    private fun elegirFotoGaleria(){
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

            Picasso.get().
            load(fotoUri).
            into(imgPrenda)

        }
    }

}