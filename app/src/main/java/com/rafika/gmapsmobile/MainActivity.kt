package com.rafika.gmapsmobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.lang.Exception

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etLat: TextInputEditText
    private lateinit var etLng: TextInputEditText
    private lateinit var btnSearch: Button
    private lateinit var fabMyLocation: FloatingActionButton

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getMyCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getMyCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        etLat = findViewById(R.id.et_lat)
        etLng = findViewById(R.id.et_lng)
        btnSearch = findViewById(R.id.btn_search)
        fabMyLocation = findViewById(R.id.fab_my_location)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupActionListeners()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        requestLocationPermission()
    }

    private fun setupActionListeners() {
        btnSearch.setOnClickListener {
            addMarkerFromInput()
        }
        fabMyLocation.setOnClickListener {
            getMyCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getMyCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun getMyCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    Toast.makeText(this, "Gagal mendapatkan lokasi. Aktifkan GPS Anda.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun addMarkerFromInput() {
        val latString = etLat.text.toString()
        val lngString = etLng.text.toString()

        if (latString.isEmpty() || lngString.isEmpty()) {
            Toast.makeText(this, "Latitude dan Longitude tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val lat = latString.toDouble()
            val lng = lngString.toDouble()

            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                Toast.makeText(this, "Koordinat tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            val newLocation = LatLng(lat, lng)
            mMap.addMarker(MarkerOptions().position(newLocation).title("Marker dari Input"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))

        } catch (e: Exception) {
            Toast.makeText(this, "Input koordinat tidak valid", Toast.LENGTH_SHORT).show()
        }
    }
}