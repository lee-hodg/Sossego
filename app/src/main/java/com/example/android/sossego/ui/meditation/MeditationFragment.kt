package com.example.android.sossego.ui.meditation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.android.sossego.R
import com.example.android.sossego.databinding.FragmentMediationTimerBinding


class MeditationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMediationTimerBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_mediation_timer, container, false
        )

        val viewModel = ViewModelProvider(this).get(MeditationTimerViewModel::class.java)

        binding.mediationTimerViewModel = viewModel
        binding.lifecycleOwner = this.viewLifecycleOwner

        return binding.root
    }
}