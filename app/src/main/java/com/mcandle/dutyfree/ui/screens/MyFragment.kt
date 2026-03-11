package com.mcandle.dutyfree.ui.screens

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.mcandle.dutyfree.R
import com.mcandle.dutyfree.data.PassportStore
import com.mcandle.dutyfree.databinding.FragmentMyBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    private lateinit var passportStore: PassportStore

    private val dfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passportStore = PassportStore(requireContext())

        setupToolbar()
        setupAirlineSpinner()
        setupDatePickers()
        setupValidation()
        setupSaveButton()

        loadSavedData()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "여권 / 출국 정보 설정"
    }

    private fun setupAirlineSpinner() {
        val airlines = arrayOf("대한항공 (KE)", "아시아나항공 (OZ)", "제주항공 (7C)", "진에어 (LJ)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, airlines)
        binding.spinnerAirline.setAdapter(adapter)

        val levels = arrayOf("SILVER", "GOLD", "VIP", "VVIP")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        binding.spinnerMemberLevel.setAdapter(levelAdapter)
    }

    private fun setupDatePickers() {
        binding.etExpiryDate.setOnClickListener {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("여권 만료일 선택")
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                binding.etExpiryDate.setText(dfDate.format(Date(selection)))
                validateForm()
            }
            datePicker.show(childFragmentManager, "EXPIRY_DATE_PICKER")
        }

        binding.etDepartureTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("출국 시간 선택")
                .build()

            picker.addOnPositiveButtonClickListener {
                val timeStr = String.format("%02d:%02d", picker.hour, picker.minute)
                binding.etDepartureTime.setText(timeStr)
                validateForm()
            }
            picker.show(childFragmentManager, "TIME_PICKER")
        }
    }

    private fun setupValidation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etMemberName.addTextChangedListener(textWatcher)
        binding.etMembershipNo.addTextChangedListener(textWatcher)
        binding.etPhoneNumber.addTextChangedListener(textWatcher)
        binding.etPassportNum.addTextChangedListener(textWatcher)
        binding.etFirstName.addTextChangedListener(textWatcher)
        binding.etLastName.addTextChangedListener(textWatcher)
        binding.etExpiryDate.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val name = binding.etMemberName.text.toString().trim()
        val membershipNo = binding.etMembershipNo.text.toString().trim()
        val phone = binding.etPhoneNumber.text.toString().trim()
        val passportNum = binding.etPassportNum.text.toString().trim()
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val expiryDate = binding.etExpiryDate.text.toString().trim()

        val isValid = name.isNotEmpty() &&
                membershipNo.isNotEmpty() &&
                phone.isNotEmpty() &&
                passportNum.isNotEmpty() &&
                firstName.isNotEmpty() &&
                lastName.isNotEmpty() &&
                expiryDate.isNotEmpty()

        binding.btnSave.isEnabled = isValid
        if (isValid) {
            binding.btnSave.setBackgroundColor(requireContext().getColor(R.color.primary_navy))
        } else {
            binding.btnSave.setBackgroundColor(requireContext().getColor(R.color.text_secondary))
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val info = com.mcandle.dutyfree.data.PassportInfo(
                membershipNo = binding.etMembershipNo.text.toString().trim().uppercase(),
                memberLevel = binding.spinnerMemberLevel.text.toString().trim(),
                phoneNumber = binding.etPhoneNumber.text.toString().trim(),
                memberName = binding.etMemberName.text.toString().trim(),
                passportNumber = binding.etPassportNum.text.toString().trim().uppercase(),
                firstName = binding.etFirstName.text.toString().trim().uppercase(),
                lastName = binding.etLastName.text.toString().trim().uppercase(),
                expiryDate = binding.etExpiryDate.text.toString().trim(),
                airline = binding.spinnerAirline.text.toString().trim(),
                flightNumber = binding.etFlightNum.text.toString().trim().uppercase(),
                departureTime = binding.etDepartureTime.text.toString().trim()
            )
            passportStore.savePassportInfo(info)
            
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            
            Toast.makeText(context, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedData() {
        val info = passportStore.getPassportInfo()
        binding.etMemberName.setText(info.memberName)
        binding.etMembershipNo.setText(info.membershipNo)
        binding.spinnerMemberLevel.setText(info.memberLevel, false)
        binding.etPhoneNumber.setText(info.phoneNumber)
        binding.etPassportNum.setText(info.passportNumber)
        binding.etFirstName.setText(info.firstName)
        binding.etLastName.setText(info.lastName)
        binding.etExpiryDate.setText(info.expiryDate)
        binding.spinnerAirline.setText(info.airline, false)
        binding.etFlightNum.setText(info.flightNumber)
        binding.etDepartureTime.setText(info.departureTime)
        
        validateForm()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
