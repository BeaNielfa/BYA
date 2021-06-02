package com.example.bya.ui.pedidos

import android.annotation.SuppressLint
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.clases.Pedido
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_anadir_prenda.*

class PedidosDetalleFragment(private val p : Pedido) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    private lateinit var imgPedido : ImageView
    private lateinit var tvIdCliente : TextView
    private lateinit var tvNombre : TextView
    private lateinit var tvTalla : TextView
    private lateinit var tvReferencia : TextView
    private lateinit var btnEnviar : Button

    //Variables Mapa
    private lateinit var mMap: GoogleMap
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = true

    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pedidos_detalle, container, false)

        imgPedido = root.findViewById(R.id.imgDetallePedidoFoto)
        tvIdCliente = root.findViewById(R.id.tvDetallePedidoIdCliente)
        tvNombre = root.findViewById(R.id.tvDetallePedidoNombre)
        tvTalla = root.findViewById(R.id.tvDetallePedidoTalla)
        tvReferencia = root.findViewById(R.id.tvDetallePedidoReferencia)
        val imgX : ImageView = root.findViewById(R.id.imgDetallePedidoCerrar)
        btnEnviar = root.findViewById(R.id.btnDetallePedidoEnviar)

        cargarCampos()
        initMapa()

        btnEnviar.setOnClickListener {

            db.collection("pedidos").document(p.idPedido).update("estado", 1)
            btnEnviar.isEnabled = false
            btnEnviar.setBackgroundColor(resources.getColor(R.color.browser_actions_bg_grey))
            Toast.makeText(requireContext(), "Pedido enviado", Toast.LENGTH_SHORT).show()
        }

        imgX.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("pedidos", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            //pedidos()
        }

        return root
    }

    private fun cargarCampos() {

        if (p.estado == 1){
            btnEnviar.isEnabled = false
            btnEnviar.setBackgroundColor(resources.getColor(R.color.browser_actions_bg_grey))
        }

        db.collection("prendas")
            .whereEqualTo("idPrenda", p.idPrenda)
            .get()
            .addOnSuccessListener { result ->
                for (prenda in result) {

                    val nombre = prenda.get("nombre").toString()
                    val referencia = prenda.get("referencia").toString()
                    val foto = prenda.get("foto").toString()

                    tvIdCliente.text = p.idUsuario
                    tvReferencia.text = referencia
                    tvNombre.text = nombre
                    tvTalla.text = p.talla
                    Picasso.get().load(Uri.parse(foto)).into(imgPedido)

                }
            }


    }


    //Inicialiazamos el mapa
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapaDetallePedidoMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

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
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        //activarEventosMarcadores()
        obtenerPosicion()
    }



    /**
     * Obtiene la posición actual para pasarsela al mapa y que se cargue en nuestra posición
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

    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }





}