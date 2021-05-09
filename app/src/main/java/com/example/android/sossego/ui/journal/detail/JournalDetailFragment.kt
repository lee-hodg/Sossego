package com.example.android.sossego.ui.journal.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.sossego.R
import com.example.android.sossego.database.journal.JournalDatabase
import com.example.android.sossego.databinding.FragmentJournalDetailBinding


class JournalEntryDetailFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.title = "Edit Journal"

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentJournalDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_journal_detail, container, false
        )

        // Now we build the view model and take the safeargs passed
        val application = requireNotNull(this.activity).application
        val arguments = JournalEntryDetailFragmentArgs.fromBundle(requireArguments())


        // Create an instance of the ViewModel Factory.
        val dataSource = JournalDatabase.getInstance(application).journalDatabaseDao
        val viewModelFactory = JournalEntryViewModelFactory(arguments.journalEntryIdKey, dataSource)
        // Get a reference to the ViewModel associated with this fragment.
        val journalEntryDetailViewModel =
            ViewModelProvider(
                this, viewModelFactory
            ).get(JournalEntryDetailViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.journalEntryDetailViewModel = journalEntryDetailViewModel

        binding.journalEntryEditText.onFocusChangeListener = journalEntryDetailViewModel.onFocusChangeListener()

        binding.lifecycleOwner = this

        // Add an Observer to the state variable for Navigating when a Quality icon is tapped.
        journalEntryDetailViewModel.navigateToListing.observe(viewLifecycleOwner, {
            if (it == true) { // Observed state is true.
                this.findNavController().navigate(
                    JournalEntryDetailFragmentDirections.actionJournalEntryDetailFragment2ToNavigationJournal()
                )
                // Reset state to make sure we only navigate once, even if the device
                // has a configuration change.
                journalEntryDetailViewModel.doneNavigating()
            }
        })


        return binding.root
    }
}


