package com.example.android.sossego.ui.journal.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.sossego.R
import com.example.android.sossego.database.journal.FirebaseJournalEntry
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.databinding.FragmentJournalDetailBinding
import com.example.android.sossego.hideKeyboard
import com.example.android.sossego.ui.gratitude.detail.GratitudeDetailFragment
import com.example.android.sossego.ui.gratitude.detail.GratitudeDetailFragmentDirections
import com.example.android.sossego.ui.gratitude.listing.GratitudeFragment
import com.example.android.sossego.ui.login.LoginViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import timber.log.Timber


class JournalEntryDetailFragment : Fragment(), KoinComponent {

    private val journalRepository: JournalRepository by inject()

    private var journalEntryKey: String? = null

    companion object{
        const val TAG = "JournalDetailFrag"
    }

//    private var journalEntryDetailListener: ValueEventListener? = null
//
//
//    override fun onStop() {
//        super.onStop()
//        if(journalEntryKey != null && journalEntryDetailListener != null) {
//            journalRepository.removeJournalEntryDetailValueEventListener(journalEntryDetailListener,
//                journalEntryKey)
//        }
//    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val loginViewModel by viewModels<LoginViewModel>()


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
//        val application = requireNotNull(this.activity).application
        val arguments = JournalEntryDetailFragmentArgs.fromBundle(requireArguments())
        journalEntryKey = arguments.journalEntryIdKey

        // Create an instance of the ViewModel Factory.
        val viewModelFactory = JournalEntryViewModelFactory(arguments.journalEntryIdKey,
        journalRepository)
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

        // Observe the authentication state so we can know if the user has logged in successfully.
        // If the user has logged in successfully
        loginViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Timber.tag(TAG).d("Logged in success")
                }
                else -> {
                    Timber.tag(GratitudeFragment.TAG).d("Un-authenticated: $authenticationState")
                    // If logged out navigate back to listing
                    this.findNavController().navigate(
                        JournalEntryDetailFragmentDirections.actionJournalEntryDetailFragment2ToNavigationJournal()
                    )
                    // Reset state to make sure we only navigate once, even if the device
                    // has a configuration change.
                    journalEntryDetailViewModel.doneNavigating()
                }
            }
        })

        // Hide the soft keyboard for example when journal text is saved
        // this is a UI action so happens in the fragment, but the logic to determine
        // if it should happen lives in the viewmodel
        journalEntryDetailViewModel.hideSoftKeyboard.observe(viewLifecycleOwner, {
            it?.let {
                hideKeyboard()
                journalEntryDetailViewModel.softKeyboardHidden()
            }
        })

        val journalEntryDetailListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val journalEntry = dataSnapshot.getValue<FirebaseJournalEntry>()
                journalEntryDetailViewModel.journalEntry.value = journalEntry
            }

            override fun onCancelled(p0: DatabaseError) {
                Timber.d("onCancelled called")
            }
        }
        Timber.d("Add entry detail listener with Id ${arguments.journalEntryIdKey}")
        journalRepository.addJournalEntryDetailValueEventListener(journalEntryDetailListener,
            arguments.journalEntryIdKey)

        return binding.root
    }
}


