package com.example.bya.ui.perfil

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.ui.AppBarConfiguration
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_perfil.*
import java.io.IOException
import java.util.*


class PerfilFragment : Fragment() {


    /**
     * VARIABLES
     */
    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""
    private var pass =""
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null
    private lateinit var Auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var imgPerfil: ImageView




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        //Enlazamos los elementos con el diseño
        etNombre = root.findViewById(R.id.etPerfilNombre)
        etEmail =  root.findViewById(R.id.etPerfilEmail)
        etPass =   root.findViewById(R.id.etPerfilPass)
        imgPerfil = root.findViewById(R.id.imgPerfil)
        val btnPerfil: Button = root.findViewById(R.id.btnPerfilCambios)
        val img: ImageView = root.findViewById(R.id.imgPerfil)

        //Instanciamos la autenticacion
        Auth = Firebase.auth

        //Recogemos el idUsuario del usuario activo con las SharedPreferences
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Cargamos los datos del perfil
        cargarDatos()

        /**
         * Al pulsar en el botón del perfil, nos lleva al fragment donde podemos editar el perfil
         */
        btnPerfil.setOnClickListener {
            editarDatos()
        }

        /**
         * Al pulsar en la imagen, nos aparecerá un dialog para poder cmbiarla
         */
        img.setOnClickListener {
            mostrarDialogo()
        }

        return  root
    }

    /**
     * Metodo que carga los datos del usuario activo
     */
    private fun cargarDatos() {

        //Consultamos los datod del usuario
        db.collection("usuarios").document(idUsuario).get().addOnSuccessListener {
            //Rescatamos los datos del usuario
            name = it.get("nombre").toString()
            email =  it.get("email").toString()
            photoUrl =  it.get("foto").toString()
            pass =  it.get("pass").toString()

            //Asignamos la información en el layout
            etNombre.setText(name)
            etEmail.setText(email)
            etPass.setText(pass)

            Picasso.get()
                .load(Uri.parse(photoUrl))
                .transform(CirculoTransformacion())
                .resize(178, 178)
                .into(imgPerfil)

            //Deshabilitamos el email, ya que no se va a poder cambiar
            etEmail.isEnabled = false
            etEmail.setBackgroundColor(resources.getColor(R.color.dark))

        }



    }


    /**
     * Metodo en el que editamos el perfil del usuario
     */
    private fun editarDatos() {

        //VARIABLES
        val user = Auth.currentUser
        val nombre = etNombre.text.toString()
        val pass = etPass.text.toString()

        //Comprobamos que los datos son correctos
        if(nombre.isEmpty() || pass.isEmpty()){
            if(nombre.isEmpty()) {
                tilPerfilNombre.setError("El nombre es obligatorio")
            }
            if(pass.isEmpty()){
                tilPerfilPass.setError("La contraseña es obligatoria")
            }
        }else if(pass.length < 6){
            tilPerfilNombre.setError(null)
            tilPerfilPass.setError("Debe tener mínimo 6 caracteres")
        }else {

            //ACTUALIZAMOS LA CONTRASEÑA EN LA TABLA DE AUTENTICACION (SI LA CAMBIA)
            user!!.updatePassword(pass)

            if (fotoUri == null) {//SI EL USUARIO NO HA ELEGIDO FOTO

                //actualizamos solo los campos nombre y pass
                db.collection("usuarios").document(idUsuario).update("nombre", nombre)
                db.collection("usuarios").document(idUsuario).update("pass", pass)

                Toast.makeText(requireContext(), "Usuario actualizado",Toast.LENGTH_SHORT).show()

            } else {//Si ha elegido foto
                val filename = UUID.randomUUID().toString()//Le creamos un id
                val ref = FirebaseStorage.getInstance().getReference("/fotosUsuarios/$filename")//Creamos una url
                ref.putFile(fotoUri!!).addOnSuccessListener {//Metemos la imagen en el storage
                    ref.downloadUrl.addOnSuccessListener {//Descargamos la url de la imagen

                        photoUrl = it.toString()//Rescatamos la url de la imagen

                        //Actualizamos todos los campos, incluida la foto
                        db.collection("usuarios").document(idUsuario).update("nombre", nombre)
                        db.collection("usuarios").document(idUsuario).update("pass", pass)
                        db.collection("usuarios").document(idUsuario).update("foto", photoUrl)

                    }
                }

                Toast.makeText(requireContext(), "Usuario actualizado",Toast.LENGTH_SHORT).show()
            }
        }

    }

    /**
     * Metodo que abre un dialogo para elegir camara o galeria
     */
    private fun mostrarDialogo(){
        val fotoDialogo = AlertDialog.Builder(activity)
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

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == GALERIA) {

            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                fotoUri = data.data
                try {
                    Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imgPerfil)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(activity, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imgPerfil)

        }
    }
}