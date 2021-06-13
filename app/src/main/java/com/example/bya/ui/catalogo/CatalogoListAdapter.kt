package com.example.bya.ui.catalogo

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_prenda_administrador.view.*

class CatalogoListAdapter(private val listaPrendas: MutableList<Prenda>)
    : RecyclerView.Adapter<CatalogoListAdapter.PrendaViewHolder>() {

    //Instanciamos la bbdd
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        return PrendaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenda_administrador, parent, false)
        )
    }

    //Rescatamos los datos de una prenda y los ponemos en sus componentes
    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {

        //Consultamos el tipo de prenda
        db.collection("tipo").document(listaPrendas[position].idTipo).get().addOnSuccessListener {

            //Asignamos los datos en el item
            holder.tvItemPrendaAdminTipo.text = it.get("descripcion").toString()
            holder.tvItemPrendaAdminNombre.text = listaPrendas[position].nombre
            holder.tvItemPrendaAdminPrecio.text = listaPrendas[position].precio + " EUR"
            holder.tvItemPrendaAminRef.text = listaPrendas[position].referencia
            holder.tvItemPrendaAdminStock.text = "Stock: "+ listaPrendas[position].stock.toString()

            Picasso.get().load(Uri.parse(listaPrendas[position].foto)).into(holder.imgItemPrendaAdmin)



        }





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

    //Rescatamos y enlazamos los valores
    class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemPrendaAdminNombre = itemView.tvItemPrendaAdminNombre
        var tvItemPrendaAdminPrecio = itemView.tvItemPrendaAdminPrecio
        var tvItemPrendaAdminTipo = itemView.tvItemPrendaAdminTipo
        var tvItemPrendaAminRef = itemView.tvItemPrendaAminRef
        var tvItemPrendaAdminStock = itemView.tvItemPrendaAdminStock
        var imgItemPrendaAdmin = itemView.imgItemPrendaAdmin
        var context = itemView.context
    }

}