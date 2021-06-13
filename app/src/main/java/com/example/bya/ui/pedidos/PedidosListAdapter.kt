package com.example.bya.ui.pedidos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_pedido.view.*

class PedidosListAdapter(private val context : Context,
                         private val listaPedidos: MutableList<Pedido>,
                         private val accionPrincipal: (Pedido) -> Unit,
                         ) : RecyclerView.Adapter<PedidosListAdapter.PedidosViewHolder>() {

    //Creamos una instancia de la bbdd de firestore
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidosViewHolder {
        return PedidosViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pedido, parent, false)
        )
    }

    /**
     * Rescatamos los datos del pedido y los ponemos en sus componentes
     */
    override fun onBindViewHolder(holder: PedidosViewHolder, position: Int) {
        var tipo = ""

        //Consultamos la prenda
        db.collection("prendas")
            .whereEqualTo("idPrenda",  listaPedidos[position].idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {
                    //Rescatamos la información de la prenda
                    val foto = prenda.get("foto").toString()
                    val precio = prenda.get("precio").toString()
                    val nombre = prenda.get("nombre").toString()
                    tipo = prenda.get("idTipo").toString()

                    //Asignamos la información en el item
                    holder.tvItemPedidosNombre.text = nombre
                    holder.tvItemPedidosPrecio.text = precio
                    Picasso.get().load(Uri.parse(foto)).into(holder.imgItemPedidos)

                }
                //Consultamos el tipo de la prenda
                db.collection("tipo").document(tipo).get().addOnSuccessListener {
                    //Asignamos el tipo de prenda
                    holder.tvItemPedidosTipo.text = it.get("descripcion").toString()
                }
                //Rescatamos el estado del pedido
                var estado = listaPedidos[position].estado.toString().toInt()


                if(estado == 1){//Si el estado es 1, ponemos el teto en verde
                    holder.tvItemPedidosEstado.setTextColor(getColor(context, R.color.verde))
                    holder.tvItemPedidosEstado.text = "Enviado"
                } else if(estado == 0){//Si el estado es 0, lo ponemos en rojo
                    holder.tvItemPedidosEstado.setTextColor(getColor(context, R.color.rojo))
                    holder.tvItemPedidosEstado.text = "Sin enviar"
                }else{//Si no, lo ponemos en rojo
                    holder.tvItemPedidosEstado.setTextColor(getColor(context, R.color.rojo))
                    holder.tvItemPedidosEstado.text = "Pedido devuelto"
                }

            }


        /**
         * Al pulsar en un item, nos lleva al detalle del pedido
         */
        holder.imgItemPedidos.setOnClickListener {
            accionPrincipal(listaPedidos[position])
        }


    }



    /**
     * Devolvemos el numero de elementos que tiene la lista
     */
    override fun getItemCount(): Int {
        return listaPedidos.size
    }


    /**
     * Rescatamos los componentes del pedido
     */
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