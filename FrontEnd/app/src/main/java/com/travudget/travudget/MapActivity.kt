package com.travudget.travudget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import android.util.Log

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // Inicializar el SDK de mapas
        MapsInitializer.initialize(applicationContext)

        // Obtener el mapa de forma asíncrona
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.setOnMapClickListener { latLng ->
            mMap!!.clear()
            mMap!!.addMarker(MarkerOptions().position(latLng))

            val returnIntent = Intent()
            returnIntent.putExtra("lat", latLng.latitude)
            returnIntent.putExtra("long", latLng.longitude)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}
