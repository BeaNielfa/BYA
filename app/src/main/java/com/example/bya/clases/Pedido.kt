package com.example.bya.clases
import java.io.Serializable

class Pedido : Serializable {


    /**
     * VARIABLES
     */
    var idPedido: String
    var idPrenda: String
    var idUsuario: String
    var fechaCompra: String
    var latitud: String
    var longitud: String
    var estado: Boolean


    /**
     * CONSTRUCTOR
     */
    constructor(idPedido: String, idPrenda: String, idUsuario:String, fechaCompra: String, latitud: String, longitud: String, estado : Boolean){
        this.idPedido = idPedido
        this.idPrenda = idPrenda
        this.idUsuario = idUsuario
        this.fechaCompra = fechaCompra
        this.latitud = latitud
        this.longitud = longitud
        this.estado = estado
    }

}
