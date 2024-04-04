package com.travudget.travudget

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Typeface
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.ImageButton

class VeureViatge : AppCompatActivity() {
    private val backendManager = BackendManager()
    private lateinit var viatgeInfo: ViatgeInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.veure_viatge)

        val txtRetornar = findViewById<TextView>(R.id.txtRetornar)
        val textViewViatge = findViewById<TextView>(R.id.textViewViatge)
        val btnOptions = findViewById<ImageButton>(R.id.btn_options)
        val textDataInici = findViewById<TextView>(R.id.textDataInici)
        val textDataFi = findViewById<TextView>(R.id.textDataFi)
        val textDivisa = findViewById<TextView>(R.id.textDivisa)
        val textPressupost = findViewById<TextView>(R.id.textPressupost)
        val textCodi = findViewById<TextView>(R.id.textCodi)

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")
        viatgeInfo = intent.getSerializableExtra("viatgeInfo") as ViatgeInfo

        txtRetornar.setOnClickListener {
            val intent = Intent(this, Viatge::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            Thread.sleep(500)
            startActivity(intent)
            finish()
        }

        btnOptions.setOnClickListener {
            showPopupMenu(btnOptions)
        }

        textViewViatge.text = viatgeInfo.nomViatge
        textDataInici.text = formatDate(viatgeInfo.dataInici)
        textDataFi.text = formatDate(viatgeInfo.dataFi)
        textDivisa.text = viatgeInfo.divisa
        textPressupost.text = viatgeInfo.pressupostTotal.toString()
        textCodi.text = viatgeInfo.codi

        val duration = calculateDuration(viatgeInfo.dataInici, viatgeInfo.dataFi)
        val layout = findViewById<LinearLayout>(R.id.layoutPressupostPerDia)

        val pressupostVariableMap = viatgeInfo.pressupostVariable

        for (i in 1..duration) {
            val dateLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            val rectangle = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val data = addDays(viatgeInfo.dataInici, i - 1)
                setText(formatDate(data))
                isEnabled = false
                setTextColor(resources.getColor(android.R.color.black))
                setTypeface(null, Typeface.BOLD)
            }

            val form = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = (8 * resources.displayMetrics.density).toInt() // Establece el margen de inicio en 8dp
                }
                val dateStr = formatDate(addDays(viatgeInfo.dataInici, i - 1))
                if (pressupostVariableMap.containsKey(dateStr)) {
                    text = pressupostVariableMap[dateStr].toString()
                } else {
                    text = "0"
                }
            }

            dateLayout.addView(rectangle)
            dateLayout.addView(form)
            layout.addView(dateLayout)
        }

        val layoutPart = findViewById<LinearLayout>(R.id.layoutParticipants)
        val participants = viatgeInfo.participants

        for (participant in participants) {
            val participantLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            val participantTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = participant
            }

            participantLayout.addView(participantTextView)
            layoutPart.addView(participantLayout)
        }
    }

    private fun calculateDuration(startDate: Date?, endDate: Date?): Int {
        if (startDate == null || endDate == null) return 0

        val difference = endDate.time - startDate.time
        return (difference / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    private fun addDays(date: Date?, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    private fun formatDate(date: Date?): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(date)
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu_viatge)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete)

        val spannableString = SpannableString(deleteMenuItem.title)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        deleteMenuItem.title = spannableString

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val intent = Intent(this, ViatgeEditar::class.java).apply {
                        putExtra("viatgeInfo", viatgeInfo)
                        putExtra("emailCreador", emailCreador)
                    }
                    Thread.sleep(500)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Estàs segur de que vols eliminar el viatge?")
                        .setPositiveButton("Sí") { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                backendManager.deleteViatge(emailCreador, viatgeInfo.viatgeId)
                            }
                            Thread.sleep(500)
                            startActivity(Intent(this, Principal::class.java))
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