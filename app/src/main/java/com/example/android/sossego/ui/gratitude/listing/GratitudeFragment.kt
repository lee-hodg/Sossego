package com.example.android.sossego.ui.gratitude.listing

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

/**
 * This is responsible for the listing of gratitude lists which will be displayed
 * in a recyclerview.
 */
class GratitudeFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentGratitudeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_gratitude, container, false)

        // Build view model with access to the database by using a factory
        val application = requireNotNull(this.activity).application
        val dataSource = GratitudeDatabase.getInstance(application).gratitudeDatabaseDao
        val viewModelFactory = GratitudeViewModelFactory(dataSource, application)
        val gratitudeViewModel = ViewModelProvider(
                this, viewModelFactory).get(GratitudeViewModel::class.java)


        // Make it accessible to the binding (remember the xml must have outer layout tag
        // for data-binding to work)
        binding.gratitudeViewModel = gratitudeViewModel

        // Set this to be the lifecycle owern
        binding.lifecycleOwner = this

        // Add an Observer on this variable to tell us when to navigate to the detail view
        // navigation is UI so is done in the fragment, but click-handler changes data on the
        // view model. The action takes the id of the gratitudeList whose detail we want to view.
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
                            GratitudeFragmentDirections.actionNavigationGratitudeToGratitudeDetailFragment(gratitudeListId))
                    // Reset state to make sure we only navigate once, even if the device
                    // has a configuration change.
                    gratitudeViewModel.doneNavigating()
                }
            })

        // Build an adapter for our recyclerview.
        // We feed the adapter a GratitudeListListener, which itself is constructed with a lambda
        // function that takes the Id. GratitudeListListener.onClick will then call this function
        val gratitudeListAdapter = GratitudeListAdapter(GratitudeListListener { gratitudeListId ->
            gratitudeViewModel.onGratitudeListClicked(gratitudeListId)
        })

        // We observe the gratitudeLists liveData of the view model. If it changes we must
        // rebuild the recycler view with submitList
        gratitudeViewModel.gratitudeLists.observe(viewLifecycleOwner, {
            it?.let {
                gratitudeListAdapter.submitGratitudeList(it)
            }
        })

        // Set our recyclerview to use this adapter
        binding.gratitudeListRecycler.adapter = gratitudeListAdapter

        return binding.root
    }

}