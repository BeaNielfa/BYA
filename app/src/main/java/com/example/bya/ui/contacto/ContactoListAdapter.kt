package com.example.bya.ui.contacto

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.example.bya.clases.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_contacto.view.*
import kotlinx.android.synthetic.main.item_historial.view.*

class ContactoListAdapter(private val listaContactos: MutableList<Usuario>,
                          private val accionPrincipal: (Usuario) -> Unit

) : RecyclerView.Adapter<ContactoListAdapter.ContactoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        return ContactoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contacto, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {

        Picasso.get().load(Uri.parse(listaContactos[position].foto)).transform(CirculoTransformacion()).into(holder.imgItemContactoFoto)

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

        var itemContacto = itemView.itemContacto
        var context = itemView.context
    }

}