package com.travudget.travudget

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.ImageView
import android.graphics.PorterDuffColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager

class CrearDespesa : AppCompatActivity() {
    private val backendManager = BackendManager()

    private lateinit var editTextNom: EditText
    private lateinit var editTextPreu: EditText
    private lateinit var editTextDescripcio: EditText
    private lateinit var editTextDataInici: EditText
    private lateinit var editTextDataFi: EditText
    private lateinit var buttonUbicacio: Button
    private var selectedCategoryId: Int = -1
    private var ubicacio_lat: Double = 0.0
    private var ubicacio_long: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crear_despesa)

        editTextNom = findViewById(R.id.editTextNom)
        editTextPreu = findViewById(R.id.editTextPreu)
        editTextDataInici = findViewById(R.id.editTextDataInici)
        editTextDataFi = findViewById(R.id.editTextDataFi)
        editTextDescripcio = findViewById(R.id.editTextDescripcio)
        buttonUbicacio = findViewById(R.id.buttonUbicacio)

        val txtCancelar = findViewById<TextView>(R.id.txtCancelar)

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        txtCancelar.setOnClickListener {
            val intent = Intent(this@CrearDespesa, Viatge::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            startActivity(intent)
            finish()
        }

        buttonUbicacio.setOnClickListener {
            val intent = Intent(this@CrearDespesa, MapActivity::class.java)
            startActivityForResult(intent, 101)
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

        val categories = listOf(
            R.id.imageViewAllotjament,
            R.id.imageViewCompres,
            R.id.imageViewMenjar,
            R.id.imageViewTransport,
            R.id.imageViewTurisme,
            R.id.imageViewAltres
        )

        val colorFilterSelected =
            PorterDuffColorFilter(Color.parseColor("#0000FF"), PorterDuff.Mode.SRC_IN)
        val colorFilterUnselected =
            PorterDuffColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN)

        categories.forEach { categoryId ->
            val imageView = findViewById<ImageView>(categoryId)
            imageView.setOnClickListener {
                if (selectedCategoryId != categoryId) {
                    selectedCategoryId = categoryId
                    categories.forEach { otherCategoryId ->
                        if (otherCategoryId != categoryId) {
                            findViewById<ImageView>(otherCategoryId).isSelected = false
                            applyColorFilter(findViewById(otherCategoryId), colorFilterUnselected)
                        }
                    }
                    imageView.isSelected = true
                    applyColorFilter(imageView, colorFilterSelected)
                } else {
                    imageView.isSelected = !imageView.isSelected
                    applyColorFilter(
                        imageView,
                        if (imageView.isSelected) colorFilterSelected else colorFilterUnselected
                    )
                    if (imageView.isSelected) {
                        selectedCategoryId = categoryId
                    } else {
                        selectedCategoryId = -1
                    }
                }
            }
        }

        val txtGuardar = findViewById<TextView>(R.id.txtGuardar)
        txtGuardar.setOnClickListener {
            if (editTextNom.text.toString().isEmpty()) {
                Toast.makeText(this, "El nom de la despesa és obligatori", Toast.LENGTH_SHORT)
                    .show()
            } else if (editTextPreu.text.toString().isEmpty()) {
                Toast.makeText(this, "El preu és obligatori", Toast.LENGTH_SHORT).show()
            } else if (selectedCategoryId == -1) {
                Toast.makeText(this, "Selecciona una categoria", Toast.LENGTH_SHORT).show()
            } else {
                val dataInici = editTextDataInici.text.toString()
                val dataFi = editTextDataFi.text.toString()

                val sdfInici = SimpleDateFormat("dd/MM/yyyy").parse(dataInici)
                val sdfFi = SimpleDateFormat("dd/MM/yyyy").parse(dataFi)

                val dataIniciFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dataInici)
                ).toString()
                val dataFiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dataFi)
                ).toString()

                if (sdfFi.before(sdfInici)) {
                    Toast.makeText(
                        this,
                        "La data de finalització ha de ser posterior a la data d'inici",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (getCategoria(selectedCategoryId).isNullOrEmpty()) {
                    Toast.makeText(this, "Has de seleccionar una categoria", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val despesaInfo = DespesaInfo(
                            nomDespesa = editTextNom.text.toString(),
                            viatgeId = viatgeId,
                            emailCreador = emailCreador,
                            descripcio = editTextDescripcio.text.toString(),
                            preu = editTextPreu.text.toString().toInt(),
                            categoria = getCategoria(selectedCategoryId),
                            dataInici = dataIniciFormat,
                            dataFi = dataFiFormat,
                            ubicacio_lat = ubicacio_lat,
                            ubicacio_long = ubicacio_long,
                            deutors = mapOf("Placeholder1" to 50, "Placeholder2" to 50)
                        )

                        backendManager.createDespesa(despesaInfo)

                        val intent = Intent(this@CrearDespesa, Viatge::class.java).apply {
                            putExtra("viatgeId", viatgeId)
                            putExtra("emailCreador", emailCreador)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        setupTouchListeners()
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

    private fun getCategoria(categoryId: Int): String {
        return when (categoryId) {
            R.id.imageViewAllotjament -> "Allotjament"
            R.id.imageViewCompres -> "Compres"
            R.id.imageViewMenjar -> "Menjar"
            R.id.imageViewTransport -> "Transport"
            R.id.imageViewTurisme -> "Turisme"
            R.id.imageViewAltres -> "Altres"
            else -> ""
        }
    }

    private fun applyColorFilter(imageView: ImageView, colorFilter: PorterDuffColorFilter) {
        imageView.colorFilter = colorFilter
    }

    private fun setupTouchListeners() {
        val rootView = findViewById<View>(R.id.layoutCrearDespesa)
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
            }
            false
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            ubicacio_lat = data?.getDoubleExtra("lat", 0.0)!!
            ubicacio_long = data?.getDoubleExtra("long", 0.0)!!

            buttonUbicacio.setText(ubicacio_lat.toString() + " " + ubicacio_long.toString())
        }
    }
}
