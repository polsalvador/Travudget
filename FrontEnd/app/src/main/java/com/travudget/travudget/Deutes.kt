package com.travudget.travudget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable
import android.widget.Toast

class Deutes : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.deutes)

        val participantsArray = intent.getStringArrayExtra("participants")
        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val googleEmail = sharedPreferences.getString("googleEmail", "")

        val layoutPart = findViewById<LinearLayout>(R.id.layoutParticipants)
        val participants = participantsArray?.toList()?.toMutableList() ?: mutableListOf()
        emailCreador?.let { participants.add(it) }

        val txtCancelar = findViewById<TextView>(R.id.txtCancelar)
        txtCancelar.setOnClickListener {
            val intent = Intent(this, CrearDespesa::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            startActivity(intent)
            finish()
        }

        for (participant in participants) {
            if (participant != googleEmail) {
                val participantLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
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

                val participantForm = EditText(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                    )
                    gravity = Gravity.END
                    inputType = InputType.TYPE_CLASS_NUMBER
                    hint = "1 al 100%"
                }

                participantLayout.addView(participantTextView)
                participantLayout.addView(participantForm)

                layoutPart.addView(participantLayout)
            }
        }

        val btnGuardar = findViewById<TextView>(R.id.txtAplicar)
        btnGuardar.setOnClickListener {
            val deutors = mutableMapOf<String, Int>()

            var totalPercentage = 0
            var hasValidParticipant = false

            for (i in 0 until layoutPart.childCount) {
                val view = layoutPart.getChildAt(i)
                if (view is LinearLayout) {
                    val participantTextView = view.getChildAt(0) as TextView
                    val participantForm = view.getChildAt(1) as EditText

                    val percentText = participantForm.text.toString()

                    val percent = percentText.toIntOrNull() ?: 0

                    totalPercentage += percent

                    if (percent > 1) {
                        hasValidParticipant = true
                    }

                    percent?.let {
                        deutors[participantTextView.text.toString()] = it
                    }
                }
            }


            if (hasValidParticipant && totalPercentage <= 100) {
                val returnIntent = Intent()
                returnIntent.putExtra("viatgeId", viatgeId)
                returnIntent.putExtra("emailCreador", emailCreador)
                returnIntent.putExtra("deutors", deutors as Serializable)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            } else {
                if (!hasValidParticipant) {
                    Toast.makeText(this, "Almenys un participant ha de tenir un percentatge major a 1.", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "La suma total dels percentatges no pot superar 100.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}