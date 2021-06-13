package com.example.bya.ui.pedidos

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class PedidosDetalleFragment(private val p : Pedido) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    /**
     * VARIABLES LAYOUT
     */
    private lateinit var imgPedido : ImageView
    private lateinit var tvIdCliente : TextView
    private lateinit var tvNombre : TextView
    private lateinit var tvTalla : TextView
    private lateinit var tvReferencia : TextView
    private lateinit var btnEnviar : Button

    /**
     * VARIABLES MAPA
     */
    private lateinit var mMap: GoogleMap
    private var posicion: LatLng? = null

    /**
     * INSTANCIA DE LA BBDD DE FIRESTORE
     */
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pedidos_detalle, container, false)

        //Enlazamos los elementos con el diseño
        imgPedido = root.findViewById(R.id.imgDetallePedidoFoto)
        tvIdCliente = root.findViewById(R.id.tvDetallePedidoIdCliente)
        tvNombre = root.findViewById(R.id.tvDetallePedidoNombre)
        tvTalla = root.findViewById(R.id.tvDetallePedidoTalla)
        tvReferencia = root.findViewById(R.id.tvDetallePedidoReferencia)
        val imgX : ImageView = root.findViewById(R.id.imgDetallePedidoCerrar)
        btnEnviar = root.findViewById(R.id.btnDetallePedidoEnviar)

        //Cargamos los campos del detalle del pedido
        cargarCampos()
        //Inicializamos el mapa
        initMapa()

        /**
         * Al pulsar en el botón enviar, cambiamos el estado del pedido
         */
        btnEnviar.setOnClickListener {
            //Actualizamos el estado del pedido a 1 (enviado)
            db.collection("pedidos").document(p.idPedido).update("estado", 1)
            btnEnviar.isEnabled = false//Deshabilitamos el botón
            btnEnviar.setBackgroundColor(resources.getColor(R.color.browser_actions_bg_grey))
            Toast.makeText(requireContext(), "Pedido enviado", Toast.LENGTH_SHORT).show()
        }

        /**
         * Al pulsar en la X volvemos al fragment de pedidos
         */
        imgX.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("pedidos", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        return root
    }

    /**
     * Cargamos los campos del pedido
     */
    private fun cargarCampos() {

        //Si el estado del pedido es 1 o 2
        if (p.estado == 1|| p.estado == 2){
            //Deshabilitamos el botón de enviar
            btnEnviar.isEnabled = false
            btnEnviar.setBackgroundColor(resources.getColor(R.color.browser_actions_bg_grey))
        }

        //Consultamos la prenda del pedido
        db.collection("prendas")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {

                    //Recogemos sus datos
                    val nombre = prenda.get("nombre").toString()
                    val referencia = prenda.get("referencia").toString()
                    val foto = prenda.get("foto").toString()

                    //Los mostramos en el layout
                    tvIdCliente.text = p.idUsuario
                    tvReferencia.text = referencia
                    tvNombre.text = nombre
                    tvTalla.text = p.talla
                    Picasso.get().load(Uri.parse(foto)).into(imgPedido)

                }
            }


    }



    /**
     * Inicialiazamos el mapa
     */
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapaDetallePedidoMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Cargamos el mapa
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    /**
     * Configuración del mapa
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val uiSettings: UiSettings = mMap.uiSettings
        //gestos
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        //brújula
        uiSettings.isCompassEnabled = true
        //zoom
        uiSettings.isZoomControlsEnabled = true
        //barra de herramientas
        uiSettings.isMapToolbarEnabled = true
        //zoom por defecto
        mMap.setMinZoomPreference(14.0f)
        mMap.setOnMarkerClickListener(this)
    }


    /**
     * Cargamos el mapa
     */
    @SuppressLint("MissingPermission")
    private fun cargarMapa() {
        mMap.isMyLocationEnabled = true
        obtenerPosicion()
    }



    /**
     * Obtiene la posición del pedido
     */
    private fun obtenerPosicion() {

        posicion = LatLng(
            p.latitud.toDouble(),
            p.longitud.toDouble()
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));

        mMap.addMarker(
            MarkerOptions() // Posición
                .position(posicion) // Título
                .title("Posición Pedido") // título
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        )

    }

    /**
     * Metodo que se lanza cuando pulsamos en un marcador
     */
    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }





}