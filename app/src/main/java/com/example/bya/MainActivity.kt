package com.example.bya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.*
import com.example.bya.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
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
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_catalogo, R.id.nav_contacto, R.id.nav_devolver,
            R.id.nav_pedidos,R.id.nav_perfil), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Auth = Firebase.auth
        db = FirebaseDatabase.getInstance("https://byabea-e5b76-default-rtdb.europe-west1.firebasedatabase.app/")

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

        databaseReference = db.reference.child("usuarios").child(idUsuario)

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                name = snapshot.child("nombre").value.toString()
                email = snapshot.child("email").value.toString()
                photoUrl = snapshot.child("foto").value.toString()


                tvEmailUser.text = email
                tvNombreUser.text = name
                Picasso.get()
                    .load(Uri.parse(photoUrl))
                    .transform(CirculoTransformacion())
                    .resize(130,130)
                    .into(imgUser)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}