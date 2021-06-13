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



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        return FavoritoViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorito, parent, false)
        )
    }


    /**
     * Rescatamos los datos de la prenda y los ponemos en sus componentes
     */
    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {

        //Mostramos la imagen de la prenda
        Picasso.get().load(Uri.parse(listaFavoritos[position].foto)).into(holder.imgItemFavoritoFoto)

        //Al pulsar en el item, se abrirá el detalle de la prenda
        holder.itemFavorito.setOnClickListener(){
            accionPrincipal(listaFavoritos[position])
        }

    }


    /**
     * Devolvemos el numero de elementos que tiene la lista
     */
    override fun getItemCount(): Int {
        return listaFavoritos.size
    }


    /**
     * Rescatamos los componentes de la prenda
     */
    class FavoritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgItemFavoritoFoto = itemView.imgItemFavoritoFoto
        var itemFavorito = itemView.itemFavorito
        var context = itemView.context
    }

}
