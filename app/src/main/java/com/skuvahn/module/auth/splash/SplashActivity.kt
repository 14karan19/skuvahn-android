package com.skuvahn.module.auth.splash

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.scottyab.rootbeer.RootBeer
import com.skuvahn.BuildConfig
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivitySplashBinding
import com.skuvahn.module.auth.login.LoginActivity
import com.skuvahn.module.home.activities.HomeActivity
import com.skuvahn.network.RootedDeviceActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            openA<RootedDeviceActivity>()
            finish()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        super.initView()
        binding.version.text = "Version: ${BuildConfig.VERSION_NAME}"

        initListener()
        navigateToNext()
    }

    private fun initListener() {
        binding.btnClose.setOnClickListener {
            finishAffinity()
        }
    }

    private fun navigateToNext() {
        if (pref.isLoggedIn) {
            openA<HomeActivity>()
            finish()
        } else {
            openA<LoginActivity>()
            finish()
        }
    }
}
