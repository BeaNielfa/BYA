package com.example.bya

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //Metemos la imagen del logo
        Picasso.get()
            .load(R.drawable.logo)
            .transform(CirculoTransformacion())
            .into(imgSplashLogo)


        //Cargamos una animacion que va a hacer que el logo aparezca hacia abajo
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        imgSplashLogo.setAnimation(animation1)




        //Lanzamos el splash y ponemos una duración de 3 segundos
        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)

    }

}