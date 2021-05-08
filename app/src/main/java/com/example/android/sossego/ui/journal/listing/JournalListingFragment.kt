package com.example.android.sossego.ui.journal.listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.database.journal.JournalDatabase
import com.example.android.sossego.databinding.FragmentJournalListingBinding
import com.example.android.sossego.ui.gratitude.detail.SwipeToDeleteCallback

/**
 * This is responsible for the listing of journal entries which will be displayed
 * in a recyclerview.
 */
class JournalListingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentJournalListingBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journal_listing, container, false
        )

        // Build view model with access to the database by using a factory
        val application = requireNotNull(this.activity).application
        val dataSource = JournalDatabase.getInstance(application).journalDatabaseDao
        val viewModelFactory = JournalListingViewModelFactory(dataSource, application)
        val journalListingViewModel = ViewModelProvider(
            this, viewModelFactory
        ).get(JournalListingViewModel::class.java)


        // Make it accessible to the binding (remember the xml must have outer layout tag
        // for data-binding to work)
        binding.journalListingViewModel = journalListingViewModel

        // Set this to be the lifecycle owner
        binding.lifecycleOwner = this

        // Add an Observer on this variable to tell us when to navigate to the detail view
        // navigation is UI so is done in the fragment, but click-handler changes data on the
        // view model. The action takes the id of the gratitudeList whose detail we want to view.
        journalListingViewModel.navigateListToDetail.observe(
            viewLifecycleOwner
        ) { journalEntryId ->
            journalEntryId?.let {
                // We need to get the navController from this, because button is not ready, and it
                // just has to be a view. For some reason, this only matters if we hit stop again
                // after using the back button, not if we hit stop and choose a quality.
                // Also, in the Navigation Editor, for Quality -> Tracker, check "Inclusive" for
                // popping the stack to get the correct behavior if we press stop multiple times
                // followed by back.
                // Also: https://stackoverflow.com/questions/28929637/difference-and-uses-of-oncreate-oncreateview-and-onactivitycreated-in-fra
                this.findNavController().navigate(
                    JournalListingFragmentDirections.actionNavigationJournalToJournalEntryDetailFragment(
                        journalEntryId
                    )
                )
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                journalListingViewModel.doneNavigating()
            }
        }

        // Build an adapter for our recyclerview.
        // We feed the adapter a JournalEntryListListener, which itself is constructed with a lambda
        // function that takes the Id. GratitudeListListener.onClick will then call this function
        val journalEntryListAdapter =
            JournalEntryListAdapter(JournalEntryListener { journalEntryId ->
                journalListingViewModel.onListItemClicked(journalEntryId)
            })

        // We observe the gratitudeLists liveData of the view model. If it changes we must
        // rebuild the recycler view with submitList
        journalListingViewModel.journalEntries.observe(viewLifecycleOwner, {
            it?.let {
                journalEntryListAdapter.submitJournalEntryList(it)
            }
        })

        // Set our recyclerview to use this adapter
        binding.journalEntryListRecycler.adapter = journalEntryListAdapter

        // Swipe to delete action
        val swipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val journalEntryId = journalEntryListAdapter.currentList[viewHolder.adapterPosition] as DataItem.JournalEntryItem
                val journalEntry = journalEntryId.journalEntry
                journalListingViewModel.deleteJournalEntry(journalEntry)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.journalEntryListRecycler)

        return binding.root
    }
}

