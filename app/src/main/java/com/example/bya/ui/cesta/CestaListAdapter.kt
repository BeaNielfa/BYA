package com.example.bya.ui.cesta

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_cesta.view.*


class CestaListAdapter(private val listaCesta: MutableList<Cesta>,
                       private val accionBorrar: (Cesta) -> Unit

) : RecyclerView.Adapter<CestaListAdapter.CestaViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CestaViewHolder {
        return CestaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cesta, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: CestaViewHolder, position: Int) {

        try {

            if (listaCesta.size != 0){
                Log.e("METIU", listaCesta[position].idPrenda)
                db.collection("prendas")
                    .whereEqualTo("idPrenda",  listaCesta[position].idPrenda)
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {
                            Log.e("METIU", "METIU")
                            val foto = prenda.get("foto").toString()
                            val precio = prenda.get("precio").toString()
                            val nombre = prenda.get("nombre").toString()

                            holder.tvItemCestaNombre.text = nombre
                            holder.tvItemCestaPrecio.text = precio

                            Picasso.get().load(Uri.parse(foto)).into(holder.imgItemCesta)

                        }

                        holder.tvItemCestaTalla.text = listaCesta[position].talla

                    }

            }

            holder.imgItemCestaEliminar.setOnClickListener {
                accionBorrar(listaCesta[position])
            }



        } catch (e : java.lang.IndexOutOfBoundsException){

        }


    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaCesta.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaCesta.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Cesta, position: Int) {
        listaCesta.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaCesta.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaCesta.size
    }

    //Rescatamos los tv
    class CestaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemCestaNombre = itemView.tvItemCestaNombre
        var tvItemCestaPrecio = itemView.tvItemCestaPrecio
        var tvItemCestaTalla = itemView.tvItemCestaTalla
        var imgItemCesta = itemView.imgItemCesta
        var imgItemCestaEliminar = itemView.imgItemCestaEliminar

        var itemCesta = itemView.itemCesta
        var context = itemView.context
    }

}