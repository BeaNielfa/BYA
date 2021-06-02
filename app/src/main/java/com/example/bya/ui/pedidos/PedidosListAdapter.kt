package com.example.bya.ui.pedidos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_historial.view.*
import kotlinx.android.synthetic.main.item_pedido.view.*

class PedidosListAdapter(private val context : Context,
                         private val listaPedidos: MutableList<Pedido>,
                         private val accionPrincipal: (Pedido) -> Unit,


                         ) : RecyclerView.Adapter<PedidosListAdapter.PedidosViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidosViewHolder {
        return PedidosViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pedido, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: PedidosViewHolder, position: Int) {
        var tipo = ""

        db.collection("prendas")
            .whereEqualTo("idPrenda",  listaPedidos[position].idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {
                    Log.e("METIU", "METIU")
                    val foto = prenda.get("foto").toString()
                    val precio = prenda.get("precio").toString()
                    val nombre = prenda.get("nombre").toString()
                    tipo = prenda.get("idTipo").toString()

                    holder.tvItemPedidosNombre.text = nombre
                    holder.tvItemPedidosPrecio.text = precio

                    Picasso.get().load(Uri.parse(foto)).into(holder.imgItemPedidos)

                }
                Log.e("TIPO", "TIPO: " + tipo)
                db.collection("tipo").document(tipo).get().addOnSuccessListener {

                    holder.tvItemPedidosTipo.text = it.get("descripcion").toString()
                }
                var estado = listaPedidos[position].estado.toString().toInt()
                Log.e("PEDIDO", estado.toString()+" ehhhhh")
                if(estado == 1){
                    holder.tvItemPedidosEstado.setTextColor(getColor(context, R.color.verde))
                    holder.tvItemPedidosEstado.text = "Enviado"
                    Log.e("PEDIDO", estado.toString()+" Enviado")

                } else {
                    holder.tvItemPedidosEstado.setTextColor(getColor(context, R.color.rojo))
                    holder.tvItemPedidosEstado.text = "Sin enviar"
                    Log.e("PEDIDO", estado.toString()+" Sin enviar")
                }

            }



        holder.imgItemPedidos.setOnClickListener {
            accionPrincipal(listaPedidos[position])
        }


    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaPedidos.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaPedidos.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Pedido, position: Int) {
        listaPedidos.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaPedidos.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaPedidos.size
    }

    //Rescatamos los tv
    class PedidosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemPedidosNombre = itemView.tvItemPedidoNombre
        var tvItemPedidosPrecio = itemView.tvItemPedidoPrecio
        var tvItemPedidosTipo = itemView.tvItemPedidoTipo
        var imgItemPedidos = itemView.imgItemPedido
        var tvItemPedidosEstado = itemView.tvItemPedidoEstado

        var itemPedido = itemView.itemPedido
        var context = itemView.context

    }

}