package com.travudget.travudget

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class FiltrarDespeses : AppCompatActivity() {
    private val categories = arrayOf("Menjar", "Compres", "Turisme", "Allotjament", "Transport", "Altres")
    private val creadors = arrayOf("travudget@gmail.com", "pol.salvadornogues@gmail.com", "salvadorpol14@gmail.com")
    private var selectedCategories = emptyArray<String>()
    private var selectedItems = BooleanArray(categories.size)
    private var selectedCreadors = BooleanArray(3)
    private var selectedCreadorsResult = emptyArray<String>()
    private var preuMinim: Int = 0
    private var preuMaxim: Int = 99999

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filtrar_despeses)

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        val txtCancelar = findViewById<TextView>(R.id.txtCancelar)
        txtCancelar.setOnClickListener {
            val intent = Intent(this, Viatge::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            startActivity(intent)
            finish()
        }

        val buttonCreador = findViewById<Button>(R.id.buttonCreador)
        buttonCreador.setOnClickListener {
            showCreadorDialog()
        }

        val buttonCategoria = findViewById<Button>(R.id.buttonCategoria)
        buttonCategoria.setOnClickListener {
            showCategoriaDialog()
        }
        val editTextPreuMaxim = findViewById<EditText>(R.id.preuMaxim)
        val editTextPreuMinim = findViewById<EditText>(R.id.preuMinim)

        val txtAplicar = findViewById<TextView>(R.id.txtAplicar)
        txtAplicar.setOnClickListener {
            preuMinim = editTextPreuMinim.text.toString().toIntOrNull() ?: 0
            preuMaxim = editTextPreuMaxim.text.toString().toIntOrNull() ?: 99999

            if (preuMinim < preuMaxim) {
                val intent = Intent(this, Viatge::class.java).apply {
                    putExtra("viatgeId", viatgeId)
                    putExtra("emailCreador", emailCreador)
                    putExtra("preuMinim", preuMinim)
                    putExtra("preuMaxim", preuMaxim)
                    putExtra("categories", selectedCategories)
                    putExtra("creadors", selectedCreadorsResult)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "El preu mínim ha de ser menor al preu màxim", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCategoriaDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona categories")
        builder.setMultiChoiceItems(categories, selectedItems) { _, which, isChecked ->
            selectedItems[which] = isChecked
        }
        builder.setPositiveButton("Acceptar") { dialog, _ ->
            val tempList = mutableListOf<String>()
            for (i in categories.indices) {
                if (selectedItems[i]) {
                    tempList.add(categories[i])
                }
            }
            selectedCategories = tempList.toTypedArray()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showCreadorDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona els creadors")
        builder.setMultiChoiceItems(creadors, selectedCreadors) { _, which, isChecked ->
            selectedCreadors[which] = isChecked
        }
        builder.setPositiveButton("Acceptar") { dialog, _ ->
            val tempList = mutableListOf<String>()
            for (i in creadors.indices) {
                if (selectedCreadors[i]) {
                    tempList.add(creadors[i])
                }
            }
            selectedCreadorsResult = tempList.toTypedArray()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}