package com.example.bya

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bya.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var db: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private var idUsuario = ""
    private var name: String? = ""
    private var email: String? = ""
    private var photoUrl: String? = ""
    private lateinit var Auth: FirebaseAuth


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

        val tvEmailUser: TextView = navView.getHeaderView(0).findViewById(R.id.tvEmailUser)
        val tvNombreUser: TextView = navView.getHeaderView(0).findViewById(R.id.tvNombreUser)
        val imgUser: ImageView = navView.getHeaderView(0).findViewById(R.id.imgUser)

        val pref = getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        databaseReference = db.reference.child("usuarios").child(idUsuario)

        var getData = object: ValueEventListener {
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
        }

        databaseReference.addValueEventListener(getData)
        databaseReference.addListenerForSingleValueEvent(getData)


    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}