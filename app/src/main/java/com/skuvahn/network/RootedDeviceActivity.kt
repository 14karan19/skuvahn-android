package com.skuvahn.network

import com.skuvahn.base.view.BaseActivity
import com.skuvahn.R
import com.skuvahn.databinding.ActivityRootedDeviceErrorBinding

class RootedDeviceActivity : BaseActivity<ActivityRootedDeviceErrorBinding>() {

    override fun initView() {
        super.initView()
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_rooted_device_error
    }

    override fun onBackPressed() {
        //do nothing
    }
}