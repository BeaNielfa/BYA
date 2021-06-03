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

    private lateinit var recy : RecyclerView
    private var listaContactos = mutableListOf<Usuario>() //Lista de favoritos
    private lateinit var contactoAdapter: ContactoListAdapter

    private val db = FirebaseFirestore.getInstance()

    private var idUsuario = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_contacto, container, false)

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //detecta cuando pulsamos en un item
        contactoAdapter = ContactoListAdapter(idUsuario, listaContactos) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.contactoRecycler)

        recy.layoutManager = LinearLayoutManager(context)

        rellenarArrayContacto()

        return root
    }

    private fun rellenarArrayContacto() {

        db.collection("usuarios")
            .whereEqualTo("tipo", 1)
            .whereEqualTo("chat",1)
            .addSnapshotListener{ snapshot, e->
                listaContactos.clear()

                for (contacto in snapshot!!) {

                    val idUsuario = contacto.get("idUsuario").toString()
                    val nombre = contacto.get("nombre").toString()
                    val email = contacto.get("email").toString()
                    val foto = contacto.get("foto").toString()
                    val pass = contacto.get("pass").toString()

                    val u = Usuario (idUsuario,nombre,email, pass,foto)

                    listaContactos.add(u)
                }

                recy.adapter = contactoAdapter

            }
    }

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
        transaction.addToBackStack(null)
        transaction.commit()

    }

}