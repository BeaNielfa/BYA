package com.example.bya.ui.favoritos

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.example.bya.ui.catalogoUsuario.CatalogoUsuarioDetalleFragment
import com.google.firebase.firestore.FirebaseFirestore


class FavoritosFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaFavoritos = mutableListOf<Prenda>() //Lista de favoritos
    private lateinit var favoritosAdapter: FavoritosListAdapter
    private var idUsuario = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_favorito, container, false)

        //Recogemos el idUsuario del usuario activo con las SharedPreferences
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()



        //Inicializamos el adaptador
        favoritosAdapter = FavoritosListAdapter(listaFavoritos) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.favoritoRecycler)
        recy.layoutManager = GridLayoutManager(context, 3)

        //Rellenamos la lista de favoritos
        rellenarArrayFavoritos()


        return root
    }

    /**
     * Metodo que rellena la lista de favoritos
     */
    private fun rellenarArrayFavoritos() {

        listaFavoritos.clear()//Limpiamos la lista

        //Consultamos los favoritos del usuario activo
        db.collection("favoritos")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener{ snapshot, e->
                listaFavoritos.clear()//Limpiamos la lista
                for (fav in snapshot!!) {

                    val idPrenda = fav.get("idPrenda").toString()//Recogemos el id de la prenda

                    //Consultamos los datos de la prenda
                    db.collection("prendas")
                        .whereEqualTo("idPrenda", idPrenda)
                        .addSnapshotListener { snap, a->
                            for (prenda in snap!!){
                                //Recogemos los datos
                                val foto = prenda.get("foto").toString()
                                val idTipo = prenda.get("idTipo").toString()
                                val nombre = prenda.get("nombre").toString()
                                val precio = prenda.get("precio").toString()
                                val referencia = prenda.get("referencia").toString()
                                val stock = prenda.get("stock").toString().toInt()

                                //Añadimos la prendaa a la lista
                                val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, stock)
                                listaFavoritos.add(p)

                            }
                            //Lo asignamos al adapter
                            recy.adapter = favoritosAdapter

                        }


                }

            }

    }

    /**
     * Se llama cuando hacemos clic en una prenda, para abrir su detalle
     */
    private fun eventoClicFila(prenda: Prenda) {
        abrirPrenda(prenda)
    }

    /**
     * Metodo que nos lleva al fragment del detalle de la prenda
     */
    private fun abrirPrenda(prenda: Prenda) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.fragmentFavorito, CatalogoUsuarioDetalleFragment(prenda,1))
        transaction.addToBackStack("favoritos")
        transaction.commit()

    }



}