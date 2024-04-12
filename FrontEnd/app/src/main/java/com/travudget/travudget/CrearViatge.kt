package com.travudget.travudget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import android.app.DatePickerDialog
import java.text.SimpleDateFormat

class CrearViatge : AppCompatActivity() {
    private val backendManager = BackendManager()
    private var selectedDivisa: String? = null

    private lateinit var editTextNom: EditText
    private lateinit var editTextDataInici: EditText
    private lateinit var editTextDataFi: EditText
    private lateinit var buttonDivisa: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crear_viatge)

        editTextNom = findViewById(R.id.editTextNom)
        editTextDataInici = findViewById(R.id.editTextDataInici)
        editTextDataFi = findViewById(R.id.editTextDataFi)
        buttonDivisa = findViewById(R.id.buttonDivisa)

        buttonDivisa.setOnClickListener {
            val intent = Intent(this@CrearViatge, Divises::class.java)
            startActivityForResult(intent, 1)
        }

        val txtCancelar = findViewById<TextView>(R.id.txtCancelar)
        txtCancelar.setOnClickListener {
            startActivity(Intent(this@CrearViatge, Principal::class.java))
            finish()
        }

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        editTextDataInici.setText(dateFormat.format(today.time))
        editTextDataFi.setText(dateFormat.format(today.time))

        editTextDataInici.setOnClickListener {
            showDatePickerDialog(editTextDataInici)
        }

        editTextDataFi.setOnClickListener {
            showDatePickerDialog(editTextDataFi)
        }

        val txtGuardar = findViewById<TextView>(R.id.txtGuardar)
        txtGuardar.setOnClickListener {
            if (editTextNom.text.toString().isEmpty()) {
                Toast.makeText(this, "El nom del viatge és obligatori", Toast.LENGTH_SHORT).show()
            } else {
                val nomViatge = editTextNom.text.toString()
                val dataInici = editTextDataInici.text.toString()
                val dataFi = editTextDataFi.text.toString()

                val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                val googleEmail = sharedPreferences.getString("googleEmail", "")

                val sdfInici = SimpleDateFormat("dd/MM/yyyy").parse(dataInici)
                val sdfFi = SimpleDateFormat("dd/MM/yyyy").parse(dataFi)

                val dataIniciFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dataInici)).toString()
                val dataFiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dataFi)).toString()

                if (sdfFi.before(sdfInici)) {
                    Toast.makeText(this, "La data de finalització ha de ser posterior a la data d'inici", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        selectedDivisa = selectedDivisa ?: "EUR"

                        val viatgeId = backendManager.createViatge(
                            googleEmail,
                            nomViatge,
                            dataIniciFormat,
                            dataFiFormat,
                            selectedDivisa
                        )
                        val intent = Intent(this@CrearViatge, Viatge::class.java).apply {
                            putExtra("viatgeId", viatgeId)
                            putExtra("emailCreador", googleEmail)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                selectedDivisa = data?.getStringExtra("selectedDivisa")
                buttonDivisa.text = selectedDivisa
            }
        }
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
}
