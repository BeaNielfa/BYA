package com.example.bya.ui.favoritos

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_favorito.view.*


class FavoritosListAdapter (private val listaFavoritos: MutableList<Prenda>,
                            private val accionPrincipal: (Prenda) -> Unit

) : RecyclerView.Adapter<FavoritosListAdapter.FavoritoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        return FavoritoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorito, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {

        Picasso.get().load(Uri.parse(listaFavoritos[position].foto)).into(holder.imgItemFavoritoFoto)

        holder.itemFavorito.setOnClickListener(){
            accionPrincipal(listaFavoritos[position])
        }

    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaFavoritos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaFavoritos.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Prenda, position: Int) {
        listaFavoritos.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaFavoritos.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaFavoritos.size
    }

    //Rescatamos los tv
    class FavoritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgItemFavoritoFoto = itemView.imgItemFavoritoFoto

        var itemFavorito = itemView.itemFavorito
        var context = itemView.context
    }

}
