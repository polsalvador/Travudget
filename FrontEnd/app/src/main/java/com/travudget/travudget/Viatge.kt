package com.travudget.travudget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.PopupMenu
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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

class Viatge : AppCompatActivity() {

    private lateinit var viatgeInfo: ViatgeInfo
    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viatge)

        val btnOptions = findViewById<ImageButton>(R.id.btn_options)
        val btnAddDespesa = findViewById<ImageButton>(R.id.btn_add_despesa)
        val btnReturn = findViewById<ImageButton>(R.id.btn_return)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)

        btnReturn.setOnClickListener {
            Thread.sleep(500)
            startActivity(Intent(this, Principal::class.java))
            finish()
        }

        btnOptions.setOnClickListener {
            showPopupMenu(btnOptions)
        }
        
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
                    Thread.sleep(500)
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

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        btnAddDespesa.setOnClickListener {
            val intent = Intent(this@Viatge, CrearDespesa::class.java).apply {
                putExtra("viatgeId", viatgeId)
                putExtra("emailCreador", emailCreador)
            }
            startActivity(intent)
            finish()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val googleEmail = sharedPreferences.getString("googleEmail", "")

            viatgeInfo = backendManager.getViatge(googleEmail, viatgeId)!!

            runOnUiThread {
                val textView = findViewById<TextView>(R.id.textViewViatge)
                textView.text = viatgeInfo.nomViatge
                drawerLayout.visibility = View.VISIBLE
            }
        }
        showDespeses(contentFrame)
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu)

        val deleteMenuItem = popupMenu.menu.findItem(R.id.menu_delete)

        val spannableString = SpannableString(deleteMenuItem.title)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        deleteMenuItem.title = spannableString

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val intent = Intent(this, ViatgeEditar::class.java).apply {
                        putExtra("viatgeInfo", viatgeInfo)
                    }
                    Thread.sleep(500)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.menu_informe -> {
                    //
                    true
                }
                R.id.menu_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Estàs segur de que vols eliminar el viatge?")
                        .setPositiveButton("Sí") { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val sharedPreferences =
                                    getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                                val googleEmail = sharedPreferences.getString("googleEmail", "")
                                backendManager.deleteViatge(googleEmail, viatgeInfo.viatgeId)
                            }
                            Thread.sleep(500)
                            startActivity(Intent(this@Viatge, Principal::class.java))
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

    private fun showDespeses(contentFrame: FrameLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            val viatgeId = intent.getStringExtra("viatgeId")
            val emailCreador = intent.getStringExtra("emailCreador")
            val despeses: List<DespesaShowInfo> = BackendManager().getDespeses(emailCreador, viatgeId)

            val linearLayout = LinearLayout(contentFrame.context)
            linearLayout.orientation = LinearLayout.VERTICAL

            val despesesPerData = HashMap<Date, MutableList<DespesaShowInfo>>()

            for (despesa in despeses) {
                val data: Date = despesa.dataInici
                if (!despesesPerData.containsKey(data)) {
                    despesesPerData[data] = mutableListOf()
                }
                despesesPerData[data]?.add(despesa)
            }

            runOnUiThread {
                for ((data, listaDespesas) in despesesPerData) {
                    val headerView = createHeaderView(data)
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

    private fun createHeaderView(data: Date): View {
        val inflater = LayoutInflater.from(this@Viatge)
        val headerView = inflater.inflate(R.layout.header_data_despesa, null)

        val textViewFecha = headerView.findViewById<TextView>(R.id.textData)
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val datasdf = dateFormat.format(data)
        textViewFecha.text = datasdf

        return headerView
    }

    private fun createCardViewForDespesa(despesaShowInfo: DespesaShowInfo): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.cards_despeses, null) as CardView

        val textViewNomDespesa = cardView.findViewById<TextView>(R.id.textNomDespesa)
        textViewNomDespesa.text = despesaShowInfo.nomDespesa

        val textViewPreu = cardView.findViewById<TextView>(R.id.textPreu)
        textViewPreu.text = despesaShowInfo.preu.toString() + " " + viatgeInfo.divisa

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
}
