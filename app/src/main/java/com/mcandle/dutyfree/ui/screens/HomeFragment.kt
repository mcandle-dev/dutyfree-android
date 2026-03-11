package com.mcandle.dutyfree.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mcandle.dutyfree.data.PassportStore
import com.mcandle.dutyfree.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var passportStore: PassportStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        passportStore = PassportStore(requireContext())
        loadUserData()
    }

    private fun loadUserData() {
        val info = passportStore.getPassportInfo()
        
        val memberName = info.memberName.ifEmpty { 
            if (info.firstName.isNotEmpty() || info.lastName.isNotEmpty()) {
                "${info.firstName} ${info.lastName}"
            } else {
                "KIM JOONHO"
            }
        }
        val memberNo = info.membershipNo.ifEmpty { "DF2023091234" }
        val memberLevel = info.memberLevel.ifEmpty { "VIP" }

        // 상단 그리팅 메시지 설정
        binding.tvUserNameGreeting.text = "$memberName 님"
        
        // 면세 카드 영역 설정
        binding.tvCardName.text = memberName
        binding.tvCardMembership.text = memberNo
        binding.tvMemberLevelBadge.text = when(memberLevel.uppercase()) {
            "VVIP" -> "👑 VVIP"
            "VIP" -> "👑 VIP"
            "GOLD" -> "⭐ GOLD"
            "SILVER" -> "⚪ SILVER"
            else -> memberLevel
        }

        // 바코드 텍스트 포맷팅 (예: DF2023091234 -> DF 2023 0912 34)
        val formattedBarcode = if (memberNo.startsWith("DF") && memberNo.length == 12) {
            "${memberNo.substring(0, 2)} ${memberNo.substring(2, 6)} ${memberNo.substring(6, 10)} ${memberNo.substring(10)}"
        } else {
            memberNo
        }
        binding.tvCardBarcodeText.text = formattedBarcode

        // 여권 정보 설정 (신규)
        binding.tvPassportNum.text = info.passportNumber.ifEmpty { "M12345678" }
        binding.tvPassportFirstName.text = info.firstName.ifEmpty { "JOONHO" }
        binding.tvPassportLastName.text = info.lastName.ifEmpty { "KIM" }
        binding.tvPassportExpiry.text = info.expiryDate.ifEmpty { "2030-05-20" }

        // 출국 정보 설정 (신규 디자인 반영)
        val airline = if (info.airline.isNotEmpty()) {
            info.airline
        } else {
            "대한항공 (KE)"
        }
        val flightNo = info.flightNumber.ifEmpty { "KE123" }
        val depTime = info.departureTime.ifEmpty { "14:30" }
        
        binding.tvAirlineName.text = airline
        binding.tvFlightNum.text = flightNo
        binding.tvDepartureTime.text = depTime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
