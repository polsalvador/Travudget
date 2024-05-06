package com.travudget.travudget

import android.widget.ImageView
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Typeface
import android.util.Log
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

        val participants = viatgeInfo.participants.toMutableList()
        if (emailCreador != null) {
            participants.add(emailCreador)
        }

        for (participant in participants) {
            val participantLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setPadding(0, 0, 0, 16.dpToPx())
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val participantTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                gravity = Gravity.START
                text = participant
            }

            participantLayout.addView(participantTextView)

            val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val googleEmail = sharedPreferences.getString("googleEmail", "")

            if (participant != googleEmail) {
                val payButton = ImageButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.START
                    }
                    setImageResource(R.drawable.ic_pay)
                    setOnClickListener {
                        val debtKey = "$googleEmail/$participant"

                        val alertDialogBuilder = AlertDialog.Builder(this@VeureViatge)
                        alertDialogBuilder.setTitle("Vols pagar els deutes a $participant?")
                        val builder = StringBuilder()
                        for ((key, value) in viatgeInfo.deutes) {
                            if (key.startsWith(debtKey)) {
                                val (_, googleEmail, nomDespesa, preuDespesa) = key.split("/")
                                builder.append("$nomDespesa $value")
                                builder.appendLine()
                            }
                        }

                        if (builder.isNotEmpty()) {
                            alertDialogBuilder.setMessage(builder.toString())
                            alertDialogBuilder.setPositiveButton("Sí") { _, _ ->
                                for ((key, _) in viatgeInfo.deutes.toList()) {
                                    if (key.startsWith(debtKey)) {
                                        viatgeInfo.deutes.remove(key)
                                    }
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    backendManager.editViatge(emailCreador, viatgeInfo)
                                }
                            }
                        } else {
                            alertDialogBuilder.setMessage("0")
                        }

                        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                    }
                }
                participantLayout.addView(payButton)
            }

            if (emailCreador == googleEmail && emailCreador != participant) {
                val crossImageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.END
                    }
                    setImageResource(R.drawable.ic_cross_red)
                    setOnClickListener {
                        AlertDialog.Builder(this@VeureViatge)
                            .setTitle("Estàs segur de que vols expulsar l'usuari?")
                            .setPositiveButton("Sí") { _, _ ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    backendManager.expulsarViatge(participant, viatgeInfo.viatgeId)
                                }
                                val intent = Intent(this@VeureViatge, VeureViatge::class.java).apply {
                                    putExtra("viatgeId", viatgeId)
                                    putExtra("emailCreador", emailCreador)
                                    putExtra("viatgeInfo", viatgeInfo)
                                }
                                layoutPart.removeView(participantLayout)
                                Thread.sleep(500)
                                startActivity(intent)
                                finish()

                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
                participantLayout.addView(crossImageView)
            }

            layoutPart.addView(participantLayout)

            val separator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.dpToPx()
                )
                setBackgroundColor(Color.parseColor("#CCCCCC"))
            }
            layoutPart.addView(separator)
        }

    }

    private fun Int.dpToPx(): Int {
        val scale: Float = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
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

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val googleEmail = sharedPreferences.getString("googleEmail", "")

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
                    if (googleEmail == emailCreador) {
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

                    } else {
                        Toast.makeText(this, "Has de ser el creador per eliminar un viatge", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}