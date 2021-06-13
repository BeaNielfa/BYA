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
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*


class DevolverFragment : Fragment() {

    /**
     * VARIABLES
     */
    private lateinit var dialog: Dialog
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_devolver, container, false)

        //Enlazamos el recycler con el del layout
        val imgQr : ImageView = root.findViewById(R.id.imgDevolverQr)

        //Instanciamos un dialogo
        dialog = Dialog(requireActivity())

        /**
         * Al pulsar en la imagen del QR, podremos escanear
         */
        imgQr.setOnClickListener {
            scanQRCode()
        }

        return root
    }

    /**
     * Metodo que nos permite escanear un codigo QR
     */
    private fun scanQRCode() {

        val integrator = IntentIntegrator.forSupportFragment(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Escaneando Código")
        }
        //Inicializamos el escaner
        integrator.initiateScan()
    }

    /**
     * Cuando hayamos terminado de escanear
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Recogemos la información del escaner
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {//Si es null, ha habido un error al escanear
            if (result.contents == null){
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
            }
            else {
                //Si ha ido bien, abrimos un dialogo
                dialog.setContentView(R.layout.resultado_devolver_layout)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                //Enlazamos con el diseño
                val imgQr: LottieAnimationView = dialog.findViewById(R.id.imgResultadoDevolver)
                val tvQr : TextView = dialog.findViewById(R.id.tvResultadoDevolver)


                var estado = 0
                var fecha = ""
                var idPedido = ""

                //Consultamos el pedido a devolver
                db.collection("pedidos")
                    .whereEqualTo("idPedido", result.contents)
                    .get()
                    .addOnSuccessListener { result ->
                        for (prenda in result) {
                            //Recogemos su información
                            fecha = prenda.get("fechaCompra").toString()
                            estado = prenda.get("estado").toString().toInt()
                            idPedido = prenda.get("idPedido").toString()

                        }

                        //Le damos un formato a la fecha del pedido
                        val sdf = SimpleDateFormat("dd/MM/yyyy")
                        var fechaComprado = Date(sdf.parse(fecha).getTime())
                        var dia = fechaComprado.date
                        //Obtenemos la fecha limite de devolucion del pedido
                        fechaComprado.date = dia + 15

                        //Obetenemos la fecha actual
                        val fechaHoy = sdf.format(Date())
                        val fechaCompradoMeter = sdf.format(fechaComprado)



                        if(estado == 1){ //Si el estado del pedido es 1
                            if (fechaHoy.toString() <= fechaCompradoMeter.toString()){//Comprobamos que no ha pasado el limite de días devolución
                                imgQr.setAnimation(R.raw.tick)//Le damos la animacion
                                imgQr.playAnimation()//La ejecutamos
                                tvQr.text = "La prenda se ha devuelto correctamente"
                                //Actualizamos el estado a 2 (pedido devuelto)
                                db.collection("pedidos").document(idPedido).update("estado", 2)

                            } else  {//Si ha pasado el limite, indicamos que no se puede devolver
                                imgQr.setAnimation(R.raw.cross)//Le damos la animacion
                                imgQr.playAnimation()//La ejecutamos
                                tvQr.text = "La prenda no se puede devolver, han pasado 15 días desde su compra"
                            }
                        } else if (estado == 2){//Si el estado es 2, no se puede devolver, porque ya esta devuelta
                            imgQr.setAnimation(R.raw.cross)//Le damos la animacion
                            imgQr.playAnimation()//La ejecutamos
                            tvQr.text = "La prenda no se puede devolver, ya ha sido devuelta"
                        } else {//Si no, no se puede devolver porque no se ha realizado su entrega
                            imgQr.setAnimation(R.raw.cross)
                            imgQr.playAnimation()
                            tvQr.text = "La prenda no se puede devolver, todavía no se ha realizado su entrega"
                        }

                    }

                dialog.show()//Mostramos el dialogo



            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


}