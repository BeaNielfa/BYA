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

    /**
     * ListaReferencias contiene la lista de todas las referencias de las prendas
     * spiReferencia hace referencia al spiner del layout
     * db hace referencia a firestore
     */
    private var listaReferencias = mutableListOf<Int>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var spiReferencia: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_anadir_prenda_existente, container, false)

        //Enlazamos los elementos con el diseño
        val btnGuardar : Button = root.findViewById(R.id.btnAnadirPrendaExistenteGuardarCambios)
        val imgX : ImageView = root.findViewById(R.id.imgAnadirExistenteCerrar)
        val etNombre: EditText = root.findViewById(R.id.etAnadirPrendaExistenteNombre)
        val etPrecio: EditText = root.findViewById(R.id.etAnadirPrendaExistentePrecio)
        spiReferencia = root.findViewById(R.id.spiAnadirPrendaExistenteReferencia)
        val imaFoto : ImageView = root.findViewById(R.id.imgAnadirPrendaExistenteFoto)

        //El precio y el nombre no es editable, por eso esta enabled a false
        etNombre.isEnabled = false
        etPrecio.isEnabled = false
        etNombre.setBackgroundColor(resources.getColor(R.color.dark))
        etPrecio.setBackgroundColor(resources.getColor(R.color.dark))

        //rellenamos las referencias
        rellenarSpinnerReferencia()

        //Al pulsar el botón de la X
        imgX.setOnClickListener {
            volverCatalogo()//volvemos al catalogo
        }

        //Al pulsar el botón guardar
        btnGuardar.setOnClickListener {
            var item = spiReferencia.selectedItem.toString()//recogemos la referencia seleccionada

            //hacemos una consulta de la prenda con esa referencia
            db.collection("prendas")
                .get()
                .addOnSuccessListener { result ->
                    for (prenda in result) {
                        if(prenda.get("referencia").toString().equals(item)){

                            //recogemos el id de la prenda y el stock de la misma
                            var idPrendaActual = prenda.get("idPrenda").toString()
                            var stock = prenda.get("stock").toString().toInt()

                            //Actualizamos el stock ++  de esa misma prenda
                            db.collection("prendas").document(idPrendaActual).update("stock", stock + 1)
                        }

                    }

                }

            Toast.makeText(requireContext(), "Prenda insertada correctamente",Toast.LENGTH_SHORT).show()


        }

        //Al cambiar en el spinner la referencia se llama a este metodo
        spiReferencia.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                var item = spiReferencia.selectedItem.toString()//recogemos la referencia seleccionada

                //Hacemos una consulta de esa prenda para rescatar sus datos
                db.collection("prendas")
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {
                            if(prenda.get("referencia").toString().equals(item)){
                                var foto = prenda.get("foto").toString()
                                var nombre = prenda.get("nombre").toString()
                                var precio = prenda.get("precio").toString()

                                //Y los asignamos al layout
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

    /**
     * Metodo que vuelve al fragment del catalogo
     */
    private fun volverCatalogo(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.anadirPrenda, CatalogoFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Metodo que rellena el spinner con todas las referencias de las prendas
     */
    private fun rellenarSpinnerReferencia() {


        //Consultamos todas las referencias de las predas
        db.collection("prendas")
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {

                    var ref = prenda.get("referencia").toString().toInt()
                    //Las añadimos a la lista
                    listaReferencias.add(ref)

                }

                //Las ordenamos, para que sea más fácil su elección
                listaReferencias.sort()
                //las añadimos en el spinner
                spiReferencia.setAdapter(
                    ArrayAdapter<Int>(
                        requireActivity(),
                        android.R.layout.simple_spinner_dropdown_item,
                        listaReferencias
                    )
                )
            }
    }

}