package com.example.bya.clases

import java.io.Serializable

class Cesta : Serializable {


    /**
     * VARIABLES
     */
    var idCesta: String
    var idUsuario: String
    var idPrenda: String
    var talla: String


    /**
     * CONSTRUCTOR
     */
    constructor(idCesta: String, idUsuario: String, idPrenda: String, talla: String){
        this.idCesta = idCesta
        this.idUsuario = idUsuario
        this.idPrenda = idPrenda
        this.talla = talla
    }
}
