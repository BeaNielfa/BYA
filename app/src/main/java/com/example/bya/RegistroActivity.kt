package com.example.bya

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.bya.clases.Usuario
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registro.*
import java.io.IOException
import java.util.*

class RegistroActivity : AppCompatActivity() {

    //VARIABLES
    private val GALERIA = 1
    private val CAMARA = 2
    private var fotoUri: Uri? = null
    private var foto = ""
    private var email =""
    private var pass = ""
    private var nombre =""
    private var id = ""

    private lateinit var Auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var Storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Variables para acceder a Firebase
        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://byabea-e5b76-default-rtdb.europe-west1.firebasedatabase.app/")
        databaseReference = db.reference.child("usuarios")
        Storage = FirebaseStorage.getInstance()


        //BOTON QUE NOS PERMITE REGISTRARNOS EN BYA
        btnRegistroRegistrarse.setOnClickListener {

                registrar()

        }
        //BOTON QUE NOS PERMITE CANCELAR EL REGISTRO Y VOLVER A LA VENTANA DE LOGIN
        btnRegistroCancelar.setOnClickListener {
            entrarLogin()
        }
        //BOTON QUE NOS ABRE UN DIALOGO PARA ELEGIR SI QUEREMOS UNA FOTO DE LA GALERIA O LA CAMARA
        imgRegistro.setOnClickListener {
            mostrarDialogo()
        }
    }

    /**
     * Metodo que registra un nuevo usuario en la bbdd para acceder a BYA
     */
    private fun registrar(){
        Log.e("REGISTRO", "Entrada al metodo de registrar")

        email = etRegistroEmail.text.toString()
        nombre = etRegistroNombre.text.toString()
        pass = etRegistroPass.text.toString()

        Log.e("REGISTRO",email+ "EMAIL")
        Log.e("REGISTRO",nombre+ " NOMBRE")
        Log.e("REGISTRO",pass+ " PASSWORD"+ " LONGITUD "+pass.length)

        //COMPROBAMOS QUE LOS CAMPOS DEL REGISTRO ESTÁN RELLENADOS
        if(email.isEmpty() || nombre.isEmpty() || pass.isEmpty()){
            if(email.isEmpty()){
                tilRegistroEmail.setError("El email es obligatorio")
            }

            if(pass.isEmpty()){
                tilRegisroPass.setError("La contraseña es obligatoria")
            }

            if(nombre.isEmpty()){
                tilRegistroNombre.setError("El nombre es obligatorio")
            }

        }else if(pass.length < 6){//COMPROBAMOS QUE LA CONTRASEÑA TIENE MÍNIMO 6 CARACTERES
            tilRegistroEmail.setError(null)
            tilRegistroNombre.setError(null)
            tilRegisroPass.setError("Debe tener mínimo 6 caracteres")
        }else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            tilRegistroNombre.setError(null)
            tilRegisroPass.setError(null)
           tilRegistroEmail.setError("El formato no es correcto")
        }else{
            tilRegistroEmail.setError(null)
            tilRegistroNombre.setError(null)
            tilRegisroPass.setError(null)
            //PRIMERO NOS AUTENTICAMOS EN FIREBASE
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass).
            addOnCompleteListener{
                if(it.isSuccessful){//SI HA IDO BIEN
                    id = Auth.currentUser.uid
                    val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE).edit()
                    pref.putString("idUsuario",id)
                    pref.apply()

                    val img = UUID.randomUUID().toString()//DAMOS UN NOMBRE A LA IMAGEN
                    val ref = Storage.getReference("/fotosUsuarios/$img")
                    if(fotoUri == null){//cuando no hay foto, ponemos una por defecto
                        var fotoDefecto = Uri.parse("android.resource://com.example.bya/"+R.drawable.profile_user)
                        ref.putFile(fotoDefecto).addOnSuccessListener { //Subimos la foto
                            ref.downloadUrl.addOnSuccessListener { //descargamos su url
                                foto = it.toString()  //lo asignamos a la variable

                                val u = Usuario (id,nombre,email, pass,foto)
                                databaseReference.child(id).setValue(u)//AÑADIMOS EL USUARIO A LA TABLA
                            }
                        }

                    }else{//cuando la hay la subimos

                        ref.putFile(fotoUri!!).addOnSuccessListener { //Subimos la foto
                            ref.downloadUrl.addOnSuccessListener { //descargamos su url
                                foto = it.toString()  //lo asignamos a la variable

                                val u = Usuario (id,nombre,email, pass,foto)//AÑADIMOS EL USUARIO A LA TABLA
                                databaseReference.child(id).setValue(u)
                            }
                        }
                    }
                        //UNA VEZ REGISTRADOS NOS LLEVA A LA ACTIVIDAD MAIN
                        entrarMain()
                }else{//SI NO HA IDO BIEN, EL CORREO YA EXISTE
                    tilRegistroEmail.setError("Este email ya está registrado")
                }
            }
        }


    }
    /**
     * Metodo que nos lleva a la actividad Login
     */
    private fun entrarLogin(){
        val login = Intent(this, LoginActivity::class.java)
        startActivity(login)

    }

    /**
     * Metodo que nos lleva a la actividad Main
     */
    private fun entrarMain(){
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)

    }

    /**
     * Metodo que abre un dialogo para elegir camara o galeria
     */
    private fun mostrarDialogo(){
        val fotoDialogo = AlertDialog.Builder(this)
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
        fotoUri = this.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, value)!!
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
                    Picasso.get().load(fotoUri).transform(CirculoTransformacion()).into(imgRegistro)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (requestCode == CAMARA) {

            Picasso.get().
                    load(fotoUri).
                    transform(CirculoTransformacion()).
                    into(imgRegistro)

        }
    }
}