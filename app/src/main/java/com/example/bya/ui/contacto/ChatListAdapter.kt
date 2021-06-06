package com.example.bya.ui.contacto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatListAdapter(private val context: Context, private val listaChat: ArrayList<Chat>):
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>(){

    /**
     * CONSTANTES
     */
    private val MENSAJE_IZQUIERDA = 0
    private val MENSAJE_DERECHA = 1

    /**
     * VARIABLES
     */
    var firebaseUser: FirebaseUser? = null
    var idUsuario = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //Comprobamos si el mensaje va a la derecha o izquierda y cargamos sus items
        if (viewType == MENSAJE_DERECHA){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_derecha, parent, false)
            return ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_izquierda, parent, false)
            return ViewHolder(view)
        }


    }

    /**
     *  Rescatamos los datos del chat y los ponemos en sus componentes
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = listaChat[position]
        holder.tvItemNombre.text = chat.mensaje

    }

    /**
     * Devolvemos el numero de elementos que tiene la lista
     */
    override fun getItemCount(): Int {
        return listaChat.size
    }


    /**
     * Rescatamos el tv del item
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvItemNombre : TextView = itemView.findViewById(R.id.tvMensaje)

    }

    /**
     * Recogemos el tipo de vista
     */
    override fun getItemViewType(position: Int): Int {

        firebaseUser = FirebaseAuth.getInstance().currentUser//Recogemos el usuario autenticado activo

        if(listaChat[position].idEmisor == firebaseUser!!.uid){//Si el usuario actual es el emisor
            return MENSAJE_DERECHA//Los mensajes van a la derecha
        } else {
            return MENSAJE_IZQUIERDA//Si no, van a la izquierda
        }

    }

}