package com.mcandle.dutyfree.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mcandle.dutyfree.databinding.FragmentConnectionCompleteBinding

class ConnectionCompleteFragment : Fragment() {

    private var _binding: FragmentConnectionCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // This screen acts as a waiting room after BLE connection succeeds.
        // Usually the POS scanner sends the actual "Payment Complete" event through GATT
        // But for UI flow testing purposes, we could add a button or just wait.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
