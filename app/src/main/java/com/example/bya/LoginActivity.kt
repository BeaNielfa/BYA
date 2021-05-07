package com.example.bya

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bya.clases.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //VARIABLES
    private var email = ""
    private var pass = ""
    private var idUsuario = ""
    private lateinit var Auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private  val GOOGLE_SIGN_IN = 100

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

        btnLogin.setOnClickListener {
            email = etLoginEmail.text.toString()
            pass = etLoginPass.text.toString()

            Auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener{

                if(it.isSuccessful){
                    idUsuario = Auth.currentUser.uid
                    val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE).edit()
                    pref.putString("idUsuario",idUsuario)
                    pref.apply()

                    entrarMain()
                }else{

                }



            }


        }

        imgGoogle.setOnClickListener {
            loginToGoogle()
        }
    }

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

                            var getData = object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if(!snapshot.hasChild(idUsuario)){
                                        val u = Usuario (idUsuario,account.displayName.toString(),account.email.toString(),"",account.photoUrl.toString())
                                        databaseReference.child(idUsuario).setValue(u)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            }

                            databaseReference.addValueEventListener(getData)
                            databaseReference.addListenerForSingleValueEvent(getData)

                            entrarMain()
                        }else{

                        }
                    }
                }
            }catch (e: ApiException){

            }
        }
    }
    private fun entrarMain(){
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)

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