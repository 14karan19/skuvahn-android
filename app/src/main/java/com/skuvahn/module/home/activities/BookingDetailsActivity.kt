package com.skuvahn.module.home.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.bsj.bysk.BSJVideo
import com.bsj.bysk.entity.RegisterInfo
import com.bsj.bysk.interfaces.EventCallBack
import com.bsj.bysk.socket.CommandUtils
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityBookingDetailsBinding
import com.skuvahn.module.home.models.BookingsResponseModel
import com.skuvahn.utilities.CustomManager
import com.skuvahn.utilities.MultiSampleVideo
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BookingDetailsActivity :
    BaseActivity<ActivityBookingDetailsBinding>(),
    EventCallBack {

    companion object {

        /**
         * SDK SERVER
         */
        private const val SDK_IP = "8.218.56.5"

        private const val SDK_DATA_PORT = 8900

        private const val SDK_VIDEO_PORT = 8901

        /**
         * CHANNELS
         */
        private const val CHANNEL_1 = 1

        private const val CHANNEL_2 = 2

        /**
         * 0 = Main Stream
         * 1 = Sub Stream
         */
        private const val CODE_TYPE = 1
    }

    private var booking: BookingsResponseModel.Booking? = null

    private lateinit var bsjVideo: BSJVideo

    private lateinit var playerView: MultiSampleVideo

    private lateinit var registerInfo: RegisterInfo

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private var currentChannel =
        CHANNEL_1

    private var isSdkConnected =
        false

    private var isLoggedIn =
        false

    private var isVideoOpened =
        false

    private var currentVideoUrl: String? =
        null

    /**
     * SDK ACCOUNT DETAILS
     */
    private val sdkUserName =
        "Skuvahn@gmail.com"
//        "Amit@gmail.com"

    private val sdkUserId =
        1

    override fun getLayoutResourceId() =
        R.layout.activity_booking_details

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()

        setupInsets()

        window.statusBarColor =
            getColor(R.color.light_blue)

        booking =
            intent.getSerializableExtra("booking")
                    as? BookingsResponseModel.Booking

        initUi()

        requestPermissions()

        initializePlayer()

        initializeSdk()

        attachChannelClicks()
    }

    // ------------------------------------------------
    // INSETS
    // ------------------------------------------------

    private fun setupInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.rootLayout
        ) { view, insets ->

            val topInset =
                insets.getInsets(
                    WindowInsetsCompat.Type.statusBars()
                ).top

            view.setPadding(
                view.paddingLeft,
                topInset,
                view.paddingRight,
                view.paddingBottom
            )

            insets
        }
    }

    // ------------------------------------------------
    // UI
    // ------------------------------------------------

    @SuppressLint("SetTextI18n")
    private fun initUi() {

        booking?.let { booking ->

            val hasCamera =
                booking.camera != null &&
                        !booking.camera.cameraNumber.isNullOrEmpty()

            if (hasCamera) {

                binding.playerView.visibility =
                    View.VISIBLE

                binding.channelContainer.visibility =
                    View.VISIBLE

                binding.cvTrack.visibility =
                    View.VISIBLE

            } else {

                binding.playerView.visibility =
                    View.GONE

                binding.channelContainer.visibility =
                    View.GONE

                binding.cvTrack.visibility =
                    View.GONE
            }

            binding.tvChildName.text =
                booking.child.fullName

            binding.tvChildDob.text =
                formatDate(booking.child.dob)

            binding.tvChildSchool.text =
                booking.school.name

            binding.tvGrade.text =
                "${booking.child.studentClass ?: "-"} - ${booking.child.section ?: "-"}"

            binding.tvEmergencyName.text =
                booking.child.emergencyContactName

            binding.tvEmergencyPhone.text =
                booking.child.emergencyContactPhone

            binding.tvAddress.text =
                booking.address.fullAddress

            binding.tvSchool.text =
                booking.school.name

            binding.tvDate.text =
                formatDate(booking.createdAt)

            binding.tvDriverName.text =
                booking.captain?.fullName
                    ?: "Not Assigned"

            binding.tvDriverPhone.text =
                booking.captain?.phone ?: "-"

            binding.tvVehicleNumber.text =
                booking.vehicle?.registrationNumber
                    ?: "-"

            Glide.with(this)
                .load(booking.child.imageUrl)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.imgProfile)

            Glide.with(this)
                .load(booking.captain?.imageUrl)
                .placeholder(R.drawable.ic_user_placeholder)
                .into(binding.imgDriverProfile)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnTrack.setOnClickListener {

            val intent =
                Intent(
                    this,
                    LiveTrackingActivity::class.java
                )

            intent.putExtra(
                "booking",
                booking
            )

            startActivity(intent)
        }

        updateChannelUi()
    }

    // ------------------------------------------------
    // CHANNEL SWITCH
    // ------------------------------------------------

    private fun attachChannelClicks() {

        binding.btnChannel1.setOnClickListener {

            if (currentChannel == CHANNEL_1) {
                return@setOnClickListener
            }

            switchChannel(CHANNEL_1)
        }

        binding.btnChannel2.setOnClickListener {

            if (currentChannel == CHANNEL_2) {
                return@setOnClickListener
            }

            switchChannel(CHANNEL_2)
        }
    }

    private fun switchChannel(
        channel: Int
    ) {

        try {

            currentChannel =
                channel

            updateChannelUi()

            stopCurrentVideo()

            ToastUtils.showShort(
                "Switching to Channel $channel"
            )

            mainHandler.postDelayed({

                try {

                    bsjVideo.openRealTimeVideo(
                        currentChannel,
                        CODE_TYPE
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }

            }, 1200)

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun updateChannelUi() {

        if (currentChannel == CHANNEL_1) {

            binding.btnChannel1.alpha = 1f
            binding.btnChannel2.alpha = 0.5f

        } else {

            binding.btnChannel1.alpha = 0.5f
            binding.btnChannel2.alpha = 1f
        }
    }

    // ------------------------------------------------
    // PERMISSION
    // ------------------------------------------------

    private fun requestPermissions() {

        PermissionUtils
            .permissionGroup(
                PermissionConstants.STORAGE,
                PermissionConstants.PHONE,
                PermissionConstants.MICROPHONE
            )
            .callback { allGranted, _, _, _ ->

                if (!allGranted) {

                    ToastUtils.showLong(
                        "Required permissions denied"
                    )
                }
            }
            .request()
    }

    // ------------------------------------------------
    // PLAYER
    // ------------------------------------------------

    private fun initializePlayer() {

        try {

            PlayerFactory.setPlayManager(
                IjkPlayerManager::class.java
            )

            CacheFactory.setCacheManager(
                ProxyCacheManager::class.java
            )

            GSYVideoType.setShowType(
                GSYVideoType.SCREEN_MATCH_FULL
            )

            playerView =
                binding.playerView

            playerView.apply {

                isRotateViewAuto = false

                isLockLand = false

                isReleaseWhenLossAudio = true

                seekRatio = 1f
            }

        } catch (e: Exception) {

            e.printStackTrace()

            ToastUtils.showLong(
                "Player initialization failed"
            )
        }
    }

    // ------------------------------------------------
    // SDK INIT
    // ------------------------------------------------

    private fun initializeSdk() {

        try {

            val camera =
                booking?.camera

            if (camera == null) {

                ToastUtils.showLong(
                    "Camera details unavailable"
                )

                return
            }

            registerInfo = RegisterInfo().apply {

                userkey =
                    camera.cameraKey

                device =
                    camera.cameraNumber

                username =
                    sdkUserName

                userId =
                    sdkUserId

                languageType =
                    "en"
            }

            LogUtils.d(
                "SDK_REGISTER_INFO",
                Gson().toJson(registerInfo)
            )

            bsjVideo =
                BSJVideo(this)

            bsjVideo.startService(
                this,
                SDK_IP,
                SDK_DATA_PORT,
                SDK_VIDEO_PORT
            )

            isSdkConnected = true

            LogUtils.d(
                "SDK_STATUS",
                "Service Started"
            )

        } catch (e: Exception) {

            e.printStackTrace()

            LogUtils.e(
                "SDK_INIT_ERROR",
                e.message
            )

            ToastUtils.showLong(
                "SDK initialization failed"
            )
        }
    }

    // ------------------------------------------------
    // CALLBACKS
    // ------------------------------------------------

    override fun onDataStart() {

        LogUtils.d(
            "SDK_CALLBACK",
            "onDataStart"
        )

        mainHandler.postDelayed({

            loginSdk()

        }, 1500)
    }

    override fun onVoiceStart() {

        LogUtils.d(
            "SDK_CALLBACK",
            "onVoiceStart"
        )
    }

    override fun onDataDisconnect() {

        LogUtils.e(
            "SDK_CALLBACK",
            "Disconnected"
        )

        runOnUiThread {

            Toast.makeText(
                this,
                "SDK disconnected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDataTimeOut() {

        LogUtils.e(
            "SDK_CALLBACK",
            "Timeout"
        )

        runOnUiThread {

            Toast.makeText(
                this,
                "SDK timeout",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onVoice(
        voice: ByteArray?,
        type: Int
    ) {

    }

    // ------------------------------------------------
    // LOGIN
    // ------------------------------------------------

    private fun loginSdk() {

        try {

            LogUtils.d(
                "SDK_LOGIN_REQUEST",
                Gson().toJson(registerInfo)
            )

            bsjVideo.loginUserName(
                registerInfo
            )

        } catch (e: Exception) {

            e.printStackTrace()

            LogUtils.e(
                "SDK_LOGIN_EXCEPTION",
                e.message
            )

            ToastUtils.showLong(
                "SDK login failed"
            )
        }
    }

    // ------------------------------------------------
    // OPEN VIDEO
    // ------------------------------------------------

    private fun openVideo() {

        try {

            val deviceNumber =
                booking?.camera?.cameraNumber

            if (deviceNumber.isNullOrEmpty()) {

                ToastUtils.showLong(
                    "Invalid device number"
                )

                return
            }

            bsjVideo.setDevice(
                deviceNumber
            )

            LogUtils.d(
                "SDK_DEVICE",
                deviceNumber
            )

            mainHandler.postDelayed({

                try {

                    bsjVideo.openRealTimeVideo(
                        currentChannel,
                        CODE_TYPE
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }

            }, 2000)

        } catch (e: Exception) {

            e.printStackTrace()

            ToastUtils.showLong(
                "Open video failed"
            )
        }
    }

    // ------------------------------------------------
    // DATA CALLBACK
    // ------------------------------------------------

    override fun onData(
        type: Int,
        string: String?
    ) {

        LogUtils.d(
            "SDK_TYPE",
            type.toString()
        )

        LogUtils.d(
            "SDK_RESPONSE",
            string ?: "NULL"
        )

        if (string.isNullOrEmpty()) {
            return
        }

        try {

            val jsonObject =
                Gson().fromJson(
                    string,
                    JsonObject::class.java
                )

            val status =
                jsonObject.get(
                    "status"
                )?.asInt ?: -1

            when (type) {

                CommandUtils.REQUEST_TYPE_REGISTER -> {

                    if (status == 1) {

                        isLoggedIn = true

                        runOnUiThread {

                            ToastUtils.showLong(
                                "SDK Login Success"
                            )
                        }

                        openVideo()

                    } else {

                        val info =
                            jsonObject.get(
                                "info"
                            )?.asString
                                ?: "Login Failed"

                        runOnUiThread {

                            ToastUtils.showLong(
                                info
                            )
                        }
                    }
                }

                CommandUtils.REQUEST_TYPE_REAL_TIME_VIDEO_OPEN -> {

                    if (status == 2) {

                        try {

                            val mediaAddress =
                                jsonObject.getAsJsonObject(
                                    "media_address"
                                )

                            val videoUrl =
                                mediaAddress.get(
                                    "video_address"
                                )?.asString

                            LogUtils.d(
                                "VIDEO_URL",
                                videoUrl
                            )

                            if (!videoUrl.isNullOrEmpty()) {

                                currentVideoUrl =
                                    videoUrl

                                runOnUiThread {

                                    startVideo(
                                        videoUrl
                                    )
                                }
                            }

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()

            LogUtils.e(
                "SDK_PARSE_ERROR",
                e.message
            )
        }
    }

    // ------------------------------------------------
    // PLAY VIDEO
    // ------------------------------------------------

    private fun startVideo(
        videoUrl: String
    ) {

        try {

            isVideoOpened = true

            stopCurrentVideo()

            LogUtils.d(
                "PLAY_VIDEO",
                videoUrl
            )

            GSYVideoOptionBuilder()
                .setUrl(videoUrl)
                .setCacheWithPlay(false)
                .setVideoTitle("")
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setAutoFullWithSize(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(false)
                .build(playerView)

            playerView.startPlayLogic()

        } catch (e: Exception) {

            e.printStackTrace()

            LogUtils.e(
                "VIDEO_PLAY_ERROR",
                e.message
            )

            ToastUtils.showLong(
                "Video playback failed"
            )
        }
    }

    private fun stopCurrentVideo() {

        try {

            playerView.currentPlayer?.release()

            playerView.release()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    // ------------------------------------------------
    // FORMAT DATE
    // ------------------------------------------------

    private fun formatDate(
        date: String?
    ): String {

        return try {

            val input =
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.getDefault()
                )

            val output =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )

            val parsed =
                input.parse(date ?: "")

            output.format(
                parsed ?: Date()
            )

        } catch (e: Exception) {
            "-"
        }
    }

    // ------------------------------------------------
    // LIFECYCLE
    // ------------------------------------------------

    override fun onPause() {
        super.onPause()

        try {

            playerView.onVideoPause()

            CustomManager.onPauseAll()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()

        try {

            playerView.onVideoResume()

            CustomManager.onResumeAll()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    override fun onDestroy() {

        releaseEverything()

        super.onDestroy()
    }

    // ------------------------------------------------
    // RELEASE
    // ------------------------------------------------

    private fun releaseEverything() {

        try {

            mainHandler.removeCallbacksAndMessages(
                null
            )

            if (isVideoOpened) {

                try {

                    bsjVideo.closeRealTimeVideo(
                        currentChannel,
                        CODE_TYPE
                    )

                } catch (e: Exception) {

                    e.printStackTrace()
                }

                try {

                    stopCurrentVideo()

                } catch (e: Exception) {

                    e.printStackTrace()
                }

                try {

                    CustomManager.clearAllVideo()

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            if (isSdkConnected) {

                try {

                    bsjVideo.stopService()

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}