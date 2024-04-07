package com.travudget.travudget

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Button
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
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Recompenses : AppCompatActivity() {
    private val backendManager = BackendManager()
    private var puntsUsuari: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recompenses)

        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val contentFrame = findViewById<FrameLayout>(R.id.content_frame)
        val puntsText = findViewById<TextView>(R.id.textViewPunts)

        showRecompenses(contentFrame)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        runOnUiThread {
            CoroutineScope(Dispatchers.IO).launch {
                val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                val googleEmail = sharedPreferences.getString("googleEmail", "")

                Thread.sleep(100)
                puntsUsuari = backendManager.getPunts(googleEmail)
                Thread.sleep(100)
                runOnUiThread {
                    puntsText.text = puntsUsuari.toString()
                }
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
                    startActivity(Intent(this, Principal::class.java))
                    finish()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_recompenses -> {
                    startActivity(Intent(this, Recompenses::class.java))
                    finish()
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


    }

    private fun showRecompenses(contentFrame: FrameLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            backendManager.createRecompensa("Booking", 1000, "ABCDE")
            backendManager.createRecompensa("Vueling", 1500, "QWERT")
            val recompenses: List<RecompensaInfo> = backendManager.getRecompenses()

            val linearLayout = LinearLayout(contentFrame.context)
            linearLayout.orientation = LinearLayout.VERTICAL

            runOnUiThread {
                for (recompensa in recompenses.take(2)) {
                    val cardView = createCardViewForRecompensa(recompensa)
                    val button = cardView.findViewById<Button>(R.id.button)

                    val imageView = cardView.findViewById<ImageView>(R.id.imageView)
                    val imageView2 = cardView.findViewById<ImageView>(R.id.imageView2)

                    if (recompensa.nomRecompensa == "Booking") {
                        imageView.visibility = View.VISIBLE
                        imageView2.visibility = View.INVISIBLE
                    } else if (recompensa.nomRecompensa == "Vueling") {
                        imageView.visibility = View.INVISIBLE
                        imageView2.visibility = View.VISIBLE
                    }

                    button.setOnClickListener {
                        val punts = recompensa.preu
                        val alertDialogBuilder = AlertDialog.Builder(this@Recompenses)
                        alertDialogBuilder.setTitle("Vols bescanviar aquesta recompensa?")
                        alertDialogBuilder.setMessage("$punts punts")

                        alertDialogBuilder.setPositiveButton("Sí") { _, _ ->
                            val message = "<b>${recompensa.codi}</b>"
                            val spannedMessage = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)

                            AlertDialog.Builder(this@Recompenses)
                                .setTitle("CODI:")
                                .setMessage(spannedMessage)
                                .setPositiveButton("OK") { _, _ ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                                        val googleEmail = sharedPreferences.getString("googleEmail", "")

                                        backendManager.getRecompensa(googleEmail, recompensa.idRecompensa)
                                    }
                                    startActivity(Intent(this@Recompenses, Recompenses::class.java))
                                    finish()
                                }
                                .create()
                                .show()
                        }

                        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }

                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.show()
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

    private fun createCardViewForRecompensa(recompensaInfo: RecompensaInfo): CardView {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.cards_recompenses, null) as CardView

        cardView.setCardBackgroundColor(Color.TRANSPARENT)

        val textView = cardView.findViewById<TextView>(R.id.textView)
        textView.text = recompensaInfo.nomRecompensa
        textView.setTextColor(Color.BLACK)

        // val photo =
        return cardView
    }
}