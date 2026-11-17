package com.skuvahn.module.home.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hes.vayu.utilities.CommonUtils
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityLiveTrackingBinding
import com.skuvahn.module.home.models.BookingsResponseModel
import com.skuvahn.module.home.models.Vehicle
import com.skuvahn.module.home.states.LiveTrackingState
import com.skuvahn.module.home.viewmodels.LiveTrackingViewModel
import com.skuvahn.utilities.CustomToast
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class LiveTrackingActivity : BaseActivity<ActivityLiveTrackingBinding>(),
    OnMapReadyCallback {

    private var booking: BookingsResponseModel.Booking? = null

    private var camera: BookingsResponseModel.Camera? = null

    private val viewModel: LiveTrackingViewModel by viewModels()

    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun getLayoutResourceId() = R.layout.activity_live_tracking

    override fun initView() {
        super.initView()

        booking =
            intent.getSerializableExtra("booking")
                    as? BookingsResponseModel.Booking

        camera = booking?.camera

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        attachClicks()
        observe()
    }

    private fun attachClicks() {
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        startTracking()
    }

    // ---------------- TRACKING LOOP ----------------

    private fun startTracking() {

        handler.post(object : Runnable {
            override fun run() {
                viewModel.getLiveLocation(camera!!.cameraNumber, camera!!.cameraKey)

                handler.postDelayed(this, 10000) // every 10 sec
            }
        })
    }

    // ---------------- OBSERVE ----------------

    private fun observe() {

        viewModel.state.observe(this) {

            when (it) {

                LiveTrackingState.Loading -> {
                    CommonUtils.showProgress(this)
                }

                is LiveTrackingState.Success -> {

                    CommonUtils.hideProgress()

                    it.vehicle?.let { vehicle ->
                        updateMap(vehicle)
                    }
                }

                is LiveTrackingState.Error -> {
                    CommonUtils.hideProgress()
                    CustomToast.showMessage(this, it.message)
                }
            }
        }
    }

    // ---------------- MAP UPDATE ----------------

    private fun updateMap(vehicle: Vehicle) {

        val latLng = LatLng(vehicle.lat, vehicle.lon)

        if (marker == null) {

            marker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(vehicle.plate)
                    .icon(getBitmapFromVector(this, R.drawable.ic_school_van))

            )

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

        } else {
            marker?.position = latLng
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    fun getBitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = createBitmap(vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}