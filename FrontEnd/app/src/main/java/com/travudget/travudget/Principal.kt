package com.travudget.travudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.content.Context
import android.text.Html
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Principal : AppCompatActivity() {
    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)

        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val createViatge = findViewById<ImageButton>(R.id.btn_add_viatge)
        createViatge.setOnClickListener {
            showPopupMenu(createViatge)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
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

                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        showViatges(contentFrame)
    }

    private fun showViatges(contentFrame: FrameLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val googleEmail = sharedPreferences.getString("googleEmail", "")
            val viatges: List<ViatgeShowInfo> = backendManager.getViatges(googleEmail) + backendManager.getViatgesParticipant(googleEmail)

            val linearLayout = LinearLayout(contentFrame.context)
            linearLayout.orientation = LinearLayout.VERTICAL

            runOnUiThread {
                for (viatge in viatges) {
                    val cardView = createCardViewForViatge(viatge)

                    cardView.setOnClickListener {
                        val intent = Intent(this@Principal, Viatge::class.java).apply {
                            putExtra("viatgeId", viatge.viatgeId)
                            putExtra("emailCreador", viatge.emailCreador)
                        }
                        Thread.sleep(500)
                        startActivity(intent)
                        finish()
                    }

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.bottomMargin = 20
                    cardView.layoutParams = layoutParams
                    linearLayout.addView(cardView)
                }
                contentFrame.addView(linearLayout)
            }
        }
    }


    private fun createCardViewForViatge(viatgeShowInfo: ViatgeShowInfo): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.cards_viatges, null) as CardView

        val textView = cardView.findViewById<TextView>(R.id.textView)
        textView.text = viatgeShowInfo.nomViatge
        textView.setTextColor(Color.BLACK)

        return cardView
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu_crear)

        val viatgeId = intent.getStringExtra("viatgeId")
        val emailCreador = intent.getStringExtra("emailCreador")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_crear -> {
                    Thread.sleep(500)
                    startActivity(Intent(this, CrearViatge::class.java))
                    finish()
                    true
                }
                R.id.menu_unio -> {
                    Thread.sleep(500)
                    startActivity(Intent(this, IntroduirCodi::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}