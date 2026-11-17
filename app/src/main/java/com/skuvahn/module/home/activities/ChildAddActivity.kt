package com.skuvahn.module.home.activities

import android.app.DatePickerDialog
import android.net.Uri
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hes.vayu.utilities.CommonUtils
import com.hes.vayu.utilities.CommonUtils.hideLoader
import com.hes.vayu.utilities.CommonUtils.showError
import com.hes.vayu.utilities.CommonUtils.showLoader
import com.skuvahn.R
import com.skuvahn.base.view.BaseActivity
import com.skuvahn.databinding.ActivityAddChildBinding
import com.skuvahn.module.home.adapters.ClassDropdownAdapter
import com.skuvahn.module.home.adapters.SectionDropdownAdapter
import com.skuvahn.module.home.listeners.ClassClickListener
import com.skuvahn.module.home.listeners.SectionClickListener
import com.skuvahn.module.home.models.ChildAddRequest
import com.skuvahn.module.home.models.ChildResponseModel
import com.skuvahn.module.home.models.SchoolResponseModel
import com.skuvahn.module.home.states.ChildState
import com.skuvahn.module.home.viewmodels.ChildViewModel
import com.skuvahn.utilities.CustomToast
import com.skuvahn.utilities.PreferenceConstants
import com.skuvahn.utilities.image.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChildAddActivity : BaseActivity<ActivityAddChildBinding>() {

    private val viewModel: ChildViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val apiFormatter =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    private var dob: Calendar? = null
    private var imageUri: Uri? = null
    private lateinit var cameraUri: Uri

    private var selectedSchoolId: String? = null
    private var selectedClass: String? = null
    private var selectedSection: String? = null
    private var schoolList: ArrayList<SchoolResponseModel.School> = ArrayList()
    private var classList: ArrayList<String> = ArrayList()
    private var sectionList: ArrayList<String> = ArrayList()
    private var selectedChild: ChildResponseModel.Child? = null
    private lateinit var classDialog: BottomSheetDialog
    private lateinit var sectionDialog: BottomSheetDialog
    private lateinit var schoolAutoAdapter: ArrayAdapter<String>

    override fun getLayoutResourceId() = R.layout.activity_add_child

    override fun initView() {
        super.initView()

        window.statusBarColor = getColor(R.color.light_blue)

        selectedChild =
            intent.getSerializableExtra(PreferenceConstants.child) as? ChildResponseModel.Child

        if (selectedChild != null) {
            selectedSchoolId = selectedChild!!.schoolId
        }

        initClassSection()
        setupImagePicker()
        attachClicks()
        observe()

        viewModel.getSchools(pref.token)
        prefillIfEdit()
    }

    private fun initClassSection(){
        classList = ArrayList()
        classList.add("Play School")
        classList.add("Pre Nursery")
        classList.add("Nursery")
        classList.add("LKG")
        classList.add("UKG")
        classList.add("Class 1")
        classList.add("Class 2")
        classList.add("Class 3")
        classList.add("Class 4")
        classList.add("Class 5")
        classList.add("Class 6")
        classList.add("Class 7")
        classList.add("Class 8")
        classList.add("Class 9")
        classList.add("Class 10")
        classList.add("Class 11")
        classList.add("Class 12")


        sectionList = ArrayList()
        sectionList.add("A")
        sectionList.add("B")
        sectionList.add("C")
        sectionList.add("D")
        sectionList.add("E")
        sectionList.add("F")
        sectionList.add("G")
        sectionList.add("H")
        sectionList.add("I")
        sectionList.add("J")

    }

    // ---------------- IMAGE PICKER ----------------
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = ImagePicker.extractResult(this, it.data, cameraUri)
                uri?.let {
                    imageUri = it
                    binding.imgProfile.setImageURI(it)
                }
            }
        }

    private fun setupImagePicker() {
        binding.btnPickImage.setOnClickListener {
            cameraUri = ImagePicker.createCameraUri(this)
            val chooser = ImagePicker.createChooser(this, cameraUri)
            imagePickerLauncher.launch(chooser)
        }
    }

    // ---------------- PREFILL ----------------
    private fun prefillIfEdit() {
        selectedChild?.let {

            binding.title.text = "Edit Child"
            binding.btnSubmit.text = "Update Child"

            binding.etFullName.setText(it.fullName)
            binding.etDob.text = it.formattedDob
            binding.etContactName.setText(it.emergencyContactName)
            binding.etContactNumber.setText(it.emergencyContactPhone)
            binding.tvSelectedClass.text = it.studentClass
            binding.tvSelectedSection.text = it.section
            binding.actvSchool.setText(it.schoolName)
            selectedSchoolId = it.schoolId

            if (!it.imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(binding.imgProfile)
            }
        }
    }

    // ---------------- CLICK HANDLERS ----------------
    private fun attachClicks() {

        binding.ivBack.setOnClickListener { finish() }

        binding.etDob.setOnClickListener {
            pickDate {
                dob = it
                binding.etDob.text = dateFormatter.format(it.time)
            }
        }

        binding.selectClassLayout.setOnClickListener {
            showClassBottomSheet()
        }

        binding.selectSectionLayout.setOnClickListener {
            showSectionBottomSheet()
        }

        binding.btnSubmit.setOnClickListener {
            if (!validate()) return@setOnClickListener

            var finalDob = ""

            val inputFormatter = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )

            if (dob == null && selectedChild != null) {
                val date = inputFormatter.parse(selectedChild?.dob!!)
                finalDob = apiFormatter.format(date!!)
            } else {
                finalDob = apiFormatter.format(dob!!.time)
            }

            val request = ChildAddRequest(
                fullName = binding.etFullName.text.toString(),
                dob = finalDob,
                studentClass = selectedClass!!,
                section = selectedSection!!,
                schoolId = selectedSchoolId!!,
                imageUrl = selectedChild?.imageUrl ?: "",
                emergencyContactName = binding.etContactName.text.toString(),
                emergencyContactPhone = binding.etContactNumber.text.toString(),
                userId = pref.userId
            )

            viewModel.addOrUpdate(
                pref.token,
                this,
                request,
                imageUri,
                selectedChild?.id
            )
        }
    }

    // ---------------- VALIDATION ----------------
    private fun validate(): Boolean {

        var valid = true

        if (binding.etFullName.text.isNullOrBlank()) {
            binding.tvErrorName.visibility = View.VISIBLE
            valid = false
        } else binding.tvErrorName.visibility = View.GONE

//        if (binding.etGrade.text.isNullOrBlank()) {
//            binding.tvErrorGrade.visibility = View.VISIBLE
//            valid = false
//        } else binding.tvErrorGrade.visibility = View.GONE

        if (selectedClass == null) {
            CustomToast.showMessage(this, "Select Class")
            valid = false
        }

        if (selectedSection == null) {
            CustomToast.showMessage(this, "Select Section")
            valid = false
        }

        if (binding.etDob.text.isNullOrBlank()) {
            binding.tvErrorDob.visibility = View.VISIBLE
            valid = false
        } else binding.tvErrorDob.visibility = View.GONE

        if (selectedSchoolId == null) {
            CustomToast.showMessage(this, "Select school")
            valid = false
        }

        if (binding.etContactName.text.isNullOrBlank()) {
            binding.tvErrorContactName.visibility = View.VISIBLE
            valid = false
        } else binding.tvErrorContactName.visibility = View.GONE

        if (binding.etContactNumber.text.isNullOrBlank()) {
            binding.tvErrorContactNumber.text = "Please enter contact number"
            binding.tvErrorContactNumber.visibility = View.VISIBLE
            valid = false
        } else binding.tvErrorContactNumber.visibility = View.GONE

        if (!CommonUtils.isValidIndianMobile(binding.etContactNumber)){
            binding.tvErrorContactNumber.text = "Please enter valid contact number"
            binding.tvErrorContactNumber.visibility = View.VISIBLE
            valid = false
        } else binding.tvErrorContactNumber.visibility = View.GONE

        if (selectedChild == null && imageUri == null) {
            CustomToast.showMessage(this, "Select image")
            valid = false
        }

        return valid
    }

    // ---------------- OBSERVER ----------------
    private fun observe() {
        viewModel.state.observe(this) {

            when (it) {

                is ChildState.Loading -> showLoader(this)

                is ChildState.SchoolListSuccess -> {

                    schoolList = it.list

                    val schoolNames = schoolList.map { school ->
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

                        val selectedSchool = schoolList[position]

                        selectedSchoolId = selectedSchool.id

                        binding.actvSchool.setText(
                            selectedSchool.name,
                            false
                        )
                    }
                }

                is ChildState.AddSuccess,
                is ChildState.UpdateSuccess -> {
                    hideLoader()
                    setResult(RESULT_OK)
                    finish()
                }

                is ChildState.Error -> {
                    hideLoader()
                    CustomToast.showMessage(this, it.message)
                }

                else -> {}
            }
        }
    }

    // ---------------- DATE PICKER ----------------
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

    private fun showClassBottomSheet() {

        classDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)

        val rvValues = view.findViewById<RecyclerView>(R.id.rvValues)
        val tvHeading = view.findViewById<TextView>(R.id.tvHeading)

        tvHeading.text = "Select Class"
        tvHeading.visibility = View.VISIBLE

        rvValues.layoutManager = LinearLayoutManager(this)

        val adapter = ClassDropdownAdapter(classList)
        rvValues.adapter = adapter

        adapter.listener = object : ClassClickListener {
            override fun onClassClick(selected: String) {

                selectedClass = selected
                binding.tvSelectedClass.text = selectedClass

                classDialog.dismiss()
            }
        }

        classDialog.setCancelable(true)
        classDialog.setContentView(view)
        classDialog.show()
    }

    private fun showSectionBottomSheet() {

        sectionDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)

        val rvValues = view.findViewById<RecyclerView>(R.id.rvValues)
        val tvHeading = view.findViewById<TextView>(R.id.tvHeading)

        tvHeading.text = "Select Section"
        tvHeading.visibility = View.VISIBLE

        rvValues.layoutManager = LinearLayoutManager(this)

        val adapter = SectionDropdownAdapter(sectionList)
        rvValues.adapter = adapter

        adapter.listener = object : SectionClickListener {
            override fun onSectionClick(selected: String) {

                selectedSection = selected
                binding.tvSelectedSection.text = selectedSection

                sectionDialog.dismiss()
            }
        }

        sectionDialog.setCancelable(true)
        sectionDialog.setContentView(view)
        sectionDialog.show()
    }
}