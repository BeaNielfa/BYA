package com.example.bya.ui.contacto


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class ContactoFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaContactos = mutableListOf<Usuario>() //Lista de contactos
    private lateinit var contactoAdapter: ContactoListAdapter
    private val db = FirebaseFirestore.getInstance()
    private var idUsuario = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_contacto, container, false)

        //Recogemos el idUsuario del usuario activo
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Inicializamos el adaptador
        contactoAdapter = ContactoListAdapter(idUsuario, listaContactos) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.contactoRecycler)
        recy.layoutManager = LinearLayoutManager(context)

        //Rellenamos la lista de contactos
        rellenarArrayContacto()

        return root
    }

    /**
     * Rellenamos la lista de contactos
     */
    private fun rellenarArrayContacto() {

        //Consultamos todos los usuarios que  tengan el tipo 1 (Usuarios normales)
        //y que chat sea 1 (Los que hayan hablado alguna vez con nosotros)
        db.collection("usuarios")
            .whereEqualTo("tipo", 1)
            .whereEqualTo("chat",1)
            .addSnapshotListener{ snapshot, e->
                listaContactos.clear()//Limpiamos la lista

                //Recogemos los usuarios
                for (contacto in snapshot!!) {

                    //Recogemos los datos del usuario
                    val idUsuario = contacto.get("idUsuario").toString()
                    val nombre = contacto.get("nombre").toString()
                    val email = contacto.get("email").toString()
                    val foto = contacto.get("foto").toString()
                    val pass = contacto.get("pass").toString()

                    //Lo añadimos a la lista de contactos
                    val u = Usuario (idUsuario,nombre,email, pass,foto)
                    listaContactos.add(u)
                }

                //Lo asignamos al adaptador
                recy.adapter = contactoAdapter

            }
    }


    /**
     * Cuando pulsamos en un item, abrimos el chat
     */
    private fun eventoClicFila(usuario: Usuario) {
        abrirUsuario(usuario)
    }

    /**
     * Se llama cuando hemos pulsado un usuario, abrimos el fragment de chat
     */
    private fun abrirUsuario(usuario : Usuario){

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.contactoLayout, ChatFragment(usuario))
        transaction.addToBackStack("chat")
        transaction.commit()

    }

}