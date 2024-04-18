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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions()
//            .position(sydney)
//            .title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }
}

//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        mMap!!.setOnMapClickListener { latLng ->
//            mMap!!.clear()
//            mMap!!.addMarker(MarkerOptions().position(latLng))
//
//            val returnIntent = Intent()
//            returnIntent.putExtra("lat", latLng.latitude)
//            returnIntent.putExtra("long", latLng.longitude)
//            setResult(Activity.RESULT_OK, returnIntent)
//            finish()
//        }
//    }
//}
