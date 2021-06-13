package com.example.bya.clases

import java.io.Serializable

class Favorito : Serializable {

    /**
     * VARIABLES
     */
    var idFavorito: String
    var idUsuario: String
    var idPrenda: String


    /**
     * CONSTRUCTOR
     */
    constructor(idFavorito: String, idUsuario: String, idPrenda: String){
        this.idFavorito = idFavorito
        this.idUsuario = idUsuario
        this.idPrenda = idPrenda
    }
}