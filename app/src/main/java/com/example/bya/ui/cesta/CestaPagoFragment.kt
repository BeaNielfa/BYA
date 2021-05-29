package com.example.bya.ui.cesta

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
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
            datePickerDialog.show()
        }

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

        if (etTarjeta.text.isEmpty()) {
            pedido = false
            etTarjeta.setError("La tarjeta no puede estar vacía")
        } else {
            etTarjeta.setError(null)
        }

        if (etCsv.text.isEmpty()) {
            pedido = false
            etCsv.setError("El CSV no puede estar vacío")
        } else {
            etCsv.setError(null)
        }

        if (!visa && !mastercard){
            Toast.makeText(requireActivity(), "Por favor seleccione un método de pago", Toast.LENGTH_SHORT).show()
            pedido = false
        }

        if(pedido){
            Toast.makeText(requireActivity(), "¡Compra realizada con éxito!", Toast.LENGTH_SHORT).show()

            val fechaCompra = LocalDateTime.now()
            for (i in 0..listaCesta.size -1){
                val idPedido = UUID.randomUUID().toString()

                val p = Pedido(idPedido, listaCesta[i].idPrenda, idUsuario, fechaCompra.toString(), latitud.toString(),
                    longitud.toString(), false)
                db.collection("pedidos").document(idPedido).set(p)
            }

            borrarCesta()
            agradecimientos()

        }else{
            Toast.makeText(requireActivity(), "¡Datos Incorrectos!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun borrarCesta(){
        for(i in 0..listaCesta.size - 1) {
            db.collection("cesta").document(listaCesta[i].idCesta).delete()
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