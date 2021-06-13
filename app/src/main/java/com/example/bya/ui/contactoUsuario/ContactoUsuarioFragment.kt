package com.example.bya.ui.contactoUsuario

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Chat
import com.example.bya.ui.contacto.ChatListAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.time.LocalDateTime

class ContactoUsuarioFragment : Fragment() {

    /**
     * VARIABLES
     */
    var chatList = ArrayList<Chat>()
    var idUsuarioReceptor = ""
    var idUsuarioEmisor = ""
    private lateinit var recy : RecyclerView
    private val db = FirebaseFirestore.getInstance()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_contacto_usuario, container, false)

        //Recogemos los componentes del layout en las variables
        var imgChatFoto : ImageView = root.findViewById(R.id.imgChatUsuarioFoto)
        var imgChatEnviar: ImageView = root.findViewById(R.id.imaChatUsuarioEnviar)
        var tvChatNombre: TextView = root.findViewById(R.id.tvChatUsuarioNombre)
        var etChatMensaje : EditText = root.findViewById(R.id.etChatUsuarioMandar)

        //Enlazamos el recycler con el del layout
        recy = root.findViewById(R.id.chatUsuarioRecycler)
        recy.layoutManager = LinearLayoutManager(context)

        //Consultamos el usuario administrador
        db.collection("usuarios")
            .whereEqualTo("tipo", 0)
            .get()
            .addOnSuccessListener { result ->
                for (usu in result) {

                    //Rescatamos la información del usuario
                    val idUsuario = usu.get("idUsuario").toString()
                    val nombre = usu.get("nombre").toString()
                    val foto = usu.get("foto").toString()

                    //Recogemos el idUsuario del usuario activo con las SharedPreferences
                    val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)
                    idUsuarioEmisor = pref?.getString("idUsuario", "null").toString()

                    //El usuario receptor, va a ser el usuario administrador
                    idUsuarioReceptor = idUsuario

                    //Asignamos la informacion en el layout
                    Picasso.get().load(Uri.parse(foto)).transform(CirculoTransformacion()).into(imgChatFoto)
                    tvChatNombre.text = nombre

                    //Cargamos los mensajes
                    leerMensaje(idUsuarioEmisor, idUsuarioReceptor)
                }
            }

        /**
         * Al pulsar en enviar
         */
        imgChatEnviar.setOnClickListener{
            var mensaje = etChatMensaje.text.toString()

            //Si el mensaje está vacio
            if (mensaje.isEmpty()){
                etChatMensaje.setText("")
            } else { //En caso de que el mensaje no este vacio, enviamos el mensaje
                enviarMensaje(idUsuarioEmisor, idUsuarioReceptor, mensaje)
                etChatMensaje.setText("")
            }
        }

        return root
    }


    /**
     * Metodo que recibe, el idEmisor, idReceptor y el mensaje para poder guardarlo en la bbdd
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarMensaje(idEmisor: String, idReceptor: String, mensaje: String){
        //Actualizamos el valor de chat, para indicar que hemos hablado con el administrador
        db.collection("usuarios").document(idEmisor).update("chat", 1)

        //Creamos un hasMap con los valores del mensaje
        var hasMap : HashMap<String, String> = HashMap()
        hasMap.put("idEmisor", idEmisor)
        hasMap.put("idReceptor", idReceptor)
        hasMap.put("mensaje", mensaje)
        hasMap.put("fecha", LocalDateTime.now().toString())

        //Añadimos el chat(mensaje,idEmisor,idReceptor,fecha) a la bbdd
        db.collection("chat").add(hasMap)

    }


    /**
     * Metodo que recibe el idUsuarioEmisor e idUsuarioReceptor para poder hacer una consulta
     * en la base de datos y asi poder rescatar el mensaje
     */
    private fun leerMensaje(idEmisor: String, idReceptor: String){

        //Consultamos el chat y ordenamos por fecha
        db.collection("chat")
            .orderBy("fecha")
            .addSnapshotListener{ snapshot, e->
                chatList.clear()//Limpiamos la lista

                for (chat in snapshot!!) {

                    //Rescatamos los mensajes si hemos mantenido una conversación
                    if(chat.get("idEmisor").toString().equals(idEmisor) && chat.get("idReceptor").toString().equals(
                            idReceptor
                        ) ||
                        chat.get("idEmisor").toString().equals(idReceptor) && chat.get("idReceptor").toString().equals(
                            idEmisor
                        )){

                        //Recogemos los datos del chat
                        val idEmisor = chat.get("idEmisor").toString()
                        val idReceptor = chat.get("idReceptor").toString()
                        val mensaje = chat.get("mensaje").toString()
                        val fecha = chat.get("fecha").toString()

                        val c = Chat(idEmisor, idReceptor, mensaje, fecha)
                        //Añadimos ese objeto a la lista de chat
                        chatList.add(c)
                    }
                }

                val chatAdapter = ChatListAdapter(requireContext(), chatList)
                //Le asignamos el adaptador
                recy.adapter = chatAdapter
            }


    }


}