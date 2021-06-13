package com.example.bya.ui.pedidos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore


class PedidosFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaPedidos = mutableListOf<Pedido>() //Lista de favoritos
    private lateinit var pedidosAdapter: PedidosListAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Enlazamos los elementos con el diseño
        val root = inflater.inflate(R.layout.fragment_pedidos, container, false)

        //Inicializamos el adaptador
        pedidosAdapter = PedidosListAdapter(requireContext(), listaPedidos) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.pedidoRecycler)
        recy.layoutManager = LinearLayoutManager(context)

        //Rellenamos la lista de pedidos
        rellenarArrayPedidos()

        return root
    }

    /**
     * Metodo que rellena la lista de pedidos
     */
    private fun rellenarArrayPedidos() {

        //Consultamos todos los pedidos
        db.collection("pedidos")
             .addSnapshotListener{ snapshot, e->
                listaPedidos.clear()//Limpiamos la lista

                for (historial in snapshot!!) {

                    //Recogemos la información del pedido
                    val idPedido = historial.get("idPedido").toString()
                    val idPrenda = historial.get("idPrenda").toString()
                    val idUsuario = historial.get("idUsuario").toString()
                    val fechaCompra = historial.get("fechaCompra").toString()
                    val latitud = historial.get("latitud").toString()
                    val longitud = historial.get("longitud").toString()
                    val talla = historial.get("talla").toString()
                    val estado = historial.get("estado").toString().toInt()

                    //Añadimos el pedido a la lista
                    val p = Pedido(idPedido, idPrenda, idUsuario, fechaCompra, latitud, longitud, talla, estado)
                    listaPedidos.add(p)
                }

            recy.adapter = pedidosAdapter

        }

    }

    /**
     * Al pulsar en un pedido , nos lleva al fragment de su detalle
     */
    private fun eventoClicFila(pedido: Pedido) {
        abrirPedido(pedido)
    }

    /**
     * Metodo que nos lleva al fragment del detalle del pedido
     */
    private fun abrirPedido(pedido: Pedido) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.fragmentPedidos, PedidosDetalleFragment(pedido))
        transaction.addToBackStack("pedidos")
        transaction.commit()
    }


}