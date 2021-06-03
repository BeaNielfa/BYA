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

    private lateinit var recy : RecyclerView
    private var listaPedidos = mutableListOf<Pedido>() //Lista de favoritos
    private lateinit var pedidosAdapter: PedidosListAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pedidos, container, false)

        val transaction1 = requireActivity().supportFragmentManager.beginTransaction()
        //detecta cuando pulsamos en un item
        pedidosAdapter = PedidosListAdapter(requireContext(), listaPedidos) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.pedidoRecycler)

        recy.layoutManager = LinearLayoutManager(context)

        rellenarArrayPedidos()

        return root
    }

    private fun rellenarArrayPedidos() {


       /* db.collection("pedidos")
            .get()
            .addOnSuccessListener { result ->
                listaPedidos.clear()
                for (historial in result) {

                    val idPedido = historial.get("idPedido").toString()
                    val idPrenda = historial.get("idPrenda").toString()
                    val idUsuario = historial.get("idUsuario").toString()
                    val fechaCompra = historial.get("fechaCompra").toString()
                    val latitud = historial.get("latitud").toString()
                    val longitud = historial.get("longitud").toString()
                    val talla = historial.get("talla").toString()
                    Log.e("NO SE", historial.get("talla").toString())
                    Log.e("NO SE", historial.get("estado").toString())
                    val estado = historial.get("estado").toString().toInt()

                    val p = Pedido(
                        idPedido,
                        idPrenda,
                        idUsuario,
                        fechaCompra,
                        latitud,
                        longitud,
                        talla,
                        estado
                    )

                    listaPedidos.add(p)
                }

                recy.adapter = pedidosAdapter
            }*/

        db.collection("pedidos")
        .addSnapshotListener{ snapshot, e->
        listaPedidos.clear()

        for (historial in snapshot!!) {

        val idPedido = historial.get("idPedido").toString()
        val idPrenda = historial.get("idPrenda").toString()
        val idUsuario = historial.get("idUsuario").toString()
        val fechaCompra = historial.get("fechaCompra").toString()
        val latitud = historial.get("latitud").toString()
        val longitud = historial.get("longitud").toString()
        val talla = historial.get("talla").toString()
        Log.e("NO SE", historial.get("talla").toString())
        Log.e("NO SE", historial.get("estado").toString())
        val estado = historial.get("estado").toString().toInt()

        val p = Pedido(idPedido, idPrenda, idUsuario, fechaCompra, latitud, longitud, talla, estado)

        listaPedidos.add(p)
        }

        recy.adapter = pedidosAdapter

        }

    }

    private fun eventoClicFila(pedido: Pedido) {
        abrirPedido(pedido)
    }

    private fun abrirPedido(pedido: Pedido) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.fragmentPedidos, PedidosDetalleFragment(pedido))
        transaction.addToBackStack("pedidos")
        transaction.commit()


    }


}