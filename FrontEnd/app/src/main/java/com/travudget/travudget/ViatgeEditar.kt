package com.travudget.travudget

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
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
import android.view.WindowManager
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViatgeEditar : AppCompatActivity() {

    private lateinit var editTextNom: EditText
    private lateinit var editTextDataInici: EditText
    private lateinit var editTextDataFi: EditText
    private lateinit var buttonDivisa: Button
    private lateinit var editTextPressupost: EditText
    private lateinit var selectedDivisa: String

    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viatge_editar)
        val viatgeInfo = intent.getSerializableExtra("viatgeInfo") as ViatgeInfo

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        editTextNom = findViewById(R.id.editTextNom)
        editTextNom.setText(viatgeInfo.nomViatge)

        editTextDataInici = findViewById<EditText>(R.id.editTextDataInici).apply {
            setText(formatDate(viatgeInfo.dataInici))
            setOnClickListener { showDatePickerDialog(this) }
        }
        editTextDataFi = findViewById<EditText>(R.id.editTextDataFi).apply {
            setText(formatDate(viatgeInfo.dataFi))
            setOnClickListener { showDatePickerDialog(this) }
        }
        editTextPressupost = findViewById<EditText>(R.id.editTextPressupost)
        editTextPressupost.setText(viatgeInfo.pressupostTotal.toString())

        buttonDivisa = findViewById(R.id.buttonDivisa)
        buttonDivisa.setText(viatgeInfo.divisa)

        selectedDivisa = buttonDivisa.text.toString()

        buttonDivisa.setOnClickListener {
            val intent = Intent(this@ViatgeEditar, Divises::class.java)
            startActivityForResult(intent, 1)
        }

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

            val rectangle = EditText(this).apply {
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

            val form = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                hint = "0"
                inputType = InputType.TYPE_CLASS_NUMBER

                val dateStr = formatDate(addDays(viatgeInfo.dataInici, i - 1))
                if (pressupostVariableMap.containsKey(dateStr)) {
                    setText(pressupostVariableMap[dateStr].toString())
                }
            }

            dateLayout.addView(rectangle)
            dateLayout.addView(form)
            layout.addView(dateLayout)
        }

        val txtGuardar = findViewById<TextView>(R.id.txtGuardarEditar)
        txtGuardar.setOnClickListener {
            if (editTextNom.text.toString().isEmpty()) {
                Toast.makeText(this, "El nom del viatge és obligatori", Toast.LENGTH_SHORT).show()
            } else {
                val dataInici = editTextDataInici.text.toString()
                val dataFi = editTextDataFi.text.toString()
                val pressupost = editTextPressupost.text.toString().toIntOrNull() ?: 0

                val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                val googleEmail = sharedPreferences.getString("googleEmail", "")

                val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val sdfIniciStr = sdfOutput.format(sdfInput.parse(dataInici))
                val sdfFiStr = sdfOutput.format(sdfInput.parse(dataFi))

                val sdfInici = sdfOutput.parse(sdfIniciStr)
                val sdfFi = sdfOutput.parse(sdfFiStr)

                if (sdfFi.before(sdfInici)) {
                    Toast.makeText(
                        this,
                        "La data de finalització ha de ser posterior a la data d'inici",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val pressupostVariableMap = mutableMapOf<String, Int>()

                    for (i in 0 until layout.childCount) {
                        val childView = layout.getChildAt(i)
                        if (childView is LinearLayout) {
                            val dateEditText = childView.getChildAt(0) as EditText
                            val formEditText = childView.getChildAt(1) as EditText
                            val pressupostStr = formEditText.text.toString()
                            val pressupost = pressupostStr.toIntOrNull() ?: 0
                            val dateStr = dateEditText.text.toString()

                            pressupostVariableMap[dateStr] = pressupost
                        }
                    }

                    val totalPressupostPerDia = calcularTotalPressupostPerDia(layout)

                    viatgeInfo.nomViatge = editTextNom.text.toString()
                    viatgeInfo.dataInici = sdfInici
                    viatgeInfo.dataFi = sdfFi
                    viatgeInfo.pressupostTotal = if (pressupost > 0) pressupost else totalPressupostPerDia
                    viatgeInfo.divisa = selectedDivisa
                    viatgeInfo.pressupostVariable = pressupostVariableMap

                    CoroutineScope(Dispatchers.IO).launch {
                        backendManager.editViatge(googleEmail, viatgeInfo)

                        val intent = Intent(this@ViatgeEditar, Viatge::class.java).apply {
                            putExtra("viatgeId", viatgeInfo.viatgeId)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
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

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                selectedDivisa = data?.getStringExtra("selectedDivisa").toString()
                buttonDivisa.text = selectedDivisa
            }
        }
    }

    private fun calcularTotalPressupostPerDia(layout: LinearLayout): Int {
        var totalPressupostPerDia = 0

        for (i in 0 until layout.childCount) {
            val childView = layout.getChildAt(i)
            if (childView is LinearLayout) {
                val editText = childView.getChildAt(1) as EditText
                val pressupostStr = editText.text.toString()
                val pressupost = pressupostStr.toIntOrNull() ?: 0
                totalPressupostPerDia += pressupost
            }
        }

        return totalPressupostPerDia
    }
}