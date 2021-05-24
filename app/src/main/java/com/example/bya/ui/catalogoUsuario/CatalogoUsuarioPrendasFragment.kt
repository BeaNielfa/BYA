package com.example.bya.ui.catalogoUsuario

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SearchView
import android.widget.Spinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class CatalogoUsuarioPrendasFragment(private val tipo: String) : Fragment() {

    private lateinit var recy : RecyclerView

    private var listaPrendas = mutableListOf<Prenda>() //Lista de ubicaciones
    private var listaPrendas2 = mutableListOf<Prenda>() //Lista de ubicaciones
    private lateinit var prendasAdapter: CatalogoUsuarioListAdapter //Adaptador de ubicaciones

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo_usuario_prendas, container, false)

        val searchField : SearchView = root.findViewById(R.id.searchField)
        val spiFiltro : Spinner = root.findViewById(R.id.spiFiltro)

        //detecta cuando pulsamos en un item
        prendasAdapter = CatalogoUsuarioListAdapter(listaPrendas) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.catalogoUsuarioRecycler)

        recy.layoutManager = GridLayoutManager(context, 2)

        rellenarArrayPrendas()

        searchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

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
     * Rellenamos y devolvemos un array de ubicaciones con todas las ubicaciones que queremos
     * cargar en el recycler
     */
    private fun rellenarArrayPrendas(){

        listaPrendas.clear()
        listaPrendas2.clear()

        db.collection("prendas")
            .whereEqualTo("idTipo", tipo)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {
                    Log.e("LISTAAA1", "ME METO")
                    val idPrenda = prenda.get("idPrenda").toString()
                    val nombre = prenda.get("nombre").toString()
                    val precio = prenda.get("precio").toString()
                    val idTipo = prenda.get("idTipo").toString()
                    val referencia = prenda.get("referencia").toString()
                    val stock = prenda.get("stock").toString()
                    val foto = prenda.get("foto").toString()

                    val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, stock.toInt())

                    listaPrendas.add(p)
                    listaPrendas2.add(p)
                }



                recy.adapter = prendasAdapter

            }



    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(prenda: Prenda) {
        //abrirPrenda(prenda)
    }



}
