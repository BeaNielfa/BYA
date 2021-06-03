package com.example.bya.ui.devolver

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.bya.CaptureActivity
import com.example.bya.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_anadir_prenda.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class DevolverFragment : Fragment() {

    private lateinit var dialog: Dialog

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_devolver, container, false)

        val imgQr : ImageView = root.findViewById(R.id.imgDevolverQr)

        dialog = Dialog(requireActivity())

        imgQr.setOnClickListener {
            scanQRCode()
        }

        return root
    }

    private fun scanQRCode() {
        val integrator = IntentIntegrator.forSupportFragment(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Escaneando Código")
        }
        integrator.initiateScan()
    }

    // Get the results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null){
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
            }
            else {

                dialog.setContentView(R.layout.resultado_devolver_layout)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val imgQr: LottieAnimationView = dialog.findViewById(R.id.imgResultadoDevolver)
                val tvQr : TextView = dialog.findViewById(R.id.tvResultadoDevolver)

                var estado = 0
                var fecha = ""
                var idPedido = ""

                db.collection("pedidos")
                    .whereEqualTo("idPedido", result.contents)
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {

                            fecha = prenda.get("fechaCompra").toString()
                            estado = prenda.get("estado").toString().toInt()
                            idPedido = prenda.get("idPedido").toString()

                        }

                        val sdf = SimpleDateFormat("dd/MM/yyyy")
                        var fechaComprado = Date(sdf.parse(fecha).getTime())

                        var dia = fechaComprado.date
                        fechaComprado.date = dia + 15

                        val fechaHoy = sdf.format(Date())
                        val fechaCompradoMeter = sdf.format(fechaComprado)

                        Log.e("FECHA COMPRADO: ", "FECHA COMPRADO: " + fechaCompradoMeter.toString())
                        Log.e("FECHA HOY: ", "FECHA HOY: " + fechaHoy.toString())

                        if(estado == 1){
                            if (fechaHoy.toString() <= fechaCompradoMeter.toString()){
                                imgQr.setAnimation(R.raw.tick)
                                imgQr.playAnimation()
                                tvQr.text = "La prenda se ha devuelto correctamente"
                                db.collection("pedidos").document(idPedido).update("estado", 2)

                            } else  {
                                imgQr.setAnimation(R.raw.cross)
                                imgQr.playAnimation()
                                tvQr.text = "La prenda no se puede devolver, han pasado 15 días desde su compra"
                            }
                        } else if (estado == 2){
                            imgQr.setAnimation(R.raw.cross)
                            imgQr.playAnimation()
                            tvQr.text = "La prenda no se puede devolver, ya ha sido devuelta"
                        } else {
                            imgQr.setAnimation(R.raw.cross)
                            imgQr.playAnimation()
                            tvQr.text = "La prenda no se puede devolver, todavía no se ha realizado su entrega"
                        }

                    }

                dialog.show()

                Log.e("ESCANEADO", result.contents)
               
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}