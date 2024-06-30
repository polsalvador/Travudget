package com.travudget.travudget

import android.content.Context
import android.content.Intent
import android.widget.PopupMenu
import android.os.Bundle
import android.text.Html
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.ImageView
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.io.Serializable

class Viatge : AppCompatActivity() {

    private lateinit var viatgeInfo: ViatgeInfo
    private lateinit var despeses: List<DespesaShowInfo>
    private var despesaTotal: Int = 0
    private lateinit var despesaPerDia: HashMap<String, Int>
    private val backendManager = BackendManager()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viatge)

        val btnInfo = findViewById<ImageButton>(R.id.btn_info)
        val btnAddDespesa = findViewById<ImageButton>(R.id.btn_add_despesa)
        val btnReturn = findViewById<ImageButton>(R.id.btn_return)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        val nomButton = findViewById<TextView>(R.id.textViewViatge)
        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        btnReturn.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            startActivity(Intent(this, Principal::class.java))
            finish()
        }

        nomButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                viatgeInfo = backendManager.getViatge(emailCreador, viatgeId)!!
                Thread.sleep(500)
                val intent = Intent(this@Viatge, VeureViatge::class.java).apply {
                    putExtra("viatgeId", viatgeId)
                    putExtra("emailCreador", emailCreador)
                    putExtra("viatgeInfo", viatgeInfo)
                }
                handler.removeCallbacksAndMessages(null)
                startActivity(intent)
                finish()
            }
        }

        btnInfo.setOnClickListener {
            showPopupMenu(btnInfo)
        }
        
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
                    Thread.sleep(500)
                    handler.removeCallbacksAndMessages(null)
                    startActivity(Intent(this, Principal::class.java))
                    finish()
                    true
                }
                R.id.nav_logout -> {
                    AlertDialog.Builder(this)
                        .setTitle("Estàs segur de que vols tancar sessió?")
                        .setPositiveButton(
                            Html.fromHtml("<font color=\"#FFFF00\">Si</font>")
                        ) { _, _ ->
                            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                            val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()
                            Thread.sleep(500)
                            handler.removeCallbacksAndMessages(null)
                            startActivity(Intent(this, IniciSessio::class.java))
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

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout_viatge)
        drawerLayout.visibility = View.INVISIBLE

        btnAddDespesa.setOnClickListener {
            val participantsArray = viatgeInfo.participants.toTypedArray()
            val intent = Intent(this@Viatge, CrearDespesa::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
                putExtra("participants", participantsArray)
            }
            handler.removeCallbacksAndMessages(null)
            startActivity(intent)
            finish()
        }

        CoroutineScope(Dispatchers.IO).launch {
            viatgeInfo = backendManager.getViatge(emailCreador, viatgeId)!!

            runOnUiThread {
                val textView = findViewById<TextView>(R.id.textViewViatge)
                textView.text = viatgeInfo.nomViatge
                drawerLayout.visibility = View.VISIBLE
            }
        }
        showDespeses(contentFrame)

        handler.postDelayed(object : Runnable {
            override fun run() {
                showDespeses(contentFrame)
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    private fun showDespeses(contentFrame: FrameLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            val viatgeId = intent.getStringExtra("viatgeId")
            val emailCreador = intent.getStringExtra("emailCreador")

            if (intent.hasExtra("preuMinim")) {
                val preuMinim = intent.getIntExtra("preuMinim", 0)
                val preuMaxim = intent.getIntExtra("preuMaxim", 0)
                val categories: Array<String>? = intent.getStringArrayExtra("categories")
                val creadors: Array<String>? = intent.getStringArrayExtra("creadors")

                despeses = BackendManager().getDespesesFiltrades(emailCreador, viatgeId, categories, creadors, preuMinim, preuMaxim)
            } else {
                despeses = BackendManager().getDespeses(emailCreador, viatgeId)
            }
            val linearLayout = LinearLayout(contentFrame.context)
            linearLayout.orientation = LinearLayout.VERTICAL

            var alertDialogShown = false

            val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val debtMessagesShown = sharedPreferences.getStringSet("debtMessagesShown", HashSet()) ?: HashSet()
            val googleEmail = sharedPreferences.getString("googleEmail", "")

            Thread.sleep(500)
            println("Viatge: ${viatgeInfo.deutes}")
            for (despesa in despeses) {
                val deutors = despesa.deutors
                if (deutors != null && deutors.isNotEmpty()) {
                    for ((nomDeute) in deutors) {
                        if (nomDeute == googleEmail) {
                            val msg =
                                "${despesa.emailCreador} ha creat la despesa '${despesa.nomDespesa}' i li deus ${deutors[nomDeute]}"
                            if (!debtMessagesShown.contains(msg)) {
                                runOnUiThread {
                                    showDebtAlertDialog(msg)
                                }
                                debtMessagesShown.add(msg)
                                val editor = sharedPreferences.edit()
                                editor.putStringSet("debtMessagesShown", debtMessagesShown)
                                editor.apply()
                            }
                            break
                        }
                    }
                }
            }

            val despesesPerData = HashMap<Date, MutableList<DespesaShowInfo>>()

            despesaTotal = 0
            despesaPerDia = HashMap()

            for (despesa in despeses) {
                val data: Date = despesa.dataInici

                if (!despesesPerData.containsKey(data)) {
                    despesesPerData[data] = mutableListOf()
                    despesaPerDia[data.toString()] = despesa.preu
                } else {
                    despesaPerDia[data.toString()] = despesa.preu + (despesaPerDia[data.toString()] ?: 0)
                }
                despesesPerData[data]?.add(despesa)
                despesaTotal += despesa.preu
            }

            runOnUiThread {
                contentFrame.removeAllViews()

                for ((data, listaDespesas) in despesesPerData) {
                    val headerView = createHeaderView(data, despesaPerDia)
                    linearLayout.addView(headerView)

                    for (despesa in listaDespesas) {
                        val cardView = createCardViewForDespesa(despesa)
                        cardView.setOnClickListener {
                            val intent = Intent(this@Viatge, VeureDespesa::class.java).apply {
                                putExtra("emailCreador", emailCreador)
                                putExtra("viatgeId", viatgeId)
                                putExtra("despesaId", despesa.despesaId)
                                putExtra("divisa", viatgeInfo.divisa)
                            }
                            Thread.sleep(500)
                            startActivity(intent)
                        }
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        layoutParams.bottomMargin = 20
                        cardView.layoutParams = layoutParams
                        linearLayout.addView(cardView)
                    }
                }
                    contentFrame.addView(linearLayout)
            }
        }
    }

    private fun showDebtAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Despesa compartida")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun createHeaderView(data: Date, despesesPerDia: HashMap<String, Int>): View {
        val inflater = LayoutInflater.from(this@Viatge)
        val headerView = inflater.inflate(R.layout.header_data_despesa, null)

        val textViewFecha = headerView.findViewById<TextView>(R.id.textData)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val datasdf = dateFormat.format(data)
        textViewFecha.text = datasdf

        val num = despesesPerDia[data.toString()] ?: 0
        val textDespesa = headerView.findViewById<TextView>(R.id.textDespesa)
        if (::viatgeInfo.isInitialized) {
            textDespesa.text = num.toString() + " " + viatgeInfo.divisa
        } else {
            Thread.sleep(500)
            textDespesa.text = num.toString() + " " + viatgeInfo.divisa
        }

        return headerView
    }

    private fun createCardViewForDespesa(despesaShowInfo: DespesaShowInfo): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.cards_despeses, null) as CardView

        val textViewNomDespesa = cardView.findViewById<TextView>(R.id.textNomDespesa)
        textViewNomDespesa.text = despesaShowInfo.nomDespesa

        val textViewPreu = cardView.findViewById<TextView>(R.id.textPreu)
        textViewPreu.text = despesaShowInfo.preu.toString() + " " + viatgeInfo.divisa

        val textViewCreador = cardView.findViewById<TextView>(R.id.textAutor)
        textViewCreador.text = despesaShowInfo.emailCreador

        val imageViewAllotjament = cardView.findViewById<ImageView>(R.id.imageViewAllotjament)
        val imageViewCompres = cardView.findViewById<ImageView>(R.id.imageViewCompres)
        val imageViewMenjar = cardView.findViewById<ImageView>(R.id.imageViewMenjar)
        val imageViewTransport = cardView.findViewById<ImageView>(R.id.imageViewTransport)
        val imageViewTurisme = cardView.findViewById<ImageView>(R.id.imageViewTurisme)
        val imageViewAltres = cardView.findViewById<ImageView>(R.id.imageViewAltres)

        imageViewAllotjament.visibility = View.GONE
        imageViewCompres.visibility = View.GONE
        imageViewMenjar.visibility = View.GONE
        imageViewTransport.visibility = View.GONE
        imageViewTurisme.visibility = View.GONE
        imageViewAltres.visibility = View.GONE

        when (despesaShowInfo.categoria) {
            "Allotjament" -> imageViewAllotjament.visibility = View.VISIBLE
            "Compres" -> imageViewCompres.visibility = View.VISIBLE
            "Menjar" -> imageViewMenjar.visibility = View.VISIBLE
            "Transport" -> imageViewTransport.visibility = View.VISIBLE
            "Turisme" -> imageViewTurisme.visibility = View.VISIBLE
            "Altres" -> imageViewAltres.visibility = View.VISIBLE
        }

        return cardView
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu)

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_filtrar -> {
                    val intent = Intent(this, FiltrarDespeses::class.java).apply {
                        putExtra("viatgeId", viatgeId)
                        putExtra("emailCreador", emailCreador)
                    }
                    Thread.sleep(500)
                    handler.removeCallbacksAndMessages(null)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_informe -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        despeses = BackendManager().getDespeses(emailCreador, viatgeId)

                        val listDespesesSerializables = ArrayList<Serializable>()
                        for (despesa in despeses) {
                            listDespesesSerializables.add(despesa)
                        }

                        val pressupostVariable = viatgeInfo.pressupostVariable
                        val pressupostVariableSerializable = HashMap(pressupostVariable)
                        val deutes = viatgeInfo.deutes
                        val deutesSerializable = HashMap(deutes)

                        val intent = Intent(this@Viatge, Informes::class.java).apply {
                            putExtra("viatgeId", viatgeId)
                            putExtra("emailCreador", emailCreador)
                            putExtra("despesaTotal", despesaTotal)
                            putExtra("pressupostTotal", viatgeInfo.pressupostTotal)
                            putExtra("despeses", listDespesesSerializables)
                            putExtra("pressupostVariable", pressupostVariableSerializable)
                            putExtra("despesaPerDia", despesaPerDia)
                            putExtra("deutes", deutesSerializable)

                        }
                        Thread.sleep(500)
                        handler.removeCallbacksAndMessages(null)
                        startActivity(intent)
                        finish()
                    }
                        true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
