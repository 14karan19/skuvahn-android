package com.skuvahn.module.home.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hes.vayu.utilities.CommonUtils.hideLoader
import com.hes.vayu.utilities.CommonUtils.showLoader
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityAddAddressBinding
import com.skuvahn.module.home.models.AddAddressRequest
import com.skuvahn.module.home.states.AddressState
import com.skuvahn.module.home.viewmodels.AddressViewModel
import com.skuvahn.utilities.CustomToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddressAddActivity : BaseActivity<ActivityAddAddressBinding>(),
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val viewModel: AddressViewModel by viewModels()

    private var selectedLatLng: LatLng? = null
    private var selectedAddress = ""
    private var pinCode = ""
    private var city = ""

    private lateinit var placesClient: PlacesClient

    private val pickupLocationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                val data = result.data

                val address = data?.getStringExtra("selected_address") ?: return@registerForActivityResult

                val lat = data.getDoubleExtra("selected_lat", 0.0)
                val lng = data.getDoubleExtra("selected_lng", 0.0)

                selectedAddress = address
                selectedLatLng = LatLng(lat, lng)

                binding.tvAddress.text = address

                val latLng = LatLng(lat, lng)

                if (::mMap.isInitialized) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                    )
                }

                fetchAddress(latLng)
            }
        }

    override fun getLayoutResourceId() = R.layout.activity_add_address

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
        observe()
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
                binding.tvAddress.text = selectedAddress
                pinCode = list[0].postalCode ?: ""
                city = list[0].locality ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ------------------ BUTTONS ------------------
    private fun attachClicks() {

        binding.btnSave.setOnClickListener {

            if (!validate()) return@setOnClickListener

            val selectedChipId = binding.chipGroup.checkedChipId

            val addressType = when (selectedChipId) {
                R.id.chipHome -> "Home"
                R.id.chipWork -> "Work"
                R.id.chipOther -> binding.etOtherTag.text.toString().ifEmpty { "Other" }
                else -> "Home"
            }

            val request = AddAddressRequest(
                addressType = addressType,
                latitude = selectedLatLng!!.latitude.toString(),
                longitude = selectedLatLng!!.longitude.toString(),
                locality = city,
                city = binding.etCity.text.toString(),
                state = binding.etState.text.toString(),
                fullAddress = binding.etHouse.text.toString(),
                landmark = binding.etLandmark.text.toString().ifBlank { null },
                pincode = pinCode,
                userId = pref.userId
            )

            viewModel.addAddress(pref.token, request)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val id = checkedIds[0]
                binding.etOtherTag.visibility =
                    if (id == R.id.chipOther) View.VISIBLE else View.GONE
            }
        }

        binding.btnPickupLocation.setOnClickListener {
            pickupLocationLauncher.launch(
                Intent(this, PickupLocationActivity::class.java)
            )
        }
    }

    private fun validate(): Boolean {

        if (selectedLatLng == null) {
            CustomToast.showMessage(this, "Location not selected")
            return false
        }

        if (binding.etHouse.text.isNullOrBlank()) {
            CustomToast.showMessage(this, "Enter house/flat details")
            return false
        }

        if (binding.etCity.text.isNullOrBlank()) {
            CustomToast.showMessage(this, "Enter city")
            return false
        }

        if (binding.etState.text.isNullOrBlank()) {
            CustomToast.showMessage(this, "Enter state")
            return false
        }

        if (binding.chipGroup.checkedChipId == -1) {
            CustomToast.showMessage(this, "Select address type")
            return false
        }

        return true
    }

    private fun observe() {

        viewModel.state.observe(this) {

            when (it) {

                is AddressState.Loading -> showLoader(this)

                is AddressState.AddSuccess -> {
                    hideLoader()
                    CustomToast.showMessage(this, it.message)
                    setResult(RESULT_OK)
                    finish()
                }

                is AddressState.Error -> {
                    hideLoader()
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

}