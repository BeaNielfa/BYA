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

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        recy = root.findViewById(R.id.catalogoRecycler)

        recy.layoutManager = LinearLayoutManager(context)

        anadirPrenda  = root.findViewById(R.id.fabCatalogoAnadir)
        var dialog = Dialog(requireActivity())

        anadirPrenda.setOnClickListener {

            //Abrimos un dialog con las 2 opciones (camara o galeria)
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



        iniciarSwipeHorizontal()
        rellenarArrayPrendas()


        return root
    }



    private fun borrarPrenda(position: Int) {
        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val deleteModel: Prenda = listaPrendas[position]
        prendasAdapter.removeItem(position)
        prendasAdapter.restoreItem(deleteModel, position)


        //Alert dialog para confirmar si desea eliminar la ubicación deslizada
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
     * Si en el alert dialog hemos confirmado que sí queremos eliminar la ubicación
     * lo borramos de la base de datos
     */
    private fun eliminarPrendaConfirmada(position: Int) {

        //SitiosController.delete(SITIOS[position])
        borrarFavoritosPrendaCesta(listaPrendas[position])
        val snackbar = Snackbar.make(
            requireView(),
            "Prenda eliminada con éxito",
            Snackbar.LENGTH_LONG
        )
        prendasAdapter.removeItem(position)
        snackbar.show()
    }

    /**
     * Swipe horizontal que nos servirá para eliminar ya que borramos deslizando
     * a ambos lados
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
                //derecha -> borramos
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

                    //En ambos casos borramos el la ubicación elegida
                    if (dX > 0) {

                        botonIzquierdo(canvas, dX, itemView, width)
                    } else {

                        botonDerecho(canvas, dX, itemView, width)
                    }
                }
                super.onChildDraw(
                    canvas,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recy)
    }


    /**
     * Metodo que al deslizar hacia la izquierda nos abrira un layout
     */
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {

        paintSweep.setColor(resources.getColor(R.color.dark))
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.edit)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * Cuando deslizamos hacia la izquierda aparece un fondo rojo con el botón de eliminar
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de rojo y ponemos el icono
        paintSweep.color = resources.getColor(R.color.dark)
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.delete)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }


    /**
     * Metodo que borra una prenda de la bbdd
     */
    private fun borrarFavoritosPrendaCesta(p: Prenda){

        db.collection("favoritos")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (fav in result) {

                    val idFavorito = fav.get("idFavorito").toString()

                    db.collection("favoritos").document(idFavorito).delete()

                }
            }

        db.collection("cesta")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (cesta in result) {

                    val idCesta = cesta.get("idCesta").toString()

                    db.collection("cesta").document(idCesta).delete()

                }
            }


        db.collection("prendas").document(p.idPrenda).delete()
    }


    private fun editarPrenda(position: Int) {

        //Ocultamos el floating button
        anadirPrenda.hide()

        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val editedModel: Prenda = listaPrendas[position]
        prendasAdapter.removeItem(position)
        prendasAdapter.restoreItem(editedModel, position)

        //llamamos al fragment editar
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, EditarPrendaFragment(listaPrendas[position]))
        transaction.addToBackStack(null)
        transaction.commit()


    }



    /**
     * Rellenamos y devolvemos un array de ubicaciones con todas las ubicaciones que queremos
     * cargar en el recycler
     */
    private fun rellenarArrayPrendas(){

        listaPrendas.clear()

        db.collection("prendas")
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
                }

                //detecta cuando pulsamos en un item
                prendasAdapter = CatalogoListAdapter(listaPrendas) {
                    eventoClicFila(it)
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


    private fun entrarAnadirPrenda(){
        anadirPrenda.hide()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, AnadirPrendaFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun entrarAnadirPrendaExistente(){
        anadirPrenda.hide()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.fragmentCatalogo, AnadirPrendaExistenteFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }


}