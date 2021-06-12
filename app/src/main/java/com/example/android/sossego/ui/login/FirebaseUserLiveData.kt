package com.example.android.sossego.ui.login

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.LiveData
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 *
 * Note that onActive() and onInactive() will get triggered when the configuration changes (for
 * example when the device is rotated). This may be undesirable or expensive depending on the
 * nature of your LiveData object, but is okay for this purpose since we are only adding and
 * removing the authStateListener.
 */
class FirebaseUserLiveData : LiveData<FirebaseUser?>(), KoinComponent {
    private val firebaseAuth: FirebaseAuth by inject()

//    init{
//        firebaseAuth.useEmulator("10.0.2.2", 9099)
//    }

    // set the value of this FireUserLiveData object by hooking it up to equal the value of the
    //  current FirebaseUser. Utilize the FirebaseAuth.AuthStateListener callback to get
    //  updates on the current Firebase user logged into the app.
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        value = firebaseAuth.currentUser
    }

    // When this object has an active observer, start observing the FirebaseAuth state to see if
    // there is currently a logged in user.
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    // When this object no longer has an active observer, stop observing the FirebaseAuth state to
    // prevent memory leaks.
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}