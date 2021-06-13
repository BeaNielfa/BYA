package com.example.bya.ui.catalogo

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Prenda
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore



class CatalogoFragment : Fragment() {

    /**
     * Variables
     */
    private lateinit var anadirPrenda : FloatingActionButton
    private lateinit var recy : RecyclerView
    private var listaPrendas = mutableListOf<Prenda>() //Lista de prendas
    private lateinit var prendasAdapter: CatalogoListAdapter //Adaptador de prendas
    private var paintSweep = Paint()
    private val db = FirebaseFirestore.getInstance()
    private var idUsuario = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_catalogo, container, false)

        //Recogemos el idUsuario del usuario activo con las SharedPreferences
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.catalogoRecycler)
        recy.layoutManager = LinearLayoutManager(context)
        anadirPrenda  = root.findViewById(R.id.fabCatalogoAnadir)

        var dialog = Dialog(requireActivity())//Instanciamos un dialogo

        //Al pulsar en añadir prenda, se abrirá un dialogo
        anadirPrenda.setOnClickListener {

            //Abrimos un dialog con las 2 opciones (prenda existente, prenda nueva)
            dialog.setContentView(R.layout.anadir_prenda_existente_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan los datos del layout
            var imgExistente: ImageView = dialog.findViewById(R.id.imgPrendaExistente)
            var imgNueva: ImageView = dialog.findViewById(R.id.imgPrendaNueva)
            var tvExistente: TextView = dialog.findViewById(R.id.tvPrendaExistente)
            var tvNueva: TextView = dialog.findViewById(R.id.tvPrendaNueva)


            /**
             * boton para anadir una prenda ya existente en la bbdd
             */
            imgExistente.setOnClickListener(){
                entrarAnadirPrendaExistente()
                dialog.dismiss()
            }

            /**
             * boton para añadir una nueva prenda en la bbdd
             */
            imgNueva.setOnClickListener(){
                entrarAnadirPrenda()
                dialog.dismiss()
            }

            /**
             * boton para anadir una prenda ya existente en la bbdd
             */
            tvExistente.setOnClickListener(){
                entrarAnadirPrendaExistente()
                dialog.dismiss()
            }

            /**
             * boton para añadir una nueva prenda en la bbdd
             */
            tvNueva.setOnClickListener(){
                entrarAnadirPrenda()
                dialog.dismiss()
            }

            dialog.show()
        }


        //Iniciamos el SwipeHorizontal y rellenamos la lista  de prendas
        iniciarSwipeHorizontal()
        rellenarArrayPrendas()


        return root
    }


    /**
     * Metodo que elimina una prenda del recycler y de la bbdd
     */
    private fun borrarPrenda(position: Int) {
        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val deleteModel: Prenda = listaPrendas[position]
        prendasAdapter.removeItem(position)
        prendasAdapter.restoreItem(deleteModel, position)


        //Alert dialog para confirmar si desea eliminar la prenda deslizada
        Log.i("Elimar", "Eliminando...")
        AlertDialog.Builder(requireContext())
            .setIcon(R.mipmap.ic_launcher_bya_round)
            .setTitle("Eliminar prenda")
            .setMessage("¿Desea eliminar la prenda existente?")
            .setPositiveButton("Sí"){ dialog, which -> eliminarPrendaConfirmada(position)}
            .setNegativeButton("No", null)
            .show()

    }

    /**
     * Si en el alert dialog hemos confirmado que sí queremos eliminar la prenda
     * la borramos de la base de datos
     */
    private fun eliminarPrendaConfirmada(position: Int) {

        borrarFavoritosPrendaCesta(listaPrendas[position])
        val snackbar = Snackbar.make(requireView(), "Prenda eliminada con éxito", Snackbar.LENGTH_LONG)
        prendasAdapter.removeItem(position)
        snackbar.show()
    }

    /**
     * Swipe horizontal que nos servirá para eliminar o editar
     */
    private fun iniciarSwipeHorizontal() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            //Según donde deslizemos
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                //izquierda -> borramos
                //derecha -> editamos
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        borrarPrenda(position)
                    }
                    else -> {
                        editarPrenda(position)
                    }
                }
            }




            /**
             * Se crea el dibujo cuando deslizamos
             */
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3


                    if (dX > 0) {//Creamos el boton de borrar
                        botonIzquierdo(canvas, dX, itemView, width)
                    } else {//Creamos el boton de editar
                        botonDerecho(canvas, dX, itemView, width)
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recy)
    }


    /**
     * Metodo que al deslizar hacia la izquierda nos dibuja el botón
     */
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {

        //Le damos un color y un fondo
        paintSweep.setColor(resources.getColor(R.color.dark))
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        //Le damos un icono para que lo muestre
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.edit)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Cuando deslizamos hacia la derecha nos dibuja el botón de editar
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        //Le damos un color y un fondo
        paintSweep.color = resources.getColor(R.color.dark)
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        //Le damos un icono para que lo muestre
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.delete)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }


    /**
     * Metodo que borra un favorito de la bbdd
     */
    private fun borrarFavoritosPrendaCesta(p: Prenda){

        //Hacemos una consulta para borrar todos los favoritos que tengan ese idPrenda
        db.collection("favoritos")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    val idFavorito = fav.get("idFavorito").toString()

                    db.collection("favoritos").document(idFavorito).delete()

                }
            }

        //Hacemos una consulta para borrar todos las prendas de la cesta que tengan ese idPrenda
        db.collection("cesta")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (cesta in result) {

                    val idCesta = cesta.get("idCesta").toString()

                    db.collection("cesta").document(idCesta).delete()

                }
            }

        //Borramos la prenda
        db.collection("prendas").document(p.idPrenda).delete()
    }


    /**
     * Metodo para editar una prenda
     */
    private fun editarPrenda(position: Int) {

        //Ocultamos el floating button
        anadirPrenda.hide()

        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val editedModel: Prenda = listaPrendas[position]
        prendasAdapter.removeItem(position)
        prendasAdapter.restoreItem(editedModel, position)

        //llamamos al fragment editar
        editar(position)


    }

    /**
     * Metodo que abre el fragment de editar, pasandole la prenda a editar
     */
    private fun editar(position: Int){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, EditarPrendaFragment(listaPrendas[position]))
        transaction.addToBackStack(null)
        transaction.commit()
    }



    /**
     * Rellenamos y devolvemos un array con las prendas que queremos cargar en el recycler
     */
    private fun rellenarArrayPrendas(){

        listaPrendas.clear()//Limpiamos la lista

        db.collection("prendas")//Consultamos todas las prendas
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

                    //Añadimos la prenda a la lista
                    val p = Prenda(idPrenda, idTipo, nombre, precio, foto, referencia, stock.toInt())
                    listaPrendas.add(p)
                }

                //Le damos valor a prendasAdapter
                prendasAdapter = CatalogoListAdapter(listaPrendas)

                //Le indicamos el adaptador
                recy.adapter = prendasAdapter

            }



    }

    /**
     * Metodo que entra en el fragment de añadir una prenda nueva
     */
    private fun entrarAnadirPrenda(){
        anadirPrenda.hide()//ocultamos el boton floating
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, AnadirPrendaFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     *  Metodo que entra en el fragment de añadir una prenda ya existente
     */
    private fun entrarAnadirPrendaExistente(){
        anadirPrenda.hide()//ocultamos el boton floating
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, AnadirPrendaExistenteFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }


}