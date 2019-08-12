package com.xinthink.muzei.photos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.xinthink.muzei.photos.TokenService.Companion.exchangeAccessToken
import com.xinthink.muzei.photos.worker.BuildConfig
import kotlinx.android.synthetic.main.fragment_auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk19.listeners.onClick
import org.jetbrains.anko.toast

/**
 * The Authorization screen
 */
@ExperimentalCoroutinesApi
class AuthFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var navController: NavController
    private lateinit var signInClient: GoogleSignInClient
    private lateinit var photosService: PhotosService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = NavHostFragment.findNavController(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_auth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in_button.setSize(SignInButton.SIZE_WIDE)
        sign_in_button.onClick { onSignIn() }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(BuildConfig.AUTH_CLIENT_ID, true)
            .requestScopes(Scope(BuildConfig.AUTH_SCOPE_PHOTOS))
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        signInClient = GoogleSignIn.getClient(context!!, gso)
        photosService = PhotosService.create()
    }

    override fun onResume() {
        super.onResume()
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(context)
        updateUI(account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(resultCode, task)
        }
    }

    private fun updateUI(account: GoogleSignInAccount? = null, isLoading: Boolean = false) {
        Log.d(TAG, "account logged in: $account")
        val loggedIn = account != null
        sign_in_button.visibility = if (loggedIn) View.GONE else View.VISIBLE
        loading_indicator.visibility = if (isLoading) View.VISIBLE else View.GONE

//        txt_msg.visibility = if (loggedIn) View.VISIBLE else View.GONE
//        btn_library.visibility = txt_msg.visibility
//
//        if (loggedIn) {
//            txt_msg.text = "Logged in as ${account!!.displayName}\nToken: ${account.serverAuthCode}"
//            btn_library.onClick { onTest(account) }
//        }
    }

    private fun onSignIn() {
        updateUI(isLoading = true)
        val signInIntent = signInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun handleSignInResult(resultCode: Int, completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                exchangeAccessToken(account) // Signed in successfully, fetch access token now
            } else if (resultCode != Activity.RESULT_CANCELED) {
                context?.toast("Google Sign-In failed")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=${e.statusCode}", e)
//            updateUI(null)

            if (resultCode != Activity.RESULT_CANCELED) {
                context?.toast("Google Sign-In failed: ${e.message}")
            }
        }
    }

    /** fetch access token */
    private fun exchangeAccessToken(account: GoogleSignInAccount) {
        launch {
            try {
                updateUI(account, isLoading = true)
                context?.exchangeAccessToken(account.serverAuthCode ?: "")

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "token fetched, return to previous screen")
                    navController.popBackStack()
                }
            } catch (e: Throwable) {
                context?.toast("Authorization failed: ${e.message}")
                Log.e(TAG, "fetchAccessToken failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "AUTH"
        private const val RC_SIGN_IN = 1
    }
}
