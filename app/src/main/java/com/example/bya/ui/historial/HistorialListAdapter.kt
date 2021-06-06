package com.example.bya.ui.historial

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_historial.view.*

class HistorialListAdapter(private val listaHistorial: MutableList<Pedido>,
                           private val accionQr: (Pedido) -> Unit

) : RecyclerView.Adapter<HistorialListAdapter.HistorialViewHolder>() {

    /**
     * Creamos una instancia de la bbdd de firestore
     */
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        return HistorialViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_historial, parent, false)
        )
    }

    /**
     * Rescatamos los datos del historial y los ponemos en sus componentes
     */
    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {

        //Consultamos la prenda del pedido
        db.collection("prendas")
            .whereEqualTo("idPrenda",  listaHistorial[position].idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {
                    //Recogemos la información de la prenda
                    val foto = prenda.get("foto").toString()
                    val precio = prenda.get("precio").toString()
                    val nombre = prenda.get("nombre").toString()

                    //Asignamos la información en el item
                    holder.tvItemHistorialNombre.text = nombre
                    holder.tvItemHistorialPrecio.text = precio
                    Picasso.get().load(Uri.parse(foto)).into(holder.imgItemHistorial)
                }

            }

        //Asignamos la fecha al item
        holder.tvItemHistorialFecha.text = listaHistorial[position].fechaCompra
        //Al pulsar en la imagen del codigo qr, nos aparecerá el codigo qr
        holder.imgItemHistorialQr.setOnClickListener {
            accionQr(listaHistorial[position])
        }


    }

    /**
     * Devolvemos el numero de elementos que tiene la lista
     */
    override fun getItemCount(): Int {
        return listaHistorial.size
    }


    /**
     * Rescatamos los componentes del pedido
     */
    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemHistorialNombre = itemView.tvItemHistorialNombre
        var tvItemHistorialPrecio = itemView.tvItemHistorialPrecio
        var tvItemHistorialFecha = itemView.tvItemHistorialFecha
        var imgItemHistorial = itemView.imgItemHistorial
        var imgItemHistorialQr = itemView.imgItemHistorialQr


        var context = itemView.context
    }

}