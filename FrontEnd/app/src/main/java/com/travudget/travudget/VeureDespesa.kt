package com.travudget.travudget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.PopupMenu

class VeureDespesa : AppCompatActivity() {
    private lateinit var despesaInfo: DespesaInfo
    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.visibility = View.INVISIBLE
        setContentView(R.layout.veure_despesa)

        val imageViewAllotjament = findViewById<ImageView>(R.id.imageViewAllotjament)
        val imageViewCompres = findViewById<ImageView>(R.id.imageViewCompres)
        val imageViewMenjar = findViewById<ImageView>(R.id.imageViewMenjar)
        val imageViewTransport = findViewById<ImageView>(R.id.imageViewTransport)
        val imageViewTurisme = findViewById<ImageView>(R.id.imageViewTurisme)
        val imageViewAltres = findViewById<ImageView>(R.id.imageViewAltres)

        imageViewAllotjament.visibility = View.GONE
        imageViewCompres.visibility = View.GONE
        imageViewMenjar.visibility = View.GONE
        imageViewTransport.visibility = View.GONE
        imageViewTurisme.visibility = View.GONE
        imageViewAltres.visibility = View.GONE

        val emailCreador = intent.getStringExtra("emailCreador")!!
        val viatgeId = intent.getStringExtra("viatgeId")!!
        val despesaId = intent.getStringExtra("despesaId")!!
        val divisa = intent.getStringExtra("divisa")!!

        val btnOptions = findViewById<ImageButton>(R.id.btn_options)
        val btnReturn = findViewById<ImageButton>(R.id.btn_return)

        btnReturn.setOnClickListener {
            val intent = Intent(this, Viatge::class.java).apply {
                putExtra("emailCreador", emailCreador)
                putExtra("viatgeId", viatgeId)
            }
            Thread.sleep(500)
            startActivity(intent)
            finish()
        }

        btnOptions.setOnClickListener {
            showPopupMenu(btnOptions)
        }

        CoroutineScope(Dispatchers.IO).launch {
            despesaInfo = backendManager.getDespesa(emailCreador, viatgeId, despesaId)!!

            runOnUiThread {
                when (despesaInfo.categoria) {
                    "Allotjament" -> imageViewAllotjament.visibility = View.VISIBLE
                    "Compres" -> imageViewCompres.visibility = View.VISIBLE
                    "Menjar" -> imageViewMenjar.visibility = View.VISIBLE
                    "Transport" -> imageViewTransport.visibility = View.VISIBLE
                    "Turisme" -> imageViewTurisme.visibility = View.VISIBLE
                    "Altres" -> imageViewAltres.visibility = View.VISIBLE
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val dateInici = sdf.parse(despesaInfo.dataInici)
                val formattedDataInici = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateInici)
                val textViewDataInici = findViewById<TextView>(R.id.textDataInici)
                textViewDataInici.text = formattedDataInici

                if (despesaInfo.dataFi != null) {
                    val dateFi = sdf.parse(despesaInfo.dataFi)
                    val formattedDataFi = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateFi)
                    val textViewDataFi = findViewById<TextView>(R.id.textDataFi)
                    textViewDataFi.text = formattedDataFi
                }

                if (despesaInfo.ubicacio_lat != 0.0) {
                    val textViewUbicacio = findViewById<TextView>(R.id.textUbicacio)
                    textViewUbicacio.text = despesaInfo.ubicacio_lat.toString() + " " + despesaInfo.ubicacio_long
                }

                val textViewNom = findViewById<TextView>(R.id.textNomDespesa)
                textViewNom.text = despesaInfo.nomDespesa

                val textViewPreu = findViewById<TextView>(R.id.textPreu)
                textViewPreu.text = despesaInfo.preu.toString() + " " + divisa

            }
        }
        window.decorView.visibility = View.VISIBLE
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu_despesa)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete)

        val spannableString = SpannableString(deleteMenuItem.title)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        deleteMenuItem.title = spannableString

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val emailCreador = intent.getStringExtra("emailCreador")!!
                    val despesaId = intent.getStringExtra("despesaId")!!
                    val viatgeId = intent.getStringExtra("viatgeId")!!
                    val divisa = intent.getStringExtra("divisa")!!

                    val intent = Intent(this, EditarDespesa::class.java).apply {
                        putExtra("despesaInfo", despesaInfo)
                        putExtra("despesaId", despesaId)
                        putExtra("emailCreador", emailCreador)
                        putExtra("viatgeId", viatgeId)
                        putExtra("divisa", divisa)
                    }
                    Thread.sleep(500)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Estàs segur de que vols eliminar la despesa?")
                        .setPositiveButton("Sí") { _, _ ->
                            val emailCreador = intent.getStringExtra("emailCreador")!!
                            val viatgeId = intent.getStringExtra("viatgeId")!!
                            val despesaId = intent.getStringExtra("despesaId")!!

                            CoroutineScope(Dispatchers.IO).launch {
                                val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                                val googleEmail = sharedPreferences.getString("googleEmail", "")

                                if (googleEmail != null) {
                                    backendManager.deleteDespesa(googleEmail, emailCreador, viatgeId, despesaId)
                                }
                            }
                            val intent = Intent(this, Viatge::class.java).apply {
                                putExtra("emailCreador", emailCreador)
                                putExtra("viatgeId", viatgeId)
                            }
                            Thread.sleep(500)
                            startActivity(intent)
                            finish()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
