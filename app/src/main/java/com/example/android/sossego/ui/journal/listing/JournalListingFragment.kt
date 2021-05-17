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
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.databinding.FragmentJournalListingBinding
import com.example.android.sossego.ui.gratitude.detail.SwipeToDeleteCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * This is responsible for the listing of journal entries which will be displayed
 * in a recyclerview.
 */
class JournalListingFragment : Fragment() {

    // Dependency inject the repository
    private val journalRepository: JournalRepository by inject()


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
        val viewModelFactory = JournalListingViewModelFactory(application,
            journalRepository)
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
                Timber.d("Navigate to journal detail with Id $journalEntryId")
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


        val journalEntriesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val journalEntries : MutableList<FirebaseJournalEntry> = mutableListOf()

                for(journalEntry in dataSnapshot.children.reversed()) {
                    val item = journalEntry.getValue<FirebaseJournalEntry>()
                    journalEntries.add(item!!)
                }


                journalEntryListAdapter.submitJournalEntryList(journalEntries)

            }

            override fun onCancelled(p0: DatabaseError) {
                Timber.d("onCancelled called")
            }
        }
        journalRepository.addJournalEntryListValueEventListener(journalEntriesListener)

        // Set our recyclerview to use this adapter
        binding.journalEntryListRecycler.adapter = journalEntryListAdapter

        // Swipe to delete action
        val swipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val dataItem = journalEntryListAdapter.currentList[viewHolder.adapterPosition]
                if(dataItem.id != null) {
                    // Do not do this for headers
                    val journalDataItem = dataItem as DataItem.JournalEntryItem
                    val journalEntry = journalDataItem.journalEntry
                    journalListingViewModel.deleteJournalEntry(journalEntry)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.journalEntryListRecycler)

        return binding.root
    }
}

