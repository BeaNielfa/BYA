package com.example.bya

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AdministradorActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val db = FirebaseFirestore.getInstance()
    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""
    private lateinit var Auth: FirebaseAuth
    private lateinit var tvEmailUser: TextView
    private  lateinit var tvNombreUser: TextView
    private  lateinit var imgUser: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrador)
        val toolbar: Toolbar = findViewById(R.id.toolbar_admin)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout_admin)
        val navView: NavigationView = findViewById(R.id.nav_view_admin)
        val navController = findNavController(R.id.nav_host_fragment_admin)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_catalogo, R.id.nav_contacto, R.id.nav_devolver,
            R.id.nav_pedidos,R.id.nav_perfil), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Auth = Firebase.auth

        tvEmailUser = navView.getHeaderView(0).findViewById(R.id.tvEmailUser)
        tvNombreUser  = navView.getHeaderView(0).findViewById(R.id.tvNombreUser)
        imgUser  = navView.getHeaderView(0).findViewById(R.id.imgUser)

        val imgSesion: ImageView = navView.getHeaderView(0).findViewById(R.id.imgSesion)

        imgSesion.setOnClickListener {
            salir()
        }



        cargarDatos()
    }
    /**
     * METODO DE CONFIRMACIÓN PARA SALIR DE LA APP
     */
    private fun salir() {
        AlertDialog.Builder(this)
            .setIcon(R.mipmap.ic_launcher_bya_round)
            .setTitle("Cerrar sesión actual")
            .setMessage("¿Desea salir de la sesión actual?")
            .setPositiveButton("Sí"){ dialog, which -> cerrarSesion()}
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

        val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        db.collection("usuarios").document(idUsuario).addSnapshotListener{ snapshot, e->
            name = snapshot?.get("nombre").toString()
            Log.e("NOMBRE" , "NOMBRE" + name)
            email = snapshot?.get("email").toString()
            photoUrl = snapshot?.get("foto").toString()

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
        val navController = findNavController(R.id.nav_host_fragment_admin)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}