package com.example.bya

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bya.clases.Usuario
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    /**
     * VARIABLES
     */
    private var email = ""
    private var pass = ""
    private var idUsuario = ""
    private lateinit var Auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private  val GOOGLE_SIGN_IN = 100
    private var google = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //COMPROBAR PERMISOS
        initPermisos()

        //Variables para acceder a Firebase
        Auth = Firebase.auth


        /**
         * ENTRAMOS EN LA ACTIVIDAD DE REGISTRO
         */
        tvLoginRegistrate.setOnClickListener{
            val r = Intent(this, RegistroActivity::class.java)
            startActivity(r)

        }


        /**
         * BOTON PARA ENTRAR EN BYA
         */
        btnLogin.setOnClickListener {
            //Recogemos la informaci??n del usuario introducida
            email = etLoginEmail.text.toString()
            pass = etLoginPass.text.toString()

            //COMPROBAMOS QUE LOS CAMPOS DEL REGISTRO EST??N RELLENADOS
            if(email.isEmpty() || pass.isEmpty()){
                if(email.isEmpty()){
                    tilLoginEmail.setError("El email es obligatorio")

                }

                if(pass.isEmpty()){
                    tilLoginPass.setError("La contrase??a es obligatoria")
                }

            }else {
                //Quitamos los errores
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
                        Snackbar.make(this, login,"El email o la contrase??a son incorrectos", Snackbar.LENGTH_LONG).show()
                    }


                }
            }


        }

        /**
         * BOTON PARA ACCEDER A BYA DESDE GOOGLE
         */
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

    /**
     * Al querer iniciar sesi??n desde Google, se lanza este metodo
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){//Si la cuenta existe, intentamos loguearnos con Google
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{

                        if(it.isSuccessful){//Si ha salido bien

                            idUsuario = Auth.currentUser.uid//Recogemos su id de la autenticaci??n y lo guardamos en la sharedPreferences
                            val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE).edit()
                            pref.putString("idUsuario",idUsuario)
                            pref.apply()

                            var existe = false
                            //Comprobamos si el usuario esta en la bbdd de firestore
                            db.collection("usuarios")
                                .get()
                                .addOnSuccessListener { result ->
                                    for(usuario in result){

                                        if(usuario.get("idUsuario").toString().equals(idUsuario))  {
                                            existe = true//El usuario existe
                                        }

                                    }

                                    if(!existe){//Si no existe
                                        //A??adimos el usuario a la bbdd de B&A
                                        val u = Usuario (idUsuario,account.displayName.toString(),account.email.toString(),"",account.photoUrl.toString())
                                        db.collection("usuarios").document(idUsuario).set(u)
                                    }


                                    google = true//PARA QUE NO PUEDA EDITAR SU PERFIL
                                    entrarMain()//Entramos al main
                                }



                        }else{
                            Toast.makeText(this, "Algo no ha ido bien", Toast.LENGTH_LONG).show()
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
        var tipo = ""
        //Consultamos el tipo de usuario (admin, normal)
        val pillarTipo = db.collection("usuarios").document(idUsuario)

        pillarTipo.get().addOnSuccessListener {
            tipo = it.get("tipo").toString()//Recogemos el tipo
            main(tipo)//Entramos al main
        }



    }

    /**
     * Metodo que nos lleva al main
     */
    private fun main(tipo : String){

        if(tipo.equals("1")){//Si el tipo es 1, nos lleva al main de los usuario normales
            val main = Intent(this, MainActivity::class.java)
            main.putExtra("Google", google)//Le indicamos si viene de google o no
            startActivity(main)
        }else{//Si no, nos lleva al main del administrador
            val main = Intent(this, AdministradorActivity::class.java)
            startActivity(main)
        }

    }
    /**
     * Funci??n con la que vamos a comprobar los permisos de la aplicaci??n
     * lo vamos a hacer con la librer??a dexter
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
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    //si le damos todos los permisos nos saltar?? un toast diciendolo
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(applicationContext, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                    }

                    //si no tenemos todos los permisos nos lo recordar??
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
            }).withErrorListener { Toast.makeText(applicationContext, "Existen errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }


}