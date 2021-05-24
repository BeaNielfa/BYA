package com.example.bya.clases

import java.io.Serializable

class Prenda : Serializable{
    /**
     * VARIABLES
     */
    var nombre: String
    var precio: String
    var referencia: String
    var idPrenda: String
    var idTipo: String
    var foto: String
    var stock: Int


    /**
     * CONSTRUCTOR
     */
    constructor(idPrenda: String, idTipo: String, nombre:String, precio: String, foto: String,referencia: String, stock : Int){
        this.idPrenda = idPrenda
        this.nombre = nombre
        this.idTipo = idTipo
        this.precio = precio
        this.foto = foto
        this.referencia = referencia
        this.stock = stock
    }


}