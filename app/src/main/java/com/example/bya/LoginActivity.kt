package com.example.bya

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bya.clases.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_registro.*
import kotlinx.android.synthetic.main.fragment_perfil.*

class LoginActivity : AppCompatActivity() {
    //VARIABLES
    private var email = ""
    private var pass = ""
    private var idUsuario = ""
    private lateinit var Auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private  val GOOGLE_SIGN_IN = 100
    private var google = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //COMPROBAR PERMISOS
        initPermisos()

        //Variables para acceder a Firebase
        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://byabea-e5b76-default-rtdb.europe-west1.firebasedatabase.app/")


        //ENTRAMOS EN LA ACTIVIDAD DE REGISTRO
        tvLoginRegistrate.setOnClickListener{
            val r = Intent(this, RegistroActivity::class.java)
            startActivity(r)

        }

        //BOTON PARA ENTRAR EN BYA
        btnLogin.setOnClickListener {
            email = etLoginEmail.text.toString()
            pass = etLoginPass.text.toString()

            //COMPROBAMOS QUE LOS CAMPOS DEL REGISTRO ESTÁN RELLENADOS
            if(email.isEmpty() || pass.isEmpty()){
                if(email.isEmpty()){
                    tilLoginEmail.setError("El email es obligatorio")

                }

                if(pass.isEmpty()){
                    tilLoginPass.setError("La contraseña es obligatoria")
                }

            }else {
                tilLoginPass.setError(null)
                tilLoginEmail.setError(null)
                //COMPROBAMOS SI ESTA AUTENTICADO EN BYA
                Auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {

                    if (it.isSuccessful) {//SI LO ESTA
                        idUsuario = Auth.currentUser.uid
                        val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE).edit()
                        pref.putString("idUsuario", idUsuario)
                        pref.apply()

                        entrarMain()//ENTRAMOS EN LA APP
                    } else {
                        Snackbar.make(this, login,"El email o la contraseña son incorrectos", Snackbar.LENGTH_LONG).show()
                    }


                }
            }


        }

        //BOTON PARA ACCEDER A BYA DESDE GOOGLE
        imgGoogle.setOnClickListener {
            loginToGoogle()
        }
    }

    /**
     * BOTON QUE NOS PERMITE ACCEDER A BYA SI TENEMOS UNA CUENTA DE GOOGLE CREADA
     */
    private fun loginToGoogle(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this, googleConf)
        googleClient.signOut()

        startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            databaseReference = db.reference.child("usuarios")

                            idUsuario = Auth.currentUser.uid
                            val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE).edit()
                            pref.putString("idUsuario",idUsuario)
                            pref.apply()

                            databaseReference.addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if(!snapshot.hasChild(idUsuario)){
                                        val u = Usuario (idUsuario,account.displayName.toString(),account.email.toString(),"",account.photoUrl.toString())
                                        databaseReference.child(idUsuario).setValue(u)

                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })

                            google = true//PARA QUE NO PUEDA EDITAR SU PERFIL
                            entrarMain()
                        }else{

                        }
                    }
                }
            }catch (e: ApiException){

            }
        }
    }

    /**
     * Metodo que nos lleva a la actividad Main
     */
    private fun entrarMain(){
        databaseReference = db.reference.child("usuarios").child(idUsuario)

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val tipo = snapshot.child("tipo").getValue().toString()

                main(tipo)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })




    }

    private fun main(tipo : String){

        if(tipo.equals("1")){
            val main = Intent(this, MainActivity::class.java)
            main.putExtra("Google", google)
            startActivity(main)
        }else{
            val main = Intent(this, AdministradorActivity::class.java)
            startActivity(main)
        }

    }
    /**
     * Función con la que vamos a comprobar los permisos de la aplicación
     * lo vamos a hacer con la librería dexter
     */
    private fun initPermisos() {

        Dexter.withContext(this)
            //Permisos que queremos comprobar
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
            )

            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    //si le damos todos los permisos nos saltará un toast diciendolo
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                    }

                    //si no tenemos todos los permisos nos lo recordará
                    if (report.isAnyPermissionPermanentlyDenied) {
                        Toast.makeText(applicationContext, "Faltan permisos!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "Existe errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }


}