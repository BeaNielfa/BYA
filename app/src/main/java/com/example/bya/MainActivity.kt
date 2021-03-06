package com.example.bya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    /**
     * VARIABLES
     */
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""
    private lateinit var Auth: FirebaseAuth
    private lateinit var tvEmailUser: TextView
    private  lateinit var tvNombreUser: TextView
    private  lateinit var imgUser: ImageView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val google = intent.extras?.getBoolean("Google")

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_perfil,R.id.nav_catalogoUsuario, R.id.nav_favorito, R.id.nav_cesta,
            R.id.nav_historial, R.id.nav_contactoUsuario), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Recogemos el usuario actual
        Auth = Firebase.auth
        //Enlazamos los elementos con el dise??o
        tvEmailUser = navView.getHeaderView(0).findViewById(R.id.tvEmailUser)
        tvNombreUser  = navView.getHeaderView(0).findViewById(R.id.tvNombreUser)
        imgUser  = navView.getHeaderView(0).findViewById(R.id.imgUser)
        val imgSesion: ImageView = navView.getHeaderView(0).findViewById(R.id.imgSesion)

        //Si viene de google, ocultamos el perfil para que pueda editarlo
        if(google!!){
            ocultarPerfil()
        }

        /**
         * Al pulsar en la imagen, nos aparecer?? un  dialogo para cerrar sesi??n
         */
        imgSesion.setOnClickListener {
            salir()
        }

        //Cargamos los datos del usuario en el navHeader
        cargarDatos()

    }

    /**
     * Metodo que oculta la opci??n del perfil del men??
     */
    private fun ocultarPerfil(){
        var navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val nav_Menu: Menu = navigationView.getMenu()
        nav_Menu.findItem(R.id.nav_perfil).isVisible = false
    }


    /**
     * METODO DE CONFIRMACI??N PARA SALIR DE LA APP
     */
    private fun salir() {
        AlertDialog.Builder(this)
            .setIcon(R.mipmap.ic_launcher_bya_round)
            .setTitle("Cerrar sesi??n actual")
            .setMessage("??Desea salir de la sesi??n actual?")
            .setPositiveButton("S??"){ dialog, which -> cerrarSesion()}
            .setNegativeButton( "No",null)
            .show()
    }

    /**
     * METODO QUE CIERRA LA SESION DEL USUARIO Y BORRA SUS PREFERENCIAS
     */
    private fun cerrarSesion(){

        //Se borran las shared preferences
        val prefs = this.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)?.edit()
        prefs?.clear()
        prefs?.apply()

        //Se cierra sesion del firebase
        FirebaseAuth.getInstance().signOut()

        //Se vuelve al login
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

    }


    /**
     * METODO QUE CONSULTA LOS DATOS DEL USUARIO
     */
    private fun cargarDatos(){

        //Recogemos el idUsuario del usuario activo
        val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Consultamos la informaci??n del usuario
        db.collection("usuarios").document(idUsuario).addSnapshotListener{ snapshot, e->
            //Rescatamos la informaci??n del usuario
            name = snapshot?.get("nombre").toString()
            email = snapshot?.get("email").toString()
            photoUrl = snapshot?.get("foto").toString()

            //Asignamos la informaci??n en el navHeader
            tvEmailUser.text = email
            tvNombreUser.text = name
            Picasso.get()
                .load(Uri.parse(photoUrl))
                .transform(CirculoTransformacion())
                .resize(130,130)
                .into(imgUser)

        }

    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}