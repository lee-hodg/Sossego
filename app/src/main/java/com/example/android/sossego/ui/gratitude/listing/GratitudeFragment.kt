package com.example.android.sossego.ui.gratitude.listing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.sossego.R
import com.example.android.sossego.database.gratitude.FirebaseGratitudeList
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.database.quotes.database.QuoteDatabase
import com.example.android.sossego.database.user.repository.User
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.databinding.FragmentGratitudeBinding
import com.example.android.sossego.ui.gratitude.detail.SwipeToDeleteCallback
import com.example.android.sossego.ui.login.LoginViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import timber.log.Timber

/**
 * This is responsible for the listing of gratitude lists which will be displayed
 * in a recyclerview.
 */
class GratitudeFragment : Fragment(), KoinComponent {

    companion object {
        const val TAG = "GratitudeFragment"
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val loginViewModel: LoginViewModel by inject()

    private val gratitudeRepository: GratitudeRepository by inject()

    private val userRepository: UserRepository by inject()

    private lateinit var gratitudeViewModel: GratitudeViewModel

    private lateinit var gratitudeListListener: ValueEventListener

    private lateinit var streakCountListener: ValueEventListener

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                // Change importance
                NotificationManager.IMPORTANCE_HIGH
            )
                // Disable badges for this channel
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.channel_description)

            val notificationManager = requireActivity().getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }


    override fun onDestroy() {
        Timber.tag(TAG).d("onDestory called for GratitudeFragment")
        super.onDestroy()
        // Unhook the listener
        gratitudeViewModel.authenticatedUserId.value?.let {
            Timber.tag(TAG).d("Unhook the firebase event value listeners")
            gratitudeRepository.removeGratitudeListValueEventListener(
                it, gratitudeListListener)
            userRepository.removeStreakCountListener(streakCountListener, it)
        }
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        // Channel for notifications
        createChannel(
            getString(R.string.periodic_reminder_channel_id),
            getString(R.string.periodic_reminder_channel_name)
        )
        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentGratitudeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_gratitude, container, false)

        // Build view model with access to the database by using a factory
        val application = requireNotNull(this.activity).application
        val viewModelFactory = GratitudeViewModelFactory(gratitudeRepository, application)
        gratitudeViewModel = ViewModelProvider(
                this, viewModelFactory).get(GratitudeViewModel::class.java)


        // Observe the authentication state so we can know if the user has logged in successfully.
        // We record this state on the gratitudeViewModel
        loginViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Timber.tag(TAG).d("Logged in success")
                    gratitudeViewModel.userLoggedIn()
                }
                else -> {
                    Timber.tag(TAG).d("Un-authenticated: $authenticationState")
                    gratitudeViewModel.userLoggedOut()
                }
            }
        })
        // Track user displayName
        loginViewModel.userDisplayName.observe(viewLifecycleOwner, { displayName ->
            gratitudeViewModel.setUserDisplayName(displayName)
        })

        // Make it accessible to the binding (remember the xml must have outer layout tag
        // for data-binding to work)
        binding.gratitudeViewModel = gratitudeViewModel

        // Set this to be the lifecycle owner
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

        // Set the event listener callbacks
        gratitudeListListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // Figure out how this can be some function we pass in?
                val listOfGratitudeLists : MutableList<FirebaseGratitudeList> = mutableListOf()

                // Ensure the lists are reverse createdDate ordered
                val sortedGratitudeLists = dataSnapshot.children.sortedByDescending { "createdDate" }

                for(gratitudeList in sortedGratitudeLists) {
                    val item = gratitudeList.getValue<FirebaseGratitudeList>()
                    listOfGratitudeLists.add(item!!)
                }

                gratitudeListAdapter.submitGratitudeList(listOfGratitudeLists)

            }

            override fun onCancelled(p0: DatabaseError) {
                Timber.d("onCancelled called for gratitudeListListener. Error: $p0")
            }
        }

        // Set the streak count
        streakCountListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userInstance = dataSnapshot.getValue<User>()
                gratitudeViewModel.streakCount.value = userInstance?.streakCount ?: 1
            }

            override fun onCancelled(p0: DatabaseError) {
                Timber.d("onCancelled called for streakCountListener. Error: $p0")
            }
        }


        // Observe the authentication state so we can know if the user has logged in successfully.
        // After log-in/log-out we add/remove the event listener with the userId
        // and we also record the userId on the gratitudeViewModel (or set it to null)
        loginViewModel.authenticationUserId.observe(viewLifecycleOwner, { authUserId ->
            when(authUserId){
                null -> {
                    // Unhook the listener now we have unauthenticated user
                    gratitudeViewModel.authenticatedUserId.value?.let {
                        gratitudeRepository.removeGratitudeListValueEventListener(
                            it,
                            gratitudeListListener)
                        userRepository.removeStreakCountListener(streakCountListener, it)
                    }
                }
                else -> {
                    // Hook up the listener for this auth user id
                    gratitudeRepository.addGratitudeListValueEventListener(authUserId,
                        gratitudeListListener)
                    userRepository.addStreakCountListener(streakCountListener, authUserId)
                }
            }
            // Either way make sure our view model records the latest user id (or null)
            gratitudeViewModel.setAuthenticatedUserId(authUserId)

        })


        // Set our recyclerview to use this adapter
        binding.gratitudeListRecycler.adapter = gratitudeListAdapter

        // Swipe to delete action
        val swipeHandler = object : SwipeToDeleteCallback(this.requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val gratitudeListId = gratitudeListAdapter.currentList[viewHolder.adapterPosition] as DataItem.GratitudeListItem
                val currentListId = gratitudeListId.gratitudeList.gratitudeListId
                gratitudeViewModel.deleteGratitudeList(currentListId)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.gratitudeListRecycler)

        // The Quote viewmodel too
        val quoteDatabase = QuoteDatabase.getInstance(application)
        val quoteViewModelFactory = QuotesViewModel.Factory(quoteDatabase, application)

        val quoteViewModel = ViewModelProvider(
            this, quoteViewModelFactory).get(QuotesViewModel::class.java)

        binding.quoteViewModel = quoteViewModel

        return binding.root
    }

}