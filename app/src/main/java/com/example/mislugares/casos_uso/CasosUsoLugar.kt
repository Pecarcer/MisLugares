package com.example.mislugares.casos_uso

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.mislugares.presentacion.Aplicacion
import com.example.mislugares.datos.LugaresBD
import com.example.mislugares.modelo.GeoPunto
import com.example.mislugares.modelo.Lugar
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import com.example.mislugares.R
import com.example.mislugares.presentacion.*

open class CasosUsoLugar(

   open val actividad: FragmentActivity,//AppCompatActivity,
   open val fragment: Fragment?,
   open val lugares: LugaresBD,
   open val adaptador: AdaptadorLugaresBD) /*: TimePickerDialog.OnTimeSetListener */ {




   fun guardar(id: Int, nuevoLugar: Lugar) {
      lugares.actualiza(id, nuevoLugar)
      adaptador.cursor = lugares.extraeCursor()
      adaptador.notifyDataSetChanged()
   }

   fun actualizaPosLugar(pos: Int, lugar: Lugar) {
      val id = adaptador.idPosicion(pos)
      guardar(id, lugar)
   }

   fun obtenerFragmentVista(): VistaLugarFragment? {
      val manejador = actividad /*as AppCompatActivity*/.supportFragmentManager
      return manejador.findFragmentById(R.id.vista_lugar_fragment) as VistaLugarFragment?
   }

   fun obtenerFragmentSelector(): SelectorFragment? {
      val manejador = actividad.supportFragmentManager
      return manejador.findFragmentById(R.id.selector_fragment) as SelectorFragment?
   }

   fun mostrar(pos: Int) {
      val fragmentVista = obtenerFragmentVista()
      if (fragmentVista != null) {
         fragmentVista.pos = pos
         fragmentVista._id = adaptador.idPosicion(pos)  /////////////////
         fragmentVista.actualizaVistas()
      } else {
         val i = Intent(actividad, VistaLugarActivity::class.java)
         i.putExtra("pos", pos)
         actividad.startActivity(i)
      }
   }

   fun editar(pos: Int, codidoSolicitud: Int) {
      val i = Intent(actividad, EdicionLugarActivity::class.java)
      i.putExtra("pos", pos)
      fragment?.startActivityForResult(i, codidoSolicitud)
              ?:actividad.startActivityForResult(i, codidoSolicitud)
   }

   fun nuevo() {
      val _id = lugares.nuevo()
      val posicion = (actividad.application as Aplicacion).posicionActual
      if (posicion != GeoPunto.SIN_POSICION) {
         val lugar = lugares.elemento(_id)
         lugar.posicion = posicion
         lugares.actualiza(_id, lugar)
      }
      val i = Intent(actividad, EdicionLugarActivity::class.java)
      i.putExtra("_id", _id)
      actividad.startActivity(i)
   }

   fun borrar(id: Int) =
      AlertDialog.Builder(actividad)
         .setTitle("Borrado de lugar")
         .setMessage("¿Estás seguro que quieres eliminar este lugar?")
         .setPositiveButton("Confirmar") { dialog, whichButton ->
            lugares.borrar(id)
            adaptador.cursor = lugares.extraeCursor()
            adaptador.notifyDataSetChanged()


            if (obtenerFragmentSelector()==null){
               actividad.finish()
            } else {

               mostrar(0)
            }
         }
         .setNegativeButton("Cancelar", null)
         .show()


   fun refrescarAcaptador() {
      adaptador.cursor = (lugares.extraeCursor())
      adaptador.notifyDataSetChanged()
   }

   fun compartir(lugar: Lugar) = actividad.startActivity(
      Intent(Intent.ACTION_SEND).apply {
         type = "text/plain"
         putExtra(Intent.EXTRA_TEXT, "$(lugar.nombre) -  $(lugar.url)")
      })

   fun llamarTelefono(lugar: Lugar) = actividad.startActivity(
      Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + lugar.telefono))
   )

   fun verPgWeb(lugar: Lugar) = actividad.startActivity(
      Intent(Intent.ACTION_VIEW, Uri.parse(lugar.url))
   )

   fun verMapa(lugar: Lugar) {
      val lat = lugar.posicion.latitud
      val lon = lugar.posicion.longitud
      val uri = if (lugar.posicion != GeoPunto.SIN_POSICION)//(lat != 0.0 || lon != 0.0) {
         Uri.parse("geo:$lat,$lon")
      else
         Uri.parse("geo:0,0?q=" + lugar.direccion)
      actividad.startActivity(Intent(Intent.ACTION_VIEW, uri))
   }



   fun ponerDeGaleria(codidoSolicitud: Int) {
      val action = if (android.os.Build.VERSION.SDK_INT >= 19) { // API 19 - Kitkat
         Intent.ACTION_OPEN_DOCUMENT
      } else {
         Intent.ACTION_PICK
      }
      val i = Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
         addCategory(Intent.CATEGORY_OPENABLE)
         type = "image/*"
      }
      fragment?.startActivityForResult(i, codidoSolicitud)
         ?:actividad.startActivityForResult(i, codidoSolicitud)

   }


   fun tomarFoto(codidoSolicitud: Int): Uri? {
      try {
         val file = File.createTempFile(
            "img_" + System.currentTimeMillis() / 1000, /* prefix */
            ".jpg", /* suffix */
            actividad.getExternalFilesDir(Environment.DIRECTORY_PICTURES) /* directory */
         )
         val uriUltimaFoto = if (Build.VERSION.SDK_INT >= 24)
            FileProvider.getUriForFile(
               actividad,
               "es.upv.jtomas.mislugares.fileProvider", file
            )
         else Uri.fromFile(file)
         val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
         i.putExtra(MediaStore.EXTRA_OUTPUT, uriUltimaFoto)
         fragment?.startActivityForResult(i, codidoSolicitud)
            ?:actividad.startActivityForResult(i, codidoSolicitud)

         return uriUltimaFoto
      } catch (ex: IOException) {
         Toast.makeText(actividad, "Error al crear fichero de imagen", Toast.LENGTH_LONG).show()
         return null
      }
   }

   fun ponerFoto(pos: Int, uri: String?, imageView: ImageView) {
      val lugar = adaptador.lugarPosicion(pos)
      lugar.foto = uri ?: ""
      visualizarFoto(lugar, imageView)
      actualizaPosLugar(pos, lugar)
   }

   fun visualizarFoto(lugar: Lugar, imageView: ImageView) {
      if (!(/*lugar.foto == null ||*/ lugar.foto.isEmpty())) {
         imageView.setImageBitmap(reduceBitmap(actividad, lugar.foto, 1024, 1024))
      } else {
         imageView.setImageBitmap(null)
      }
   }


   private fun reduceBitmap(contexto: Context, uri: String, maxAncho: Int, maxAlto: Int): Bitmap? {
      try {
         var input: InputStream?
         val u = Uri.parse(uri)
         if (u.scheme == "http" || u.scheme == "https") {
            input = URL(uri).openStream()
         } else {
            input = contexto.getContentResolver().openInputStream(u)
         }
         val options = BitmapFactory.Options()
         options.inJustDecodeBounds = true
         options.inSampleSize = Math.max(
            Math.ceil((options.outWidth / maxAncho).toDouble()),
            Math.ceil((options.outHeight / maxAlto).toDouble())
         ).toInt()
         options.inJustDecodeBounds = false
         return BitmapFactory.decodeStream(input, null, options)
      } catch (e: FileNotFoundException) {
         Toast.makeText(
            contexto, "Fichero/recurso de imagen no encontrado",
            Toast.LENGTH_LONG
         ).show()
         e.printStackTrace()
         return null
      } catch (e: IOException) {
         Toast.makeText(
            contexto, "Error accediendo a imagen",
            Toast.LENGTH_LONG
         ).show()
         e.printStackTrace()
         return null
      }
   }
}
