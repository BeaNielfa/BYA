package com.example.bya.ui.cesta

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat


class CestaFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaCesta = mutableListOf<Cesta>() //Lista de cesta
    private lateinit var cestaAdapter: CestaListAdapter
    private lateinit var tvPrecio : TextView
    private var idUsuario = ""
    private var precioMostrar : Double = 0.0
    private var pre =""
    private val db = FirebaseFirestore.getInstance()
    private var precio = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root =  inflater.inflate(R.layout.fragment_cesta, container, false)

        //Enlazamos los elementos con el diseño
        tvPrecio = root.findViewById(R.id.tvCestaTotal)
        val btnContinuar : Button = root.findViewById(R.id.btnCestaContinuar)

        //Recogemos el idUsuario del usuario activo
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Inicializamos el adaptador
        cestaAdapter = CestaListAdapter(listaCesta) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.cestaRecycler)
        recy.layoutManager = LinearLayoutManager(context)

        //Rellenamos la lista de la cesta y el precio
        rellenarArrayCesta()
        rellenarPrecio()

        /**
         * Al pulsar en el botón continuar
         */
        btnContinuar.setOnClickListener {
            if (listaCesta.size == 0){//Comprobamos que la lista no este vacia
                Toast.makeText(requireContext(), "La cesta está vacía", Toast.LENGTH_LONG).show()
            } else {
                //Ocultamos el boton de continuar
                btnContinuar.visibility = View.INVISIBLE
                btnContinuar.isClickable = false
                ubicacion()//Nos vamos al fragment de ubicacion

            }
        }

        return root
    }

    /**
     * Metodo que rellena el precio en función de los precios de las prendas que tengamos en la cesta
     */
    private fun rellenarPrecio(){

        //Consultamos las prendas
        db.collection("prendas")
            .addSnapshotListener{ snapshot, e->
                precio = 0f
                for (prenda in snapshot!!) {
                    //AQUI TENEMOS TODAS LAS PRENDAS DE LA BASE DE DATOS
                    for (p in 0..listaCesta.size - 1){

                        //si la prenda esta en nuestra cesta
                        if (listaCesta[p].idPrenda.equals(prenda.get("idPrenda"))){
                            //Sumamos su precio al que ya teniamos
                            precio = prenda.get("precio").toString().toFloat() + precio

                        }

                    }
                    //PASAMOS EL NÚMERO A DOS DECIMALES
                    decimal()
                    tvPrecio.text = pre + " EUR"//LO ASIGNAMOS
                    pre = pre.replace(',', '.');//REEMPLAZAMOS LA COMA POR EL PUNTO
                    precioMostrar = pre.toDouble()

                }

            }


    }

    /**
     * Metodo que trunca un numero y lo saca con dos decimales
     */
    private fun decimal (){
         val format = DecimalFormat()
         format.setMaximumFractionDigits(2) //Define 2 decimales.
         pre = format.format(precio).toString()
    }

    /**
     * Metodo que rellena la lista de la cesta
     */
    private fun rellenarArrayCesta() {

        //Consultamos la cesta del usuario activo
        db.collection("cesta")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener{ snapshot, e->

                listaCesta.clear()//Limpiamos la cesta
                for (cesta in snapshot!!) {

                    //Recogemos los valores de la cesta
                    val idCesta = cesta.get("idCesta").toString()
                    val idPrenda = cesta.get("idPrenda").toString()
                    val talla = cesta.get("talla").toString()

                    //Añadimos la cesta a la lista
                    val c = Cesta(idCesta, idUsuario, idPrenda, talla)
                    listaCesta.add(c)
                }

                //Le indicamos el adaptador
                recy.adapter = cestaAdapter

            }


    }

    /**
     * Se llama cuando hacemos clic en el botón X
     */
    private fun eventoClicFila(cesta: Cesta) {
        borrarPrenda(cesta)
    }

    /**
     * Metodo que borra una prenda de la cesta de la bbdd
     */
    private fun borrarPrenda(cesta: Cesta) {

        db.collection("cesta").document(cesta.idCesta).delete()
        rellenarPrecio()//Volvemos a rellenar el precio

    }

    /**
     * Metodo que nos lleva al fragment de ubicacion
     */
    private fun ubicacion (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCesta, CestaUbicacionFragment(listaCesta, precioMostrar))
        transaction.addToBackStack(null)
        transaction.commit()
    }

}