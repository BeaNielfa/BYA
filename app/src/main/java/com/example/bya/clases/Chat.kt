package com.example.bya.clases

import java.io.Serializable

class Chat  : Serializable {

    /**
     * VARIABLES
     */
    var idEmisor: String
    var idReceptor: String
    var mensaje: String
    var fecha: String

    /**
     * CONSTRUCTOS
     */
    constructor(idEmisor: String, idReceptor: String, mensaje:String, fecha: String){
        this.idEmisor = idEmisor
        this.idReceptor = idReceptor
        this.mensaje = mensaje
        this.fecha = fecha

    }

}