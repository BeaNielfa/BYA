package com.example.bya.ui.catalogoUsuario

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_prenda_usuario.view.*

class CatalogoUsuarioListAdapter(private val listaPrendas: MutableList<Prenda>,
                                 private val accionPrincipal: (Prenda) -> Unit

) : RecyclerView.Adapter<CatalogoUsuarioListAdapter.PrendaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        return PrendaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenda_usuario, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {

        holder.tvItemPrendaUsuarioNombre.text = listaPrendas[position].nombre
        holder.tvItemPrendaUsuarioPrecio.text = listaPrendas[position].precio + " EUR"

        Picasso.get().load(Uri.parse(listaPrendas[position].foto)).into(holder.imgItemPrendaUsuario)

        holder.itemPrenda.setOnClickListener(){
            accionPrincipal(listaPrendas[position])
        }

    }

    fun orderByPrecioDes(){
        listaPrendas.sortBy{ it.precio.toFloat() }
        notifyDataSetChanged()
    }

    fun orderByPrecioAsc(){
        listaPrendas.sortByDescending { it.precio.toFloat() }
        notifyDataSetChanged()
    }



    fun filterByName(prenda : List<Prenda>){

        listaPrendas.clear()
        listaPrendas.addAll(prenda)
        notifyDataSetChanged()
    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaPrendas.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaPrendas.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Prenda, position: Int) {
        listaPrendas.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaPrendas.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaPrendas.size
    }

    //Rescatamos los tv
    class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemPrendaUsuarioNombre = itemView.tvItemPrendaUsuarioNombre
        var tvItemPrendaUsuarioPrecio = itemView.tvItemPrendaUsuarioPrecio
        var imgItemPrendaUsuario = itemView.imgItemPrendaUsuario

        var itemPrenda = itemView.itemPrendaUsuario
        var context = itemView.context
    }

}
