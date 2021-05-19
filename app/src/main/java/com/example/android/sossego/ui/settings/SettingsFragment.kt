package com.example.android.sossego.ui.settings

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.example.android.sossego.ui.login.LoginViewModel
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import androidx.navigation.fragment.findNavController
import com.example.android.sossego.R
import timber.log.Timber


class SettingsFragment : PreferenceFragmentCompat(), KoinComponent {

    companion object {
        const val TAG = "SettingsFragment"
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val loginViewModel: LoginViewModel by inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        loginViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> Timber.tag(TAG).d("Authenticated")
                // If the user is not logged in, they should not be able to set any preferences,
                LoginViewModel.AuthenticationState.UNAUTHENTICATED -> navController.navigate(
                    SettingsFragmentDirections.actionSettingsFragmentToNavigationGratitude())
                else -> Timber.tag(TAG).e("New $authenticationState state that doesn't require any UI change")
            }
        })
    }
}