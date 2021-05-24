package com.example.bya.ui.catalogo

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso



class AnadirPrendaExistenteFragment : Fragment() {

    private var listaReferencias = mutableListOf<String>()

    private val db = FirebaseFirestore.getInstance()

    private lateinit var spiReferencia: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_anadir_prenda_existente, container, false)

        val btnGuardar : Button = root.findViewById(R.id.btnAnadirPrendaExistenteGuardarCambios)
        val imgX : ImageView = root.findViewById(R.id.imgAnadirExistenteCerrar)
        val etNombre: EditText = root.findViewById(R.id.etAnadirPrendaExistenteNombre)
        val etPrecio: EditText = root.findViewById(R.id.etAnadirPrendaExistentePrecio)
        spiReferencia = root.findViewById(R.id.spiAnadirPrendaExistenteReferencia)
        val imaFoto : ImageView = root.findViewById(R.id.imgAnadirPrendaExistenteFoto)

        etNombre.isEnabled = false
        etPrecio.isEnabled = false
        etNombre.setBackgroundColor(resources.getColor(R.color.dark))
        etPrecio.setBackgroundColor(resources.getColor(R.color.dark))

        rellenarSpinnerReferencia()

        imgX.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.anadirPrenda, CatalogoFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        btnGuardar.setOnClickListener {
            var item = spiReferencia.selectedItem.toString()

            db.collection("prendas")
                .get()
                .addOnSuccessListener { result ->
                    for (prenda in result) {
                        if(prenda.get("referencia").toString().equals(item)){

                            var idPrendaActual = prenda.get("idPrenda").toString()
                            var stock = prenda.get("stock").toString().toInt()

                            db.collection("prendas").document(idPrendaActual).update("stock", stock + 1)
                        }

                    }

                }


        }

        spiReferencia.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                var item = spiReferencia.selectedItem.toString()
                db.collection("prendas")
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {
                            if(prenda.get("referencia").toString().equals(item)){
                                var foto = prenda.get("foto").toString()
                                var nombre = prenda.get("nombre").toString()
                                var precio = prenda.get("precio").toString()

                                Picasso.get().load(Uri.parse(foto)).into(imaFoto)
                                etNombre.setText(nombre)
                                etPrecio.setText(precio + " EUR")
                            }

                        }

                    }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

        return root

    }

    private fun rellenarSpinnerReferencia() {


        db.collection("prendas")
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {

                    var ref = prenda.get("referencia").toString()

                    listaReferencias.add(ref)

                }
                //las aÃ±adimos en el spinner
                spiReferencia.setAdapter(
                    ArrayAdapter<String>(
                        requireActivity(),
                        android.R.layout.simple_spinner_dropdown_item,
                        listaReferencias
                    )
                )
            }
    }

}