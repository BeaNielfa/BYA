package com.example.bya.ui.catalogoUsuario


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.bya.R
import com.example.bya.clases.Favorito
import com.example.bya.clases.Prenda
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_prenda_usuario.view.*


class CatalogoUsuarioListAdapter(private val listaPrendas: MutableList<Prenda>,
                                 private var idUsuario : String,
                                 private val accionPrincipal: (Prenda) -> Unit

) : RecyclerView.Adapter<CatalogoUsuarioListAdapter.PrendaViewHolder>() {

    //Instanciamos la bbdd
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        return PrendaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenda_usuario, parent, false)
        )
    }

    //Rescatamos los datos de una prenda y los ponemos en sus componentes
    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {

        //Asignamos los datos en el item
        holder.tvItemPrendaUsuarioNombre.text = listaPrendas[position].nombre
        holder.tvItemPrendaUsuarioPrecio.text = listaPrendas[position].precio + " EUR"
        Picasso.get().load(Uri.parse(listaPrendas[position].foto)).into(holder.imgItemPrendaUsuario)

        comprobarColor(position, holder)//Comprobamos los favoritos del usuario activo

        holder.imgPrendaUsuarioCorazon.setOnClickListener {

            //Recogemos el tag de la imagen (corazon) de favoritos
            var id = holder.imgPrendaUsuarioCorazon.tag
            var idFavorito = idUsuario + listaPrendas[position].idPrenda//Creamos un id al favorito

            if(id == R.drawable.twitter_like_rojo){//si idFoto esta en rojo
                holder.imgPrendaUsuarioCorazon.setImageResource(R.drawable.twitter_like)//cambiamos el icono
                holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like)//cambiamos el tag
                db.collection("favoritos").document(idFavorito).delete()//Eliminamos de la bbdd el favorito
            } else {

                holder.imgPrendaUsuarioCorazon.setAnimation(R.raw.black_joy)//Asignamos la animacion
                holder.imgPrendaUsuarioCorazon.playAnimation()//La reproducimos
                holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like_rojo)//Cambiamos el tag

                //Añadimos el favorito a la bbdd
                val f = Favorito (idFavorito,idUsuario,listaPrendas[position].idPrenda)//AÑADIMOS EL USUARIO A LA TABLA
                db.collection("favoritos").document(idFavorito).set(f)
            }
        }

        //Cuando pulsamos en un item recogemos su posición para mostrar su detalle
        holder.itemPrenda.setOnClickListener(){
            accionPrincipal(listaPrendas[position])
        }

    }

    /**
     * Metodo que comprueba los favoritos de un usuario
     */
    fun comprobarColor(position: Int, holder: PrendaViewHolder) {

        //Consultamos los favoritos del usuario activo, para marcar los corazones
        db.collection("favoritos")
            .whereEqualTo("idUsuario",  idUsuario)
            .whereEqualTo("idPrenda", listaPrendas[position].idPrenda )
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    holder.imgPrendaUsuarioCorazon.setImageResource(R.drawable.twitter_like_rojo)
                    holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like_rojo)

                }
            }
    }

    /**
     * Metodo que ordena descendente la lista de las prendas
     */
    fun orderByPrecioDes(){
        listaPrendas.sortBy{ it.precio.toFloat() }
        notifyDataSetChanged()
    }

    /**
     * Metodo que ordena ascendente la lista de las prendas
     */
    fun orderByPrecioAsc(){
        listaPrendas.sortByDescending { it.precio.toFloat() }
        notifyDataSetChanged()
    }


    /**
     * Metodo que recibe una lista de prendas y las muestra
     */
    fun filterByName(prenda : List<Prenda>){
        listaPrendas.clear()
        listaPrendas.addAll(prenda)
        notifyDataSetChanged()
    }


    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaPrendas.size
    }

    //Rescatamos los componentes del item
    class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemPrendaUsuarioNombre = itemView.tvItemPrendaUsuarioNombre
        var tvItemPrendaUsuarioPrecio = itemView.tvItemPrendaUsuarioPrecio
        var imgItemPrendaUsuario = itemView.imgItemPrendaUsuario
        var imgPrendaUsuarioCorazon : LottieAnimationView = itemView.imgPrendaUsuarioCorazon

        var itemPrenda = itemView.itemPrendaUsuario
        var context = itemView.context
    }

}