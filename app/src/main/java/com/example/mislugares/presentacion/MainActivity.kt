package com.example.mislugares.presentacion

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.lang.Integer.parseInt
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mislugares.casos_uso.CasosUsoLugar
import com.example.mislugares.R
import com.example.mislugares.casos_uso.CasosUsoActividades
import com.example.mislugares.casos_uso.CasosUsoLocalizacion
import com.example.mislugares.datos.LugaresBD
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
   val RESULTADO_PREFERENCIAS = 0
   val SOLICITUD_PERMISO_LOCALIZACION = 1
   val lugares:LugaresBD by lazy { (application as Aplicacion).lugares }
   val adaptador by lazy { (application as Aplicacion).adaptador }
   val usoLugar by lazy { CasosUsoLugar(this, null,  lugares, adaptador) }
   val usoActividades by lazy { CasosUsoActividades(this) }
   val usoLocalizacion by lazy { CasosUsoLocalizacion(this, SOLICITUD_PERMISO_LOCALIZACION) }


   override fun onCreate(savedInstanceState: Bundle?) {

      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)
      setSupportActionBar(toolbar)
      fab.setOnClickListener { view ->
         usoLugar.nuevo()
      }
   }

   override fun onCreateOptionsMenu(menu: Menu): Boolean {
      menuInflater.inflate(R.menu.menu_main, menu)
      return true
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
      return when (item.itemId) {
         R.id.action_settings -> {
            usoActividades.lanzarPreferencias(RESULTADO_PREFERENCIAS)
            true
         }
         R.id.acercaDe -> {
            usoActividades.lanzarAcerdaDe()
            true
         }
         R.id.menu_buscar -> {
            lanzarVistaLugar()
            true
         }
         R.id.menu_mapa -> {
            usoActividades.lanzarMapa()
            true
         }
         else -> super.onOptionsItemSelected(item)
      }
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?  ) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == RESULTADO_PREFERENCIAS) {
         //usoLugar.refrescarAcaptador()
         adaptador.cursor = lugares.extraeCursor()
         adaptador.notifyDataSetChanged()
         //Si tenemos fragment a la derecha lo refrescamos
         if (usoLugar.obtenerFragmentVista() != null)
            usoLugar.mostrar(0)
      }
   }


   fun lanzarVistaLugar(view: View? = null) {
      val entrada = EditText(this)
      entrada.setText("0")
      AlertDialog.Builder(this)
         .setTitle("Selección de lugar")
         .setMessage("indica su pos:")
         .setView(entrada)
         .setPositiveButton("Ok") { dialog, whichButton ->
            val id = parseInt(entrada.text.toString())
            usoLugar.mostrar(id)
         }
         .setNegativeButton("Cancelar", null)
         .show()
   }



   override fun onRequestPermissionsResult(requestCode: Int,
                                           permissions: Array<String>, grantResults: IntArray ) {
      if (requestCode == SOLICITUD_PERMISO_LOCALIZACION) {
         if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            usoLocalizacion.permisoConcedido()
         }
      }
   }
   override fun onResume() {
      super.onResume()
      usoLocalizacion.activar()
      Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show()
   }

   override fun onPause() {
      super.onPause()
      usoLocalizacion.desactivar()
   }

   override fun onStart() {
      super.onStart()
      Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show()
   }

   override fun onStop() {
      Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show()
      super.onStop()
   }

   override fun onRestart() {
      super.onRestart()
      Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show()
   }
   override fun onDestroy() {
      Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
      super.onDestroy()
   }
}
