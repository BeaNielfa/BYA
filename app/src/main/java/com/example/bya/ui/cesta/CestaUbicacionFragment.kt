package com.example.bya.ui.cesta

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.bya.R
import com.example.bya.clases.Cesta
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class CestaUbicacionFragment(
    var listaCesta: MutableList<Cesta> = mutableListOf<Cesta>(),
    var precioMostrar: Double
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    //Variables Mapa
    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_cesta_ubicacion, container, false)

        //Enlazamos los elementos con el diseño
        val imgX : ImageView = root.findViewById(R.id.imgCestaUbicacionCerrar)
        val btnContinuar : Button = root.findViewById(R.id.btnCestaUbicacionContinuar)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


        /**
         * Al pulsar en la X volvemos a la cesta
         */
        imgX.setOnClickListener {
            //Ocultamos el botón continuar
            btnContinuar.visibility = View.INVISIBLE
            btnContinuar.isClickable = false
            cesta()

        }

        /**
         * Al pulsar en el botón continuar nos vamos al fragment del Pago
         */
        btnContinuar.setOnClickListener {
            btnContinuar.visibility = View.INVISIBLE
            btnContinuar.isClickable = false
            pago()
        }

        leerPoscionGPSActual()//Recogemos la posicion actual
        initMapa()//Inicialiazamos el mapa

        return root
    }


    /**
     * Obtiene la posición actual para pasarsela al mapa y que se cargue en nuestra posición
     */
    private fun mirarPosi() {

        val task =  fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        task.addOnSuccessListener {
            if(it != null){
                posicion = LatLng(
                    it.latitude,
                    it.longitude
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
            }else{
                Toast.makeText(requireContext(),"Error al obtener su ubicación, Refresce",Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Leemos la posición actual del GPS
     */
    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    /**
     * Inicialiazamos el mapa
     */
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapaCestaUbicacionMapa) as SupportMapFragment?
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
     * Cuando pulsemos en mapa se nos crea un marcador
     */
    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si está puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posición
                    .position(point) // Título
                    .title("Posición Actual") // título
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    /**
     * Cargamos el mapa
     */
    @SuppressLint("MissingPermission")
    private fun cargarMapa() {
        mMap.isMyLocationEnabled = true
        activarEventosMarcadores()
        //obtenerPosicion()
        mirarPosi()
    }







    /**
     * Metodo que se lanza cuando pulsamos en un marcador
     */
    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }

    /**
     * Metodo que nos lleva al fragment de la cesta
     */
    private fun cesta (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(R.id.cestaUbicacionLayout, CestaFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Metodo que nos lleva al fragment del pago
     */
    private fun pago (){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.replace(
            R.id.cestaUbicacionLayout, CestaPagoFragment(
                listaCesta,
                precioMostrar,
                posicion!!.latitude,
                posicion!!.longitude
            )
        )
        transaction.addToBackStack(null)
        transaction.commit()
    }


}