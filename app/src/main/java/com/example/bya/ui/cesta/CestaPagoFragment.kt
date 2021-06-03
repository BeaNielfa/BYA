package com.example.bya.ui.cesta

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.example.bya.clases.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class CestaPagoFragment(
    var listaCesta: MutableList<Cesta> = mutableListOf<Cesta>(),
    var precioMostrar : Double,
    var latitud : Double,
    var longitud: Double
) : Fragment() {

    private var visa = false
    private var mastercard = false

    private lateinit var etTarjeta: EditText
    private lateinit var etCsv : EditText
    private lateinit var imgVisa : ImageView
    private lateinit var imgMastercard : ImageView

    private var idUsuario = ""

    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_cesta_pago, container, false)

        val imgX : ImageView = root.findViewById(R.id.imgCestaPagoCerrar)
        imgVisa= root.findViewById(R.id.imgCestaPagoVisa)
        imgMastercard = root.findViewById(R.id.imgCestaPagoMastercard)
        val tvTotal : TextView = root.findViewById(R.id.tvCestaPagoTotal)
        val btnPagar : Button = root.findViewById(R.id.btnCestaPagoPagar)
        etTarjeta = root.findViewById(R.id.etCestaPagoTarjeta)
        etCsv = root.findViewById(R.id.etCestaPagoCsv)
        val tvFecha : TextView = root.findViewById(R.id.etCestaPagoFechaElegir)

        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()

        tvTotal.text = precioMostrar.toString() + " EUR"

        imgVisa.setOnClickListener {
            comprobarMetodo()
            imgVisa.setImageResource(R.drawable.visa_seleccionado)
            visa = true
        }

        imgMastercard.setOnClickListener {
            comprobarMetodo()
            imgMastercard.setImageResource(R.drawable.mastercard_seleccionado)
            mastercard = true
        }

        imgX.setOnClickListener {
            btnPagar.visibility = View.INVISIBLE
            btnPagar.isClickable = false
            ubicacion()
        }

        val date = LocalDateTime.now()
        tvFecha.text = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)
        tvFecha.setOnClickListener(){
            val date = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, mYear, mMonth, mDay ->
                    tvFecha.text = (mDay.toString() + "/" + (mMonth + 1) + "/" + mYear)
                }, date.year, date.monthValue - 1, date.dayOfMonth
            )
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show()
        }


        var count = 0
        etTarjeta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (count <= etTarjeta.text.toString().length && (etTarjeta.text.toString()
                        .length === 4 || etTarjeta.text.toString().length === 9 || etTarjeta.text
                        .toString().length === 14)
                ) {
                    etTarjeta.setText(etTarjeta.text.toString().toString() + " ")
                    val pos: Int = etTarjeta.text.length
                    etTarjeta.setSelection(pos)
                } else if (count >= etTarjeta.text.toString().length && (etTarjeta.text.toString()
                        .length === 4 || etTarjeta.text.toString().length === 9 || etTarjeta.text
                        .toString().length === 14)
                ) {
                    etTarjeta.setText(
                        etTarjeta.text.toString().substring(0, etTarjeta.text.toString().length- 1)
                    )
                    val pos: Int = etTarjeta.text.length
                    etTarjeta.setSelection(pos)
                }
                count = etTarjeta.text.toString().length
            }
        })


        btnPagar.setOnClickListener {
            comprobarPago()
        }

        return root
    }

    private fun ubicacion (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaPagoLayout, CestaUbicacionFragment(listaCesta, precioMostrar))
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun comprobarMetodo(){

        imgMastercard.setImageResource(R.drawable.mastercard)
        imgVisa.setImageResource(R.drawable.visa)
        mastercard = false
        visa = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun comprobarPago() {

        var pedido = true
        Log.e("TARJETA", etTarjeta.text.toString().length.toString() +" TARJETITA "+ etTarjeta.text.toString())
        if (etTarjeta.text.isEmpty() || etTarjeta.text.toString().length < 19) {
            pedido = false
            if(etTarjeta.text.isEmpty()) {
                etTarjeta.setError("La tarjeta no puede estar vacía")
            }else {
                if (etTarjeta.text.toString().length < 19) {
                    etTarjeta.setError("La tarjeta debe contener 16 dígitos")
                }
            }
        } else {
            etTarjeta.setError(null)
        }

        if (etCsv.text.isEmpty()|| etCsv.text.toString().length < 3) {
            pedido = false
            if(etCsv.text.isEmpty()) {
                etCsv.setError("El CSV no puede estar vacío")
            }else {
                if (etCsv.text.toString().length < 3) {
                    etCsv.setError("El CSV debe contener 3 dígitos")
                }
            }
        } else {
            etCsv.setError(null)
        }

        if (!visa && !mastercard){
            Toast.makeText(requireActivity(), "Por favor seleccione un método de pago", Toast.LENGTH_SHORT).show()
            pedido = false
        }

        if(pedido){
            etTarjeta.setError(null)
            etCsv.setError(null)

            Toast.makeText(requireActivity(), "¡Compra realizada con éxito!", Toast.LENGTH_SHORT).show()

            val fechaCompra = LocalDateTime.now()
            var fechaBBDD = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(fechaCompra)

            for (i in 0..listaCesta.size -1){
                val idPedido = UUID.randomUUID().toString()

                val p = Pedido(idPedido, listaCesta[i].idPrenda, idUsuario, fechaBBDD.toString(), latitud.toString(),
                    longitud.toString(), listaCesta[i].talla,0)
                db.collection("pedidos").document(idPedido).set(p)
            }

            borrarCesta()
            agradecimientos()

        }else{
            Toast.makeText(requireActivity(), "¡Datos Incorrectos!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun borrarCesta(){
        Log.e("STOCK", listaCesta.size.toString()+"jejeje" )


        db.collection("prendas")
            .get()
            .addOnSuccessListener { result ->


                for (prenda in result) {

                    for(i in 0..listaCesta.size - 1) {

                        Log.e("STOCK", listaCesta[i].idCesta + " EHHH" + listaCesta[i].idPrenda)

                        if (prenda.get("idPrenda").toString().equals(listaCesta[i].idPrenda)) {


                            var stock = prenda.get("stock").toString().toInt()

                            db.collection("prendas").document(listaCesta[i].idPrenda).update("stock", stock - 1)

                        }

                        db.collection("cesta").document(listaCesta[i].idCesta).delete()
                    }



                }




            }
    }

    private fun agradecimientos(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaPagoLayout, CestaAgradecimientoFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }


}