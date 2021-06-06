package com.example.bya.ui.contacto

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Chat
import com.example.bya.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contacto.view.*
import kotlin.collections.ArrayList

class ContactoListAdapter(private val idUsuario: String,
                          private val listaContactos: MutableList<Usuario>,
                          private val accionPrincipal: (Usuario) -> Unit

) : RecyclerView.Adapter<ContactoListAdapter.ContactoViewHolder>() {

    /**
     * Variables firebase
     */
    private val db = FirebaseFirestore.getInstance()
    var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        return ContactoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contacto, parent, false)
        )
    }

    /**
     *  Rescatamos los datos de un usuario y los ponemos en sus componentes
     */
    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {


        Picasso.get().load(Uri.parse(listaContactos[position].foto)).transform(CirculoTransformacion()).into(holder.imgItemContactoFoto)
        var chatList = ArrayList<Chat>()
        firebaseUser = FirebaseAuth.getInstance().currentUser


        //Consultamos el chat ordenado por fecha
        db.collection("chat")
            .orderBy("fecha")
            .addSnapshotListener{ snapshot, e->

                for (chat in snapshot!!) {
                    //Rescatamos los mensajes si hemos mantenido una conversación
                    if (chat.get("idEmisor").toString().equals(idUsuario) && chat.get("idReceptor").toString().equals(listaContactos[position].idUsuario) ||
                        chat.get("idEmisor").toString().equals(listaContactos[position].idUsuario) && chat.get("idReceptor").toString().equals(idUsuario)) {

                       //Rescatamos la informacion del chat
                        val idEmisor = chat.get("idEmisor").toString()
                        val idReceptor = chat.get("idReceptor").toString()
                        val mensaje = chat.get("mensaje").toString()
                        val fecha = chat.get("fecha")

                        val c = Chat(idEmisor, idReceptor, mensaje, fecha.toString())
                        //Anadimos ese objeto a la lista de chat
                        chatList.add(c)

                    }
                }
                //Mostramos el ultimo mensaje
                holder.tvItemContactoMensaje.text = chatList[chatList.size -1].mensaje
                //La fecha de ese mensaje
                val ano = chatList[chatList.size -1].fecha.substring(0, 4)
                val mes = chatList[chatList.size -1].fecha.substring(5, 7)
                val dia = chatList[chatList.size -1].fecha.substring(8, 10)

                val fechaMostrar = dia + "/" + mes + "/" + ano
                holder.tvItemContactoFecha.text = fechaMostrar
            }

        //Mostramos el nombre del contacto del usuario
        holder.tvItemContactoNombre.text = listaContactos[position].nombre

        /**
         * Al pulsar en un chat de un contacto
         */
        holder.itemContacto.setOnClickListener {
            accionPrincipal(listaContactos[position])
        }

    }



    /**
     * Devolvemos el numero de elementos que tiene la lista
     */
    override fun getItemCount(): Int {
        return listaContactos.size
    }

    /**
     * Rescatamos los componentes del item
     */
    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgItemContactoFoto = itemView.imgItemContactoFoto
        var tvItemContactoNombre = itemView.tvItemContactoNombre
        var tvItemContactoMensaje = itemView.tvItemContactoMensaje
        var tvItemContactoFecha = itemView.tvItemContactoFecha

        var itemContacto = itemView.itemContacto
        var context = itemView.context
    }


}