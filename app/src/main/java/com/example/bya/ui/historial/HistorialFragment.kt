package com.example.bya.ui.historial

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class HistorialFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var recy : RecyclerView
    private var listaHistorial = mutableListOf<Pedido>() //Lista de favoritos
    private lateinit var historialAdapter: HistorialListAdapter
    private lateinit var dialog: Dialog
    private var idUsuario = ""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_historial, container, false)

        //Recogemos el idUsuario del usuario activo con las SharedPreferences
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        //Creamos una instancia de Dialog
        dialog = Dialog(requireActivity())

        //Inicializamos el adaptador
        historialAdapter = HistorialListAdapter(listaHistorial) {
            eventoClicFila(it)
        }

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.historialRecycler)
        recy.layoutManager = LinearLayoutManager(context)

        //Rellenamos la lista del historial
        rellenarArrayHistorial()

        return root
    }

    /**
     * Metodo que rellena la lista del historial
     */
    private fun rellenarArrayHistorial() {

        //Consultamos el historial del usuario activo
        db.collection("pedidos")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener{ snapshot, e->
                listaHistorial.clear()//limpiamos la lista
                for (historial in snapshot!!) {

                    //Recogemos los datos de la lista
                    val idPedido = historial.get("idPedido").toString()
                    val idPrenda = historial.get("idPrenda").toString()
                    val idUsuario = historial.get("idUsuario").toString()
                    val fechaCompra = historial.get("fechaCompra").toString()
                    val latitud = historial.get("latitud").toString()
                    val longitud = historial.get("longitud").toString()
                    val talla = historial.get("talla").toString()
                    val estado = historial.get("estado").toString().toInt()

                    //Añadimos el pedido a la lista del historial
                    val p = Pedido(idPedido, idPrenda, idUsuario, fechaCompra, latitud, longitud, talla, estado)
                    listaHistorial.add(p)
                }

                //Lo asignamos al adaptador
                recy.adapter = historialAdapter

            }

    }

    /**
     * Al hacer clic en la imagen QR  del pedido
     */
    private fun eventoClicFila(pedido: Pedido) {

        dialog.setContentView(R.layout.codigo_qr_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Enlazamos con el diseño
        val imgQr: ImageView = dialog.findViewById(R.id.imgCodigoQrImagen)
        //Recogemos el idPedido
        var texto = pedido.idPedido
        //Generamos el código QR con el idPedido
        val bitmap = generateQRCode(texto)
        imgQr.setImageBitmap(bitmap)//La asignamos a la imagen

        dialog.show()// mostramos el dialog
    }

    /**
     * Metodo que genera una imagen con el codigo qr
     */
    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("QR", "generateQRCode: ${e.message}")
        }
        return bitmap
    }


}