package com.example.android.sossego.ui.gratitude.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.database.GratitudeDatabase
import com.example.android.sossego.databinding.FragmentGratitudeDetailBinding
import timber.log.Timber


class GratitudeDetailFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */

    companion object{
        private const val TAG = "GratitudeDetailFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentGratitudeDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_gratitude_detail, container, false)

        // Now we build the view model and take the safeargs passed
        val application = requireNotNull(this.activity).application
        val arguments = GratitudeDetailFragmentArgs.fromBundle(requireArguments())

        Timber.tag(TAG).d("We got arguments $arguments")

        // Create an instance of the ViewModel Factory.
        val dataSource = GratitudeDatabase.getInstance(application).gratitudeDatabaseDao
        val viewModelFactory = GratitudeDetailViewModelFactory(arguments.gratitudeListIdKey, dataSource)
        // Get a reference to the ViewModel associated with this fragment.
        val gratitudeDetailViewModel =
            ViewModelProvider(
                this, viewModelFactory).get(GratitudeDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.gratitudeDetailViewModel = gratitudeDetailViewModel

        binding.lifecycleOwner = this

        Timber.tag(TAG).d("gratitudeDetailViewModel has list ${gratitudeDetailViewModel.gratitudeList}")
        // Add an Observer to the state variable for Navigating when a Quality icon is tapped.
        gratitudeDetailViewModel.navigateToGratitudeFragment.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    GratitudeDetailFragmentDirections.actionNavigationGratitudeDetailFragmentToHome()
                )
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                gratitudeDetailViewModel.doneNavigating()
            }
        })

        // Build an adapter for our recyclerview.
        val gratitudeDetailAdapter = GratitudeDetailAdapter(
            GratitudeItemListener { gratitudeItemId ->
            gratitudeDetailViewModel.deleteGratitudeItem(gratitudeItemId)},
            GratitudeItemTextChangedListener({gratitudeItem ->
                gratitudeDetailViewModel.updateGratitudeItem(gratitudeItem)},
                {gratitudeItem ->
                gratitudeDetailViewModel.deleteGratitudeItem(gratitudeItem.gratitudeItemId)})
        )

        // We observe the gratitudeLists liveData of the view model. If it changes we must
        // rebuild the recycler view with submitList
        gratitudeDetailViewModel.gratitudeItems.observe(viewLifecycleOwner, {
            it?.let {
                gratitudeDetailAdapter.submitGratitudeItemList(it)
            }
        })

        // Set our recyclerview to use this adapter
        binding.gratitudeDetailRecycler.adapter = gratitudeDetailAdapter

        // Swipe to delete action
        val swipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentItemId = gratitudeDetailAdapter.currentList[viewHolder.adapterPosition]
                    .gratitudeItem.gratitudeItemId
                gratitudeDetailViewModel.deleteGratitudeItem(currentItemId)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.gratitudeDetailRecycler)

        return binding.root
    }


}