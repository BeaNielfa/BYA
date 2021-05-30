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


        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        dialog = Dialog(requireActivity())

        //detecta cuando pulsamos en un item
        historialAdapter = HistorialListAdapter(listaHistorial) {
            eventoClicFila(it)
        }

        recy = root.findViewById(R.id.historialRecycler)

        recy.layoutManager = LinearLayoutManager(context)

        rellenarArrayHistorial()

        return root
    }

    private fun rellenarArrayHistorial() {

        db.collection("pedidos")
            .whereEqualTo("idUsuario", idUsuario)
            .addSnapshotListener{ snapshot, e->
                listaHistorial.clear()
                for (historial in snapshot!!) {

                    val idPedido = historial.get("idPedido").toString()
                    val idPrenda = historial.get("idPrenda").toString()
                    val idUsuario = historial.get("idUsuario").toString()
                    val fechaCompra = historial.get("fechaCompra").toString()
                    val latitud = historial.get("latitud").toString()
                    val longitud = historial.get("longitud").toString()
                    val estado = historial.get("estado").toString().toBoolean()

                    val p = Pedido(idPedido, idPrenda, idUsuario, fechaCompra, latitud, longitud, estado)

                    listaHistorial.add(p)
                }

                recy.adapter = historialAdapter

            }

    }

    private fun eventoClicFila(pedido: Pedido) {

        dialog.setContentView(R.layout.codigo_qr_layout)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imgQr: ImageView = dialog.findViewById(R.id.imgCodigoQrImagen)

        var texto = "idPedido: " + pedido.idPedido + "\nidPrenda: " + pedido.idPrenda +
                "\nidUsuario: " + pedido.idUsuario + "\nfechaCompra: " + pedido.fechaCompra +
                "\nlatitud: " + pedido.latitud + "\nlongitud: " + pedido.longitud + "\nestado:" + pedido.estado

        val bitmap = generateQRCode(texto)
        imgQr.setImageBitmap(bitmap)

        dialog.show()
    }

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