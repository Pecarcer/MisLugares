package com.example.mislugares.presentacion

import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mislugares.R
import com.example.mislugares.datos.RepositorioLugares
import com.example.mislugares.modelo.GeoPunto
import com.example.mislugares.modelo.Lugar
import com.example.mislugares.modelo.TipoLugar
import kotlinx.android.synthetic.main.elemento_lista.view.*

open class AdaptadorLugares(
   val lugares: RepositorioLugares
   ) :
                  RecyclerView.Adapter<AdaptadorLugares.ViewHolder>() {

   /*open*/ lateinit var onClick: (View) -> Unit
   class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {


      fun personaliza(lugar: Lugar, onClick: (View) -> Unit) = with(itemView) {
         nombre.text = lugar.nombre
         direccion.text = lugar.direccion
         foto.setImageResource(
            when (lugar.tipoLugar) {
               TipoLugar.RESTAURANTE -> R.drawable.restaurante
               TipoLugar.BAR -> R.drawable.bar
               TipoLugar.COPAS -> R.drawable.copas
               TipoLugar.ESPECTACULO -> R.drawable.espectaculos
               TipoLugar.HOTEL -> R.drawable.hotel
               TipoLugar.COMPRAS -> R.drawable.compras
               TipoLugar.EDUCACION -> R.drawable.educacion
               TipoLugar.DEPORTE -> R.drawable.deporte
               TipoLugar.NATURALEZA -> R.drawable.naturaleza
               TipoLugar.GASOLINERA -> R.drawable.gasolinera
               TipoLugar.OTROS -> R.drawable.otros
            }
         )
         foto.setScaleType(ImageView.ScaleType.FIT_END)
         valoracion.rating = lugar.valoracion
         setOnClickListener { onClick(itemView) }


            val posicion = (context.applicationContext as Aplicacion).posicionActual
            if (posicion== GeoPunto.SIN_POSICION || lugar.posicion== GeoPunto.SIN_POSICION) {
               distancia.text = "... Km"
            } else {
               val d = posicion.distancia(lugar.posicion).toInt()
               distancia.text = if (d < 2000) "$d m"
                                else          "${(d / 1000)} Km"

         }
      }
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val v = LayoutInflater.from(parent.context)
         .inflate(R.layout.elemento_lista, parent, false)
      return ViewHolder(v)
   }

   override fun onBindViewHolder(holder: ViewHolder, posicion: Int) {
      val lugar = lugares.elemento(posicion)
      holder.personaliza(lugar, onClick)
      holder.view.tag = posicion.toString()
   }

   override fun getItemCount() = lugares.tamanyo()
}
