package com.example.bya.ui.contacto

import android.net.Uri
import android.os.Build
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
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*
import kotlin.collections.ArrayList

class ContactoListAdapter(private val idUsuario: String,
                          private val listaContactos: MutableList<Usuario>,
                          private val accionPrincipal: (Usuario) -> Unit

) : RecyclerView.Adapter<ContactoListAdapter.ContactoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        return ContactoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contacto, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {

        Picasso.get().load(Uri.parse(listaContactos[position].foto)).transform(CirculoTransformacion()).into(holder.imgItemContactoFoto)
        var chatList = ArrayList<Chat>()
        firebaseUser = FirebaseAuth.getInstance().currentUser


        db.collection("chat")
            .orderBy("fecha")
            .addSnapshotListener{ snapshot, e->

                for (chat in snapshot!!) {

                    Log.e("DATOS", "CHAT")
                    Log.e("DATOS", "CHAT: " + idUsuario)
                    Log.e("DATOS", "CHAT: " + listaContactos[position].idUsuario)
                    if (chat.get("idEmisor").toString().equals(idUsuario) && chat.get("idReceptor").toString().equals(listaContactos[position].idUsuario) ||
                        chat.get("idEmisor").toString().equals(listaContactos[position].idUsuario) && chat.get("idReceptor").toString().equals(idUsuario)) {

                        Log.e("DATOS", "ENTRO AL IF")
                        val idEmisor = chat.get("idEmisor").toString()
                        val idReceptor = chat.get("idReceptor").toString()
                        val mensaje = chat.get("mensaje").toString()
                        val fecha = chat.get("fecha")

                        val c = Chat(idEmisor, idReceptor, mensaje, fecha.toString())
                        //AÃ±adimos ese objeto a la lista de chat
                        chatList.add(c)

                    }
                }
                holder.tvItemContactoMensaje.text = chatList[chatList.size -1].mensaje

                val ano = chatList[chatList.size -1].fecha.substring(0, 4)
                val mes = chatList[chatList.size -1].fecha.substring(5, 7)
                val dia = chatList[chatList.size -1].fecha.substring(8, 10)

                val fechaMostrar = dia + "/" + mes + "/" + ano

                holder.tvItemContactoFecha.text = fechaMostrar
            }

        holder.tvItemContactoNombre.text = listaContactos[position].nombre

        holder.itemContacto.setOnClickListener {
            accionPrincipal(listaContactos[position])
        }

    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaContactos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaContactos.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Usuario, position: Int) {
        listaContactos.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaContactos.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaContactos.size
    }

    //Rescatamos los tv
    class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgItemContactoFoto = itemView.imgItemContactoFoto
        var tvItemContactoNombre = itemView.tvItemContactoNombre
        var tvItemContactoMensaje = itemView.tvItemContactoMensaje
        var tvItemContactoFecha = itemView.tvItemContactoFecha

        var itemContacto = itemView.itemContacto
        var context = itemView.context
    }


}