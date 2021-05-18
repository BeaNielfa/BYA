package com.example.bya.clases

import java.io.Serializable

class Prenda : Serializable{
    /**
     * VARIABLES
     */
    var nombre: String
    var precio: Float
    var referencia: Int
    var idPrenda: String
    var idTipo: String
    var foto: String
    var stock: Int


    /**
     * CONSTRUCTOR
     */
    constructor(idPrenda: String, idTipo: String, nombre:String, precio: Float, foto: String,referencia: Int, stock : Int){
        this.idPrenda = idPrenda
        this.nombre = nombre
        this.idTipo = idTipo
        this.precio = precio
        this.foto = foto
        this.referencia = referencia
        this.stock = stock
    }


}