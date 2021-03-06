package com.example.mislugares.presentacion

import android.app.Application
import com.example.mislugares.presentacion.AdaptadorLugaresBD
import com.example.mislugares.datos.LugaresBD
import com.example.mislugares.modelo.GeoPunto

class Aplicacion : Application() {
   val lugares = LugaresBD(this)
   val adaptador  by lazy {
      AdaptadorLugaresBD(lugares, lugares.extraeCursor())
   }

   val posicionActual = GeoPunto(0.0, 0.0)
   var saldo: Int = 0
   override fun onCreate() {
      super.onCreate()
      val pref = getSharedPreferences("pref", MODE_PRIVATE)
      saldo = pref.getInt("saldo_inicial", -1)
   }

}
