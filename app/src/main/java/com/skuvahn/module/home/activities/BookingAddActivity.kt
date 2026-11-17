package com.skuvahn.module.home.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hes.vayu.utilities.CommonUtils.hideLoader
import com.hes.vayu.utilities.CommonUtils.showLoader
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityAddBookingBinding
import com.skuvahn.module.home.adapters.AddressDropdownAdapter
import com.skuvahn.module.home.adapters.ChildDropdownAdapter
import com.skuvahn.module.home.adapters.TenureDropdownAdapter
import com.skuvahn.module.home.listeners.AddressDropdownClickListener
import com.skuvahn.module.home.listeners.ChildDropdownClickListener
import com.skuvahn.module.home.listeners.TenureClickListener
import com.skuvahn.module.home.models.AddressResponseModel
import com.skuvahn.module.home.models.BookingCreateRequest
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.models.SchoolResponseModel
import com.skuvahn.module.home.states.BookingState
import com.skuvahn.module.home.viewmodels.BookingViewModel
import com.skuvahn.utilities.CustomToast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class BookingAddActivity : BaseActivity<ActivityAddBookingBinding>() {

    private val viewModel: BookingViewModel by viewModels()

    private lateinit var dialog: BottomSheetDialog

    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val apiFormatter =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private var children = listOf<ChildResponseModel.Child>()
    private var addresses = listOf<AddressResponseModel.Address>()
    private var schools = ArrayList<SchoolResponseModel.School>()
    private var tenures = ArrayList<String>()

    private var selectedChildId: String? = null
    private var selectedAddressId: String? = null
    private var selectedSchoolId: String? = null
    private var selectedTenure: String? = null
    private var startDate: Calendar? = null
    private lateinit var schoolAutoAdapter: ArrayAdapter<String>

    override fun getLayoutResourceId() = R.layout.activity_add_booking

    override fun initView() {
        super.initView()

        window.statusBarColor = getColor(R.color.light_blue)

        tenures = ArrayList()
        tenures.add("1 Month")
        tenures.add("2 Month")
        tenures.add("3 Month")

        attachClicks()
        observe()

        viewModel.loadBookingDropdowns(pref.token, pref.userId)
    }

    private fun observe() {

        viewModel.state.observe(this) {

            when (it) {

                is BookingState.Loading -> showLoader(this)

                is BookingState.DropdownData -> {
                    hideLoader()
                    children = it.children
                    addresses = it.addresses

                    schools = it.schools

                    val schoolNames = schools.map { school ->
                        school.name
                    }

                    schoolAutoAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        schoolNames
                    )

                    binding.actvSchool.setAdapter(schoolAutoAdapter)

                    binding.actvSchool.setOnClickListener {
                        binding.actvSchool.showDropDown()
                    }

                    binding.actvSchool.setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) {
                            binding.actvSchool.showDropDown()
                        }
                    }

                    binding.actvSchool.setOnItemClickListener { _, _, position, _ ->

                        val selectedSchool = schools[position]

                        selectedSchoolId = selectedSchool.id

                        binding.actvSchool.setText(
                            selectedSchool.name,
                            false
                        )
                    }
                }

                is BookingState.CreateSuccess -> {
                    hideLoader()
                    setResult(RESULT_OK)
                    finish()
                }

                is BookingState.Error -> {
                    hideLoader()
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

    private fun attachClicks() {

        binding.ivBack.setOnClickListener { finish() }

        binding.etStartDate.setOnClickListener {
            pickDate {
                startDate = it
                binding.etStartDate.text = dateFormatter.format(it.time)
            }
        }

        binding.selectChild.setOnClickListener { openChildSheet() }
        binding.selectAddress.setOnClickListener { openAddressSheet() }
        binding.selectTenure.setOnClickListener { openTenureSheet() }

        binding.btnSubmit.setOnClickListener {

            if (!validate()) return@setOnClickListener

            val finalDob = apiFormatter.format(startDate!!.time)

            viewModel.createBooking(
                pref.token,
                BookingCreateRequest(
                    selectedChildId!!,
                    selectedAddressId!!,
                    selectedSchoolId!!,
                    selectedTenure!!,
                    finalDob,
                    pref.userId
                )
            )
        }
    }

    // ---------------- CHILD ----------------
    private fun openChildSheet() {

        dialog = createSheet("Select Child")

        val adapter = ChildDropdownAdapter(children)
        adapter.listener = object : ChildDropdownClickListener {
            override fun onChildSelected(child: ChildResponseModel.Child) {
                selectedChildId = child.id
                binding.tvSelectedChild.text = child.fullName
                dialog.dismiss()
            }

            override fun onAddChild() {
                dialog.dismiss()
                addChildLauncher.launch(
                    Intent(
                        this@BookingAddActivity,
                        ChildAddActivity::class.java
                    )
                )
            }
        }

        setRecycler(adapter)
    }

    // ---------------- ADDRESS ----------------
    private fun openAddressSheet() {

        dialog = createSheet("Select Address")

        val adapter = AddressDropdownAdapter(addresses)
        adapter.listener = object : AddressDropdownClickListener {
            override fun onAddressSelected(address: AddressResponseModel.Address) {
                selectedAddressId = address.id
                binding.tvSelectedAddress.text = address.fullAddress
                dialog.dismiss()
            }

            override fun onAddAddress() {
                dialog.dismiss()
                addAddressLauncher.launch(
                    Intent(
                        this@BookingAddActivity,
                        AddressAddActivity::class.java
                    )
                )
            }
        }

        setRecycler(adapter)
    }

    private fun pickDate(onPicked: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                calendar.set(y, m, d)
                onPicked(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ---------------- ROUTE ----------------
    private fun openTenureSheet() {

        dialog = createSheet("Select Tenure")

        val adapter = TenureDropdownAdapter(tenures)
        adapter.listener = object : TenureClickListener {
            override fun onTenureSelected(tenure: String) {
                selectedTenure = tenure
                binding.tvSelectedTenure.text = tenure
                dialog.dismiss()
            }
        }

        setRecycler(adapter)
    }

    // ---------------- COMMON ----------------
    private lateinit var recycler: RecyclerView

    private fun createSheet(title: String): BottomSheetDialog {

        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)

        val tv = view.findViewById<TextView>(R.id.tvHeading)
        recycler = view.findViewById(R.id.rvValues)

        tv.text = title
        tv.visibility = View.VISIBLE

        recycler.layoutManager = LinearLayoutManager(this)

        dialog.setContentView(view)
        dialog.show()

        return dialog
    }

    private fun setRecycler(adapter: RecyclerView.Adapter<*>) {
        recycler.adapter = adapter
    }

    // ---------------- VALIDATION ----------------
    private fun validate(): Boolean {

        if (selectedChildId == null) {
            CustomToast.showMessage(this, "Select child")
            return false
        }

        if (selectedAddressId == null) {
            CustomToast.showMessage(this, "Select address")
            return false
        }

        if (selectedSchoolId == null) {
            CustomToast.showMessage(this, "Select school")
            return false
        }

        if (selectedTenure == null) {
            CustomToast.showMessage(this, "Select tenure")
            return false
        }

        return true
    }

    private val addChildLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                viewModel.loadBookingDropdowns(pref.token, pref.userId)
        }

    private val addAddressLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                viewModel.loadBookingDropdowns(pref.token, pref.userId)
        }
}