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
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Viatge : AppCompatActivity() {

    private lateinit var viatgeInfo: ViatgeInfo
    private val backendManager = BackendManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viatge)

        val btnOptions = findViewById<ImageButton>(R.id.btn_options)
        val btnReturn = findViewById<ImageButton>(R.id.btn_return)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        btnReturn.setOnClickListener {
            startActivity(Intent(this, Principal::class.java))
            finish()
        }

        btnOptions.setOnClickListener {
            showPopupMenu(btnOptions)
        }
        
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
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
}
