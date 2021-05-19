package com.example.android.sossego

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.example.android.sossego.database.gratitude.repository.GratitudeRepository
import com.example.android.sossego.database.journal.repository.JournalRepository
import com.example.android.sossego.database.quotes.work.RefreshDataWorker
import com.example.android.sossego.database.user.repository.UserRepository
import com.example.android.sossego.ui.gratitude.listing.GratitudeFragment
import com.example.android.sossego.ui.login.LoginViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), KoinComponent {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1001
        const val TAG = "MainActivity"
//        const val SIGN_IN_RESULT_CODE = 1001
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val loginViewModel: LoginViewModel by inject()

    private val userRepository: UserRepository by inject()

    private val applicationScope = CoroutineScope(Dispatchers.Default)


    private fun initKoin() {
        val gratitudeModule = module {
            single {
                GratitudeRepository.getInstance()
            }

        }

        val journalModule = module {
            single {
                JournalRepository.getInstance()
            }
        }

        val userModule = module {
            single {
                UserRepository.getInstance()
            }
        }

        val loginModule = module {
            single {
                LoginViewModel()
            }
        }

            // start Koin!
        startKoin {
            // declare used Android context
            androidContext(applicationContext)
            // declare modules
            modules(listOf(gratitudeModule, journalModule, userModule, loginModule))
        }
    }

    private fun delayedInit() {
        applicationScope.launch {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresCharging(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }.build()

        val repeatingRequest
                = PeriodicWorkRequestBuilder<RefreshDataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            RefreshDataWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_gratitude, R.id.navigation_journal, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        delayedInit()

        initKoin()

        // Observe the auth state here and re-draw options menu if it changes
        loginViewModel.authenticationState.observe(this, { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
                    Timber.tag(GratitudeFragment.TAG).d("Logged in success")
                    invalidateOptionsMenu()
                }
                else -> {
                    Timber.tag(GratitudeFragment.TAG).e("Not authenticated down. Re-draw menu")
                    invalidateOptionsMenu()
                }
            }
        })

    }

    override fun onSupportNavigateUp(): Boolean{
        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Timber.tag(TAG).d("Create options menu w/ user ${FirebaseAuth.getInstance().currentUser?.displayName}")

        when(FirebaseAuth.getInstance().currentUser){
            null -> menuInflater.inflate(R.menu.action_menu, menu)
            else -> menuInflater.inflate(R.menu.auth_action_menu, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.login_menu_item -> {
                Timber.tag(TAG).d("Launch Sign-in flow")
                launchSignInFlow()
                return true
            }
            R.id.logout_menu_item -> {
                Timber.tag(TAG).d("Sign-out the user")
                AuthUI.getInstance().signOut(applicationContext)
                return true
            }
            R.id.settings_menu_item -> {
                Timber.tag(TAG).d("Settings menu item selected")
                launchSettings()
                return true
            }

        }


        return super.onOptionsItemSelected(item)
    }

    private fun launchSettings(){
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(R.id.settingsFragment)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Timber.tag(TAG).i("Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                if (response != null) {
                    if(response.isNewUser){
                        Timber.tag(TAG).i("Create new user in the db")
                        userRepository.createNewUser(FirebaseAuth.getInstance().currentUser!!.uid,
                            FirebaseAuth.getInstance().currentUser?.displayName,
                            FirebaseAuth.getInstance().currentUser?.email)
                    }
                }
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Timber.tag(TAG).i("Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
            // This is where you can provide more ways for users to register and
            // sign in.
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }
}

