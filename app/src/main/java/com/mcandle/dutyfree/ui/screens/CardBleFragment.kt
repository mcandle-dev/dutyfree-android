package com.mcandle.dutyfree.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mcandle.dutyfree.R
import com.mcandle.dutyfree.advertise.AdvertiserManager
import com.mcandle.dutyfree.data.PassportStore
import com.mcandle.dutyfree.databinding.FragmentCardBleBinding
import com.mcandle.dutyfree.gatt.GattServerManager
import com.mcandle.dutyfree.model.AdvertiseMode
import com.mcandle.dutyfree.model.EncodingType
import com.mcandle.dutyfree.viewmodel.BleAdvertiseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CardBleFragment : Fragment(), GattServerManager.GattServerCallback {

    private var _binding: FragmentCardBleBinding? = null
    private val binding get() = _binding!!

    private val bleViewModel: BleAdvertiseViewModel by viewModels()

    private lateinit var passportStore: PassportStore
    private var passportDataString = ""

    private lateinit var gattServerManager: GattServerManager
    private lateinit var advertiserManager: AdvertiserManager

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startBleAdvertising()
        } else {
            Toast.makeText(context, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        passportStore = PassportStore(requireContext())
        gattServerManager = GattServerManager(requireContext(), this)
        advertiserManager = AdvertiserManager(requireContext(), bleViewModel)

        loadUserData()

        binding.btnAdvertise.setOnClickListener {
            checkPermissionsAndStartBle()
        }

        observeBleState()
    }

    private fun loadUserData() {
        val info = passportStore.getPassportInfo()
        val fullName = if (info.firstName.isEmpty() && info.lastName.isEmpty()) {
            "JOONHO KIM" 
        } else {
            "${info.firstName} ${info.lastName}"
        }
        binding.tvUserName.text = fullName
        
        passportDataString = "$fullName|${info.passportNumber}" 
        binding.tvCardNumber.text = "PASS PORT ${info.passportNumber.take(4)}"
    }

    private fun checkPermissionsAndStartBle() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            return
        }

        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startBleAdvertising()
        }
    }

    private fun startBleAdvertising() {
        if (bleViewModel.isAdvertising.value == true) {
            stopAdvertising()
        } else {
            val info = passportStore.getPassportInfo()
            
            // 멤버십 데모 데이터 (문서상 10~16자리 제한, 실제 연동 전 하드코딩)
            val memberNo = "DF2023091234"
            val grade = "V"
            val point = "258000"
            
            // 여권 및 출국 정보 연동
            val passportNo = info.passportNumber.ifEmpty { "M12345678" }
            val firstName = info.firstName.ifEmpty { "JOONHO" }
            val lastName = info.lastName.ifEmpty { "KIM" }
            
            // YYYY-MM-DD -> YYMMDD 형식 변환
            val expiry = info.expiryDate.replace("-", "").let { 
                if (it.length >= 8) it.substring(2..7) else "300520" 
            }
            
            val airline = info.airline?.take(2) ?: "KE"
            val flightNo = info.flightNumber ?: "123"
            val depTime = info.departureTime?.replace(":", "")?.take(4) ?: "1430"
            
            // 통합데이터 규격: 멤버십번호|등급|포인트#여권번호|영문이름|영문성|만료일|항공사|편명|출국시간
            val unifiedPacket = "$memberNo|$grade|$point#$passportNo|$firstName|$lastName|$expiry|$airline|$flightNo|$depTime"
            
            // 광고데이터 규격: 영문이름 영문성|전화번호끝4자리 (최대 20Bytes)
            val phoneLast4 = "1234" // 데모 데이터
            val nameSegment = "$firstName $lastName"
            val payloadStr = "$nameSegment|$phoneLast4"
            val deviceNameStr = if (payloadStr.length > 20) payloadStr.substring(0, 20) else payloadStr

            bleViewModel.updateData(
                cardNumber = memberNo,
                phoneLast4 = phoneLast4,
                deviceName = deviceNameStr,
                encoding = EncodingType.ASCII,
                advMode = AdvertiseMode.DATA
            )
            
            gattServerManager.startGattServer()
            gattServerManager.setUnifiedPacket(unifiedPacket.toByteArray(Charsets.UTF_8))
        }
    }

    private fun stopAdvertising() {
        bleViewModel.setAdvertising(false)
        advertiserManager.stopAdvertise()
        gattServerManager.stopGattServer()
    }

    private fun observeBleState() {
        bleViewModel.isAdvertising.observe(viewLifecycleOwner) { isAdvertising ->
            if (isAdvertising) {
                binding.tvBleStatus.text = "광고 전송중... POS 리더기에 태그해주세요"
                binding.tvBleStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green))
                binding.btnAdvertise.text = "바코드 전송 중지"
            } else {
                binding.tvBleStatus.text = "결제 준비 (BLE 대기중)"
                binding.tvBleStatus.setTextColor(android.graphics.Color.parseColor("#888888"))
                binding.btnAdvertise.text = "결제 바코드 생성 및 전송"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAdvertising()
        _binding = null
    }

    // --- GattServerManager.GattServerCallback Implementation ---

    override fun onGattServerReady(success: Boolean, errorMessage: String?) {
        requireActivity().runOnUiThread {
            if (success) {
                val currentData = bleViewModel.currentData.value
                if (currentData != null) {
                    advertiserManager.startAdvertise(currentData)
                } else {
                    Toast.makeText(requireContext(), "Data preparation failed", Toast.LENGTH_SHORT).show()
                    stopAdvertising()
                }
            } else {
                Toast.makeText(requireContext(), "GATT Server failed: $errorMessage", Toast.LENGTH_SHORT).show()
                stopAdvertising()
            }
        }
    }

    override fun onConnectCommandReceived(device: BluetoothDevice) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), "POS Connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnectCommandReceived(device: BluetoothDevice) {
        requireActivity().runOnUiThread {
            stopAdvertising()
            Toast.makeText(requireContext(), "POS Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOrderReceived(orderId: String, additionalData: Map<String, String>?) {
        requireActivity().runOnUiThread {
            stopAdvertising()
            findNavController().navigate(R.id.action_cardBleFragment_to_connectionCompleteFragment)
        }
    }

    override fun onClientConnected(device: BluetoothDevice) {
        // Just log or show toast
    }

    override fun onClientDisconnected(device: BluetoothDevice) {
        requireActivity().runOnUiThread {
            stopAdvertising()
        }
    }
}
