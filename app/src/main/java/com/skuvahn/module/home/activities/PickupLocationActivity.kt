package com.skuvahn.module.home.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityPickupLocationBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class PickupLocationActivity : BaseActivity<ActivityPickupLocationBinding>(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var selectedLatLng: LatLng? = null
    private var selectedAddress = ""

    private lateinit var placesClient: PlacesClient

    override fun getLayoutResourceId() = R.layout.activity_pickup_location

    override fun initView() {
        super.initView()

        window.statusBarColor = getColor(R.color.light_blue)

        // Init Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }
        placesClient = Places.createClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        attachClicks()
        setupSearch()
    }

    // ------------------ CURRENT LOCATION ------------------
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            }
        }
    }

    // ------------------ SEARCH ------------------
    private fun setupSearch() {

        binding.etSearch.setOnEditorActionListener { _, _, _ ->

            val query = binding.etSearch.text.toString()
            if (query.isEmpty()) return@setOnEditorActionListener false

            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->

                    if (response.autocompletePredictions.isNotEmpty()) {
                        val prediction = response.autocompletePredictions[0]
                        getLatLngFromPlace(prediction)
                    }
                }

            true
        }
    }

    private fun getLatLngFromPlace(prediction: AutocompletePrediction) {

        val placeId = prediction.placeId

        val request = FetchPlaceRequest.builder(
            placeId,
            listOf(Place.Field.LAT_LNG)
        ).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.latLng
                latLng?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
                }
            }
    }

    // ------------------ MAP ------------------
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getCurrentLocation()

        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            selectedLatLng = center
            fetchAddress(center)
        }
    }

    // ------------------ ADDRESS ------------------
    private fun fetchAddress(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!list.isNullOrEmpty()) {
                selectedAddress = list[0].getAddressLine(0)
                binding.etSearch.setText(selectedAddress)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ------------------ BUTTONS ------------------
    private fun attachClicks() {
        binding.btnSave.setOnClickListener {

            selectedLatLng?.let {

                val resultIntent = intent.apply {
                    putExtra("selected_address", selectedAddress)
                    putExtra("selected_lat", it.latitude)
                    putExtra("selected_lng", it.longitude)
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

}