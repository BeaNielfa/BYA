package com.example.bya.ui.cesta

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    /**
     * VARIABLES
     */
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

        val root = inflater.inflate(R.layout.fragment_cesta_pago, container, false)

        //Enlazamos los elementos con el diseño
        val imgX : ImageView = root.findViewById(R.id.imgCestaPagoCerrar)
        imgVisa= root.findViewById(R.id.imgCestaPagoVisa)
        imgMastercard = root.findViewById(R.id.imgCestaPagoMastercard)
        val tvTotal : TextView = root.findViewById(R.id.tvCestaPagoTotal)
        val btnPagar : Button = root.findViewById(R.id.btnCestaPagoPagar)
        etTarjeta = root.findViewById(R.id.etCestaPagoTarjeta)
        etCsv = root.findViewById(R.id.etCestaPagoCsv)
        val tvFecha : TextView = root.findViewById(R.id.etCestaPagoFechaElegir)

        //Recogemos el idUsuario del usuario activo
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
        idUsuario = pref?.getString("idUsuario", "null").toString()
        tvTotal.text = precioMostrar.toString() + " EUR"

        /**
         * Al pulsar en la imagen de Visa, cambiamos a la imagen seleccionada
         */
        imgVisa.setOnClickListener {
            comprobarMetodo()
            imgVisa.setImageResource(R.drawable.visa_seleccionado)
            visa = true
        }

        /**
         * Al pulsar en la imagen de Mastercard, cambiamos a la imagen seleccionada
         */
        imgMastercard.setOnClickListener {
            comprobarMetodo()
            imgMastercard.setImageResource(R.drawable.mastercard_seleccionado)
            mastercard = true
        }

        /**
         * Al pulsar en la X volvemos al fragment de ubicacion
         */
        imgX.setOnClickListener {
            //Ocultamos el botón de pagar
            btnPagar.visibility = View.INVISIBLE
            btnPagar.isClickable = false
            ubicacion()
        }

        //Recogemos la fecha actual y le damos un formato
        val date = LocalDateTime.now()
        tvFecha.text = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)
        /**
         * Al pulsar en la fecha, se nos abre un dialogo para seleccionar la fecha
         */
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


        /**
         * Le damos un formato al editText del numero de la tarjeta
         */
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


        /**
         * Al pulsar en el botón de pagar, comprobamos el pago
         */
        btnPagar.setOnClickListener {
            comprobarPago()
        }

        return root
    }

    /**
     * Metodo que nos lleva al fragment de ubicacion
     */
    private fun ubicacion (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaPagoLayout, CestaUbicacionFragment(listaCesta, precioMostrar))
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Metodo que pone las imagenes por defecto y las pone a false
     */
    private fun comprobarMetodo(){

        imgMastercard.setImageResource(R.drawable.mastercard)
        imgVisa.setImageResource(R.drawable.visa)
        mastercard = false
        visa = false
    }

    /**
     * Metodo que comprueba el pago
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun comprobarPago() {

        var pedido = true

        //Comprobamos que hayamos introducido valores correctos
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

        //Comprobamos que hemos marcado un metodo de pago
        if (!visa && !mastercard){
            Toast.makeText(requireActivity(), "Por favor seleccione un método de pago", Toast.LENGTH_SHORT).show()
            pedido = false
        }

        //Si hemos rellenado toda la informacion correctamente
        if(pedido){
            //Quitamos los erroers
            etTarjeta.setError(null)
            etCsv.setError(null)

            //Recogemos la fecha actual y le damos un formato
            val fechaCompra = LocalDateTime.now()
            var fechaBBDD = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(fechaCompra)

            //Recorremos la cesta y por cada prenda de la cesta, hacemos un pedido
            for (i in 0..listaCesta.size -1){
                val idPedido = UUID.randomUUID().toString()//Le damos un id al pedido

                //Insertamos el pedido en la bbdd
                val p = Pedido(idPedido, listaCesta[i].idPrenda, idUsuario, fechaBBDD.toString(), latitud.toString(),
                    longitud.toString(), listaCesta[i].talla,0)
                db.collection("pedidos").document(idPedido).set(p)
            }

            Toast.makeText(requireActivity(), "¡Compra realizada con éxito!", Toast.LENGTH_SHORT).show()

            borrarCesta()//Borramos las prendas de la cesta
            agradecimientos()//Nos vamos al fragment de agradecimientos

        }else{
            Toast.makeText(requireActivity(), "¡Datos Incorrectos!", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Metodo que borra la cesta
     */
    private fun borrarCesta(){

        //Consultamos las prendas
        db.collection("prendas")
            .get()
            .addOnSuccessListener { result ->
                //Recorremos todas las prendas de la bbdd
                for (prenda in result) {

                    for(i in 0..listaCesta.size - 1) {
                        //Si la prenda esta en la cesta del usuario
                        if (prenda.get("idPrenda").toString().equals(listaCesta[i].idPrenda)) {

                            //Cantidad de stock que tenemos que eliminar
                            var cantidad = 0
                            var prendaActual = listaCesta[i].idPrenda

                            //Recorremos la lista para ver cuantas prendas iguales hay
                            for(j in 0..listaCesta.size - 1 ){
                                if(listaCesta[j].idPrenda.equals(prendaActual)){
                                    cantidad++
                                }
                            }

                            var stock = prenda.get("stock").toString().toInt()

                            db.collection("prendas").document(listaCesta[i].idPrenda).update("stock", stock - cantidad )

                        }

                    }

                    for(i in 0..listaCesta.size - 1) {

                        //Borramos la cesta en la bbdd
                        db.collection("cesta").document(listaCesta[i].idCesta).delete()

                    }

                }

            }
    }

    /**
     * Metodo que nos lleva al fragment de agradecimientos
     */
    private fun agradecimientos(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaPagoLayout, CestaAgradecimientoFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }


}