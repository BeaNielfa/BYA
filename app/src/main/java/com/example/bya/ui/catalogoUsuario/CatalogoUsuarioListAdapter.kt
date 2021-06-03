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

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrendaViewHolder {
        return PrendaViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_prenda_usuario, parent, false)
        )
    }

    //Rescatamos los datos de una ubicacion y los ponemos en sus componentes
    override fun onBindViewHolder(holder: PrendaViewHolder, position: Int) {

        holder.tvItemPrendaUsuarioNombre.text = listaPrendas[position].nombre
        holder.tvItemPrendaUsuarioPrecio.text = listaPrendas[position].precio + " EUR"

        Picasso.get().load(Uri.parse(listaPrendas[position].foto)).into(holder.imgItemPrendaUsuario)

        comprobarColor(position, holder)
        holder.imgPrendaUsuarioCorazon.setOnClickListener {
            var id = holder.imgPrendaUsuarioCorazon.tag
            var idFavorito = idUsuario + listaPrendas[position].idPrenda

            if(id == R.drawable.twitter_like_rojo){

                //holder.imgPrendaUsuarioCorazon.setAnimation(R.raw.black_joy)
                //holder.imgPrendaUsuarioCorazon.playAnimation()

                holder.imgPrendaUsuarioCorazon.setImageResource(R.drawable.twitter_like)
                holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like)
                db.collection("favoritos").document(idFavorito).delete()
                //pulsado = false
                //Log.e("PULSAR", "IF" + pulsado.toString())
            } else {

                holder.imgPrendaUsuarioCorazon.setAnimation(R.raw.black_joy)
                holder.imgPrendaUsuarioCorazon.playAnimation()

                //holder.imgPrendaUsuarioCorazon.setImageResource(R.drawable.twitter_like_rojo)
                holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like_rojo)
                val f = Favorito (idFavorito,idUsuario,listaPrendas[position].idPrenda)//AÑADIMOS EL USUARIO A LA TABLA
                db.collection("favoritos").document(idFavorito).set(f)
                //pulsado = true
                //Log.e("PULSAR", "ELSE" + pulsado.toString())
            }
        }

        holder.itemPrenda.setOnClickListener(){
            accionPrincipal(listaPrendas[position])
        }

    }

    fun comprobarColor(position: Int, holder: PrendaViewHolder) {

        db.collection("favoritos")
            .whereEqualTo("idUsuario",  idUsuario)
            .whereEqualTo("idPrenda", listaPrendas[position].idPrenda )
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    holder.imgPrendaUsuarioCorazon.setImageResource(R.drawable.twitter_like_rojo)
                    holder.imgPrendaUsuarioCorazon.setTag(R.drawable.twitter_like_rojo)
                    //idFavorito = fav.get("idFavorito").toString()
                    //pulsado = true

                }
            }
    }

    fun orderByPrecioDes(){
        listaPrendas.sortBy{ it.precio.toFloat() }
        notifyDataSetChanged()
    }

    fun orderByPrecioAsc(){
        listaPrendas.sortByDescending { it.precio.toFloat() }
        notifyDataSetChanged()
    }



    fun filterByName(prenda : List<Prenda>){

        listaPrendas.clear()
        listaPrendas.addAll(prenda)
        notifyDataSetChanged()
    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaPrendas.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaPrendas.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Prenda, position: Int) {
        listaPrendas.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaPrendas.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaPrendas.size
    }

    //Rescatamos los tv
    class PrendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvItemPrendaUsuarioNombre = itemView.tvItemPrendaUsuarioNombre
        var tvItemPrendaUsuarioPrecio = itemView.tvItemPrendaUsuarioPrecio
        var imgItemPrendaUsuario = itemView.imgItemPrendaUsuario
        var imgPrendaUsuarioCorazon : LottieAnimationView = itemView.imgPrendaUsuarioCorazon

        var itemPrenda = itemView.itemPrendaUsuario
        var context = itemView.context
    }

}