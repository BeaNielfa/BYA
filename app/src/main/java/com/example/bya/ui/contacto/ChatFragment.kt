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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bya.CirculoTransformacion
import com.example.bya.R
import com.example.bya.clases.Chat
import com.example.bya.clases.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatFragment(private var u: Usuario) : Fragment() {

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
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chat, container, false)

        //Recogemos los componentes del layout en las variables
        var imgChatFoto : ImageView = root.findViewById(R.id.imgChatFoto)
        var imgChatEnviar: ImageView = root.findViewById(R.id.imaChatEnviar)
        var tvChatNombre: TextView = root.findViewById(R.id.tvChatNombre)
        var etChatMensaje : EditText = root.findViewById(R.id.etChatMandar)

        recy = root.findViewById(R.id.chatRecycler)

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

        //Si pulsamos el boton enviar
        imgChatEnviar.setOnClickListener{
            var mensaje = etChatMensaje.text.toString()

            //Si el mensaje está vacio
            if (mensaje.isEmpty()){
                etChatMensaje.setText("")
                //En caso de que el mensaje no este vacio, llamamos al metodo enviarMensaje(...)
            } else {
                enviarMensaje(idUsuarioEmisor, idUsuarioReceptor, mensaje)
                etChatMensaje.setText("")
            }
        }

        leerMensaje(idUsuarioEmisor, idUsuarioReceptor)

        return root
    }

    /*
  Metodo que recibe, el idEmisor, idReceptor y el mensaje para poder guardarlo en la bbdd
   */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun enviarMensaje(idEmisor: String, idReceptor: String, mensaje: String){
        //Instanciamos la base de datos

        var hasMap : HashMap<String, String> = HashMap()
        hasMap.put("idEmisor", idEmisor)
        hasMap.put("idReceptor", idReceptor)
        hasMap.put("mensaje", mensaje)
        hasMap.put("fecha", LocalDateTime.now().toString())

        //val docData: MutableMap<String, Any> = HashMap()

        //docData["listExample"] = Arrays.asList(1, 2, 3)

        //val idC = UUID.randomUUID().toString()//DAMOS UN NOMBRE A LA IMAGEN
        //val c = Chat(idEmisor, idReceptor, mensaje)//AÑADIMOS EL USUARIO A LA TABLA
        db.collection("chat").add(hasMap)

    }

    /*
    Metodo que recibe el idUsuarioEmisor e idUsuarioReceptor para poder hacer una consulta
    en la base de datos y asi poder rescatar el mensaje
     */
    private fun leerMensaje(idEmisor: String, idReceptor: String){

        db.collection("chat")
            .orderBy("fecha")
            .addSnapshotListener{ snapshot, e->
                chatList.clear()

                for (chat in snapshot!!) {

                    if(chat.get("idEmisor").toString().equals(idEmisor) && chat.get("idReceptor").toString().equals(
                            idReceptor
                        ) ||
                        chat.get("idEmisor").toString().equals(idReceptor) && chat.get("idReceptor").toString().equals(
                            idEmisor
                        )){

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

                recy.adapter = chatAdapter
            }


    }


}