package com.travudget.travudget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntroduirCodi : AppCompatActivity()  {
    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.introduir_codi)

        val editTextCodi = findViewById<EditText>(R.id.editTextCodi)

        val txtCancelar = findViewById<TextView>(R.id.txtCancelar)
        txtCancelar.setOnClickListener {
            startActivity(Intent(this, Principal::class.java))
            finish()
        }
        val txtGuardar = findViewById<TextView>(R.id.txtGuardar)
        txtGuardar.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                val googleEmail = sharedPreferences.getString("googleEmail", "")
                val codi = editTextCodi.text.toString()

                if (backendManager.unirViatge(googleEmail, codi)) {
                    startActivity(Intent(this@IntroduirCodi, Principal::class.java))
                    finish()
                } else {
                    Toast.makeText(this@IntroduirCodi, "El codi no pertany a cap viatge", Toast.LENGTH_SHORT).show()
                    editTextCodi.setText("")
                }
            }
        }
    }
}