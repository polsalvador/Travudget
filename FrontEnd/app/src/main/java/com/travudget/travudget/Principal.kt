package com.travudget.travudget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat

class Principal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.principal)

        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_viatges -> {
                    // Aquí colocas las acciones que deseas realizar
                    // Mantén la misma pantalla y cierra el DrawerLayout
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true // Indica que el item ha sido manejado
                }
                // Agrega más casos para otros items del menú si es necesario
                else -> false // Indica que el item no ha sido manejado
            }
        }
    }

}