package com.example.bya.ui.cesta

import android.net.Uri
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

    //Instanciamos la bbdd
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CestaViewHolder {
        return CestaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cesta, parent, false)
        )
    }

    //Rescatamos los datos de una prenda y los ponemos en sus componentes
    override fun onBindViewHolder(holder: CestaViewHolder, position: Int) {

        //Consultamos los favoritos del usuario activo
        db.collection("prendas")
            .whereEqualTo("idPrenda", listaCesta[position].idPrenda)
            .addSnapshotListener { snapshot, e ->

                for (prenda in snapshot!!) {

                    val idPrenda = prenda.get("idPrenda").toString()//Recogemos el id de la prenda

                    //Consultamos los datos de la prenda
                    db.collection("prendas")
                        .whereEqualTo("idPrenda", idPrenda)
                        .addSnapshotListener { snap, a ->
                            for (prenda in snapshot!!) {
                                //recogemos sus datos
                                val foto = prenda.get("foto").toString()
                                val precio = prenda.get("precio").toString()
                                val nombre = prenda.get("nombre").toString()

                                //Lo asignamos al item
                                holder.tvItemCestaNombre.text = nombre
                                holder.tvItemCestaPrecio.text = precio
                                Picasso.get().load(Uri.parse(foto)).into(holder.imgItemCesta)

                            }
                            if (listaCesta.size != 0) {//Si la lista no esta vacía
                                //Asignamos la talla de la prenda
                                holder.tvItemCestaTalla.text = listaCesta[position].talla
                            }


                        }
                }
            }


        /**
         * Detecta cuando pulsamos en el botón de la X para eliminar un item de la cesta
         */
        holder.imgItemCestaEliminar.setOnClickListener {
            accionBorrar(listaCesta[position])
        }

    }


    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaCesta.size
    }

    //Rescatamos los componentes del item
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