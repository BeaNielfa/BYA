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
import android.widget.ImageView
import android.widget.Toast
import androidx.navigation.ui.AppBarConfiguration
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_registro.*
import kotlinx.android.synthetic.main.fragment_perfil.*
import java.io.IOException
import java.util.*


class PerfilFragment : Fragment() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""
    private var pass =""
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null
    private lateinit var Auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://byabea-e5b76-default-rtdb.europe-west1.firebasedatabase.app/")


        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()
        Log.e("PERFIL ",idUsuario)
        cargarDatos()

        val btnPerfil: Button = root.findViewById(R.id.btnPerfilCambios)
        btnPerfil.setOnClickListener {
            editarDatos()
        }

        val img: ImageView = root.findViewById(R.id.imgPerfil)
        img.setOnClickListener {
            mostrarDialogo()
        }
        // Inflate the layout for this fragment
        return  root
    }
    private fun cargarDatos() {


        databaseReference = db.reference.child("usuarios").child(idUsuario)

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {

                    name = snapshot.child("nombre").getValue().toString()
                    email = snapshot.child("email").getValue().toString()
                    photoUrl = snapshot.child("foto").getValue().toString()
                    pass = snapshot.child("pass").getValue().toString()

                    etPerfilNombre.setText(name)
                    etPerfilEmail.setText(email)
                    etPerfilPass.setText(pass)

                    Picasso.get()
                        // .load(R.drawable.user_avatar)
                        .load(Uri.parse(photoUrl))
                        .transform(CirculoTransformacion())
                        .resize(178, 178)
                        .into(imgPerfil)

                    etPerfilEmail.isEnabled = false
                    etPerfilEmail.setBackgroundColor(resources.getColor(R.color.dark))
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }


    private fun editarDatos() {

       val user = Auth.currentUser

        val nombre = etPerfilNombre.text.toString()
        val pass = etPerfilPass.text.toString()
        val email = etPerfilEmail.text.toString()

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

            if (fotoUri == null) {

                val u = Usuario(idUsuario, nombre, email, pass, photoUrl.toString())

                databaseReference.setValue(u)


            } else {

                val filename = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReference("/fotosUsuarios/$filename")

                ref.putFile(fotoUri!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {

                        photoUrl = it.toString()

                        val u = Usuario(
                            idUsuario, etPerfilNombre.text.toString(),
                            etPerfilEmail.text.toString(), etPerfilPass.text.toString(), photoUrl.toString()
                        )

                        databaseReference.setValue(u)


                    }
                }
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