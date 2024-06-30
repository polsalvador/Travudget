package com.travudget.travudget

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions

internal class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        createMapFragment()
    }
    private fun createMapFragment() {
        (supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)
    }
}