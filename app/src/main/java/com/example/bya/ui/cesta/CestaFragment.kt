package com.example.bya.ui.cesta

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.google.firebase.firestore.FirebaseFirestore


class CestaFragment : Fragment() {

    private lateinit var recy : RecyclerView
    private var listaCesta = mutableListOf<Cesta>() //Lista de favoritos
    private lateinit var cestaAdapter: CestaListAdapter
    private lateinit var tvPrecio : TextView

    private var idUsuario = ""
    private var precioMostrar : Double = 0.0
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_cesta, container, false)

        tvPrecio = root.findViewById(R.id.tvCestaTotal)
        val btnContinuar : Button = root.findViewById(R.id.btnCestaContinuar)

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()
        Log.e("PERFIL ",idUsuario)

        //detecta cuando pulsamos en un item
        cestaAdapter = CestaListAdapter(listaCesta) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.cestaRecycler)

        recy.layoutManager = LinearLayoutManager(context)

        rellenarArrayCesta()
        rellenarPrecio()

        btnContinuar.setOnClickListener {
            if (listaCesta.size == 0){
                Toast.makeText(requireContext(), "La cesta está vacía", Toast.LENGTH_LONG).show()
            } else {
                btnContinuar.visibility = View.INVISIBLE
                btnContinuar.isClickable = false
                ubicacion()

            }
        }

        return root
    }

    private fun rellenarPrecio(){

        db.collection("prendas")
            .addSnapshotListener{ snapshot, e->
                var precio = 0f
                for (prenda in snapshot!!) {
                    //AQUI TENEMOS TODAS LAS PRENDAS DE LA BASE DE DATOS
                    for (p in 0..listaCesta.size - 1){

                        if (listaCesta[p].idPrenda.equals(prenda.get("idPrenda"))){

                            precio = prenda.get("precio").toString().toFloat() + precio

                        }

                    }
                    precioMostrar = Math. round(precio * 10.0) / 10.0
                    tvPrecio.text = precioMostrar.toString() + " EUR"

                }

            }


    }

    private fun rellenarArrayCesta() {

        db.collection("cesta")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener{ snapshot, e->
                listaCesta.clear()
                for (cesta in snapshot!!) {

                    val idCesta = cesta.get("idCesta").toString()
                    val idPrenda = cesta.get("idPrenda").toString()
                    val talla = cesta.get("talla").toString()

                    val c = Cesta(idCesta, idUsuario, idPrenda, talla)

                    listaCesta.add(c)
                }

                recy.adapter = cestaAdapter

            }


    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(cesta: Cesta) {
        borrarPrenda(cesta)
    }

    private fun borrarPrenda(cesta: Cesta) {

        db.collection("cesta").document(cesta.idCesta).delete()
        rellenarPrecio()

    }

    private fun ubicacion (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCesta, CestaUbicacionFragment(listaCesta, precioMostrar))
        transaction.addToBackStack(null)
        transaction.commit()
    }

}