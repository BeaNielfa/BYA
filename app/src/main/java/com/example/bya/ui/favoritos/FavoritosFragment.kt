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

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()
        Log.e("PERFIL ",idUsuario)

        //detecta cuando pulsamos en un item
        favoritosAdapter = FavoritosListAdapter(listaFavoritos) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.favoritoRecycler)

        recy.layoutManager = GridLayoutManager(context, 3)

        rellenarArrayFavoritos()


        return root
    }

    private fun rellenarArrayFavoritos() {

        listaFavoritos.clear()

        db.collection("favoritos")
            .whereEqualTo("idUsuario", idUsuario)
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    val idPrenda = fav.get("idPrenda").toString()

                    db.collection("prendas")
                        .whereEqualTo("idPrenda", idPrenda)
                        .get()
                        .addOnSuccessListener { result2 ->

                            for (prenda in result2){
                                val foto = prenda.get("foto").toString()
                                val idTipo = prenda.get("idTipo").toString()
                                val nombre = prenda.get("nombre").toString()
                                val precio = prenda.get("precio").toString()
                                val referencia = prenda.get("referencia").toString()
                                val stock = prenda.get("stock").toString().toInt()

                                val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, stock)

                                Log.e("LISTA", "AÑADO")

                                listaFavoritos.add(p)

                            }

                            recy.adapter = favoritosAdapter
                        }

                }

                recy.adapter = favoritosAdapter

                Log.e("LISTA", "LISTA: " +  listaFavoritos.size.toString())

            }

        Log.e("LISTA", "LISTA: " +  listaFavoritos.size.toString())
    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(prenda: Prenda) {
        abrirPrenda(prenda)
    }

    private fun abrirPrenda(prenda: Prenda) {

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.fragmentFavorito, CatalogoUsuarioDetalleFragment(prenda))
        transaction.addToBackStack(null)
        transaction.commit()

    }




}