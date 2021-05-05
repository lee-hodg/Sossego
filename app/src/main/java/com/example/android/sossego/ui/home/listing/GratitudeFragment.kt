package com.example.android.sossego.ui.home.listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.sossego.R
import com.example.android.sossego.database.GratitudeDatabase
import com.example.android.sossego.databinding.FragmentGratitudeBinding

class GratitudeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentGratitudeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_gratitude, container, false)

        // Build view model with database inject with factory
        val application = requireNotNull(this.activity).application
        val dataSource = GratitudeDatabase.getInstance(application).gratitudeDatabaseDao
        val viewModelFactory = GratitudeViewModelFactory(dataSource, application)
        val gratitudeViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(GratitudeViewModel::class.java)
        // make it accessible to the binding
        binding.gratitudeViewModel = gratitudeViewModel

        binding.lifecycleOwner = this

        // Add an Observer on the state variable for Navigating when STOP button is pressed.
        gratitudeViewModel.navigateToGratitudeListDetail.observe(viewLifecycleOwner,
            { gratitudeListId ->
                gratitudeListId?.let {
                    // We need to get the navController from this, because button is not ready, and it
                    // just has to be a view. For some reason, this only matters if we hit stop again
                    // after using the back button, not if we hit stop and choose a quality.
                    // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                    // popping the stack to get the correct behavior if we press stop multiple times
                    // followed by back.
                    // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra
                    this.findNavController().navigate(
                            GratitudeFragmentDirections.actionNavigationHomeToGratitudeDetailFragment(gratitudeListId))
                    // Reset state to make sure we only navigate once, even if the device
                    // has a configuration change.
                    gratitudeViewModel.doneNavigating()
                }
            })

        val gratitudeListAdapter = GratitudeListAdapter(GratitudeListListener { gratitudeListId ->
            gratitudeViewModel.onGratitudeListClicked(gratitudeListId)
        })

        gratitudeViewModel.gratitudeLists.observe(viewLifecycleOwner, {
            it?.let {
                gratitudeListAdapter.submitGratitudeList(it)
            }
        })

        binding.gratitudeListRecycler.adapter = gratitudeListAdapter

        return binding.root
    }
}