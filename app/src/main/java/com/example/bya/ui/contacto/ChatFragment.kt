package com.example.bya.ui.contacto

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Chat
import com.example.bya.clases.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatFragment(private var u: Usuario, var con: Context) : Fragment() {

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
        val root = inflater.inflate(R.layout.fragment_chat, container, false)

        //Recogemos los componentes del layout en las variables
        var imgChatFoto : ImageView = root.findViewById(R.id.imgChatFoto)
        var imgChatEnviar: ImageView = root.findViewById(R.id.imaChatEnviar)
        var tvChatNombre: TextView = root.findViewById(R.id.tvChatNombre)
        var etChatMensaje : EditText = root.findViewById(R.id.etChatMandar)
        var imgChatAtras : ImageView = root.findViewById(R.id.imgChatAtras)
        recy = root.findViewById(R.id.chatRecycler)

        //Recogemos el idUsuario del usuario activo con las SharedPreferences
        val pref = activity?.getSharedPreferences("Preferencias", Context.MODE_PRIVATE)

        recy.layoutManager = LinearLayoutManager(context)

        //Ponemos como idUsuarioEmisor nuestro id, porque somos la persona que nos hemos
        //logueado y quienes vamos a mandar mensajes desde nuestra app
        idUsuarioEmisor = pref?.getString("idUsuario", "null").toString()


        //Como idUsuarioReceptor ponemos el id del usuario que recibimos en esta clase, que será
        //el id del usuario que se encontraba en la posición en la que hemos hecho click para abrir
        //un chat con el
        idUsuarioReceptor = u.idUsuario


        //Recogemos y ponemos la foto y el nombre del usuario que recibimos (con el que vamos a hablar)
        Picasso.get().load(Uri.parse(u.foto)).transform(CirculoTransformacion()).into(imgChatFoto)
        tvChatNombre.text = u.nombre

        /**
         * Al pulsar en el botón de la flecha nos vamos al fragment anterior
         */
        imgChatAtras.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack("chat", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        /**
         * Al pulsar en el botón enviar
         */
        imgChatEnviar.setOnClickListener{
            var mensaje = etChatMensaje.text.toString()

            //Si el mensaje está vacio
            if (mensaje.isEmpty()){
                etChatMensaje.setText("")
            } else {
                //En caso de que el mensaje no este vacio, enviamos el mensaje
                enviarMensaje(idUsuarioEmisor, idUsuarioReceptor, mensaje)
                etChatMensaje.setText("")
            }
        }

        leerMensaje(idUsuarioEmisor, idUsuarioReceptor)//Leemos el mensaje

        return root
    }


    /**
     * Metodo que recibe, el idEmisor, idReceptor y el mensaje para poder guardarlo en la bbdd
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarMensaje(idEmisor: String, idReceptor: String, mensaje: String){
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
     *  Metodo que recibe el idUsuarioEmisor e idUsuarioReceptor para poder hacer una consulta
     *  en la base de datos y asi poder rescatar los mensajes
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
                            val idEmisora = chat.get("idEmisor").toString()
                            val idReceptora = chat.get("idReceptor").toString()
                            val mensaje = chat.get("mensaje").toString()
                            val fecha = chat.get("fecha").toString()

                            val c = Chat(idEmisora, idReceptora, mensaje, fecha)
                            //Añadimos ese objeto a la lista de chat
                            chatList.add(c)
                    }
                }

                val chatAdapter = ChatListAdapter(con, chatList)
                //Le asignamos el adaptador
                recy.adapter = chatAdapter
            }


    }


}