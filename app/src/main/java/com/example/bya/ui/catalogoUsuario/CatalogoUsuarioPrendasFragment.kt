package com.example.bya.ui.catalogoUsuario

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class CatalogoUsuarioPrendasFragment(private val tipo: String,private val catalogo: Int) : Fragment() {


    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaPrendas = mutableListOf<Prenda>() //Lista de prendas
    private var listaPrendas2 = mutableListOf<Prenda>() //Lista de prendas
    private lateinit var prendasAdapter: CatalogoUsuarioListAdapter //Adaptador de prendas
    private var idUsuario = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_catalogo_usuario_prendas, container, false)

        //Enlazamos los elementos con el diseño
        val searchField : SearchView = root.findViewById(R.id.searchField)
        val spiFiltro : Spinner = root.findViewById(R.id.spiFiltro)
        val imgAtras : ImageView = root.findViewById(R.id.imgCatalogoUsuarioAtras)

        //Recogemos el idUsuario del usuario activo
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()


        //Inicializamos el adaptador
        prendasAdapter = CatalogoUsuarioListAdapter(listaPrendas, idUsuario) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.catalogoUsuarioRecycler)
        recy.layoutManager = GridLayoutManager(context, 2)

        //Rellenamos la lista de prendas
        rellenarArrayPrendas()

        /**
         * Al pulsar en este botón volvemos al fragment anterior
         */
        imgAtras.setOnClickListener {
            if(catalogo == 0){//Si es 0 volvemos al catalogo de mujeres
                requireActivity().supportFragmentManager.popBackStack("mujer", FragmentManager.POP_BACK_STACK_INCLUSIVE)

            }else{//si no, volvemos al catálogo de hombres
                requireActivity().supportFragmentManager.popBackStack("hombre", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }

        }

        /**
         * Cuando escribimos en la barra de búsqueda
         */
        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            //Cuando cambia el texto crea una nueva lista con las prendas que coincidan con ese filtro
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if(newText.equals("")){
                        prendasAdapter.filterByName(listaPrendas2)
                    } else {
                        val filteredList = listaPrendas.filter {
                            it.nombre.toLowerCase(Locale.getDefault()).contains(newText)
                        }
                        prendasAdapter.filterByName(filteredList)
                    }

                }
                return false
            }
        })

        /**
         * Cuando selecionamos una opción del spinner del filtro, ordena la lista de prendas por precio mayor o menor
         */
        spiFiltro.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                var item = spiFiltro.selectedItem.toString()

                if(item.equals("Precio: Mayor")){
                    prendasAdapter.orderByPrecioAsc()
                }
                if (item.equals("Precio: Menor")) {
                    prendasAdapter.orderByPrecioDes()
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

        return root
    }

    /**
     * Rellenamos y devolvemos un array con las prendas que queremos cargar en el recycler
     */
    private fun rellenarArrayPrendas(){

        //Limpiamos las listas
        listaPrendas.clear()
        listaPrendas2.clear()

        db.collection("prendas")//Consultamos todas las prendas de un determinado tipo
            .whereEqualTo("idTipo", tipo)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {
                    //Recogemos los datos de la prenda
                    val idPrenda = prenda.get("idPrenda").toString()
                    val nombre = prenda.get("nombre").toString()
                    val precio = prenda.get("precio").toString()
                    val idTipo = prenda.get("idTipo").toString()
                    val referencia = prenda.get("referencia").toString()
                    val stock = prenda.get("stock").toString()
                    val foto = prenda.get("foto").toString()

                    //Añadimos la prenda a las listas
                    val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, stock.toInt())
                    listaPrendas.add(p)
                    listaPrendas2.add(p)
                }


                //Le indicamos el adaptador
                recy.adapter = prendasAdapter

            }



    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(prenda: Prenda) {
        abrirPrenda(prenda)
    }

    /**
     * Abrimos el detalle de la prenda, pasandole la prenda y el fragment de donde venimos
     */
    private fun abrirPrenda(prenda: Prenda) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.catalogoUsuarioListaLayout,
            CatalogoUsuarioDetalleFragment(prenda, 0)

        )
        transaction.addToBackStack("catalogo")
        transaction.commit()


    }


}