package com.skuvahn.module.home.activities

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityHomeBinding
import com.skuvahn.module.home.adapters.BannerAdapter
import com.skuvahn.module.home.adapters.LiveBookingsAdapter
import com.skuvahn.module.home.listeners.BookingClickListener
import com.skuvahn.module.home.models.BookingsResponseModel
import com.skuvahn.module.home.states.HomeState
import com.skuvahn.module.home.viewmodels.HomeViewModel
import com.skuvahn.utilities.CustomToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>(), BookingClickListener {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var liveBookingsAdapter: LiveBookingsAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    override fun getLayoutResourceId() = R.layout.activity_home

    override fun initView() {
        super.initView()

        initBanner()
        attachClicks()
        observe()

        viewModel.getHome(pref.token, pref.userId)
    }

    // ---------------- OBSERVE ----------------
    private fun observe() {

        viewModel.state.observe(this) {

            when (it) {

                is HomeState.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                is HomeState.Success -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    val data = it.data

                    // -------- USER --------

                    pref.phoneNumber = data.user?.phone ?: ""
                    pref.email = data.user?.email ?: ""
                    pref.profilePic = data.user?.imageUrl ?: ""
                    pref.name = data.user?.fullName ?: ""

                    binding.tvGreeting.text =
                        "Good Morning, ${data.user?.fullName ?: ""}"

                    // -------- BANNERS --------
                    val banners = data.banners ?: emptyList()

                    bannerAdapter.submitList(banners)

                    currentPage = 0
                    binding.bannerViewPager.setCurrentItem(0, false)

                    initDots(0)

                    if (banners.size > 1) {
                        startAutoScroll()
                    }

                    // -------- BOOKINGS --------
                    val bookings = data.activeBookings ?: emptyList()

                    if (bookings.isEmpty()) {
                        binding.layoutActiveBookingContainer.visibility = View.GONE
                    } else {
                        binding.layoutActiveBookingContainer.visibility = View.VISIBLE

                        liveBookingsAdapter =
                            LiveBookingsAdapter(this, ArrayList(bookings))

                        liveBookingsAdapter.bookingClickListener = this

                        binding.rvActiveBookings.apply {
                            layoutManager = LinearLayoutManager(
                                this@HomeActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            setHasFixedSize(true)
                            adapter = liveBookingsAdapter
                        }
                    }
                }

                is HomeState.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

    // ---------------- BANNER ----------------
    private fun initBanner() {

        bannerAdapter = BannerAdapter()
        binding.bannerViewPager.adapter = bannerAdapter

        binding.bannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                initDots(position)
            }
        })
    }

    private fun startAutoScroll() {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(object : Runnable {
            override fun run() {

                val count = bannerAdapter.itemCount
                if (count <= 1) return

                currentPage = (currentPage + 1) % count
                binding.bannerViewPager.setCurrentItem(currentPage, true)

                handler.postDelayed(this, 3000)
            }
        }, 3000)
    }

    private fun initDots(position: Int) {

        val count = bannerAdapter.itemCount

        if (count <= 1) {
            binding.layoutDots.visibility = View.GONE
            return
        }

        binding.layoutDots.visibility = View.VISIBLE
        binding.layoutDots.removeAllViews()

        repeat(count) {

            val dot = View(this)
            dot.setBackgroundResource(
                if (it == position) R.drawable.dot_active
                else R.drawable.dot_inactive
            )

            val params = LinearLayout.LayoutParams(16, 16)
            params.marginEnd = 8
            dot.layoutParams = params

            binding.layoutDots.addView(dot)
        }
    }

    // ---------------- CLICKS ----------------
    private fun attachClicks() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getHome(pref.token, pref.userId)
        }

        binding.layoutBookings.setOnClickListener {
            openA<BookingsActivity>()
        }

        binding.layoutChildren.setOnClickListener {
            openA<ChildrenActivity>()
        }

        binding.layoutAddress.setOnClickListener {
            openA<AddressActivity>()
        }

        binding.layoutProfile.setOnClickListener {
            openA<UserProfileActivity>()
        }

        binding.btnBookRide.setOnClickListener {
            openA<BookingAddActivity>()
        }
    }

    // ---------------- BOOKING CLICK ----------------
    override fun onBookingClick(booking: BookingsResponseModel.Booking) {

        val intent = Intent(this, BookingDetailsActivity::class.java)
        intent.putExtra("booking", booking)
        startActivity(intent)
    }

    // ---------------- CLEANUP ----------------
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}