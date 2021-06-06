package com.example.bya.clases
import java.io.Serializable


class Usuario : Serializable {

    /**
     * VARIABLES
     */
    var idUsuario: String
    var nombre: String
    var email: String
    var pass: String
    var foto: String
    var tipo: Int
    var chat: Int


    /**
     * CONSTRUCTOR
     */
    constructor(idUsuario: String, nombre: String, email:String, pass: String, foto: String){
            this.idUsuario = idUsuario
            this.nombre = nombre
            this.email = email
            this.pass = pass
            this.foto = foto
            this.tipo = 1 //1 USUARIO    0 ADMINISTRADOR
            this.chat = 0 //0 NO  O 1 SI
    }






}