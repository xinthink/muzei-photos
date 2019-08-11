package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.xinthink.muzei.photos.worker.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.jetbrains.anko.defaultSharedPreferences
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Google API OAuth2 token exchange service.
 *
 * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp">OAuth 2.0 for Mobile & Desktop Apps</a>
 */
interface TokenService {

    /**
     * Exchange an authorization code for an access token.
     *
     * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code">Step 5: Exchange authorization code for refresh and access tokens</a>
     */
    @POST("token")
    @FormUrlEncoded
    suspend fun token(
        @Field("code") authCode: String,
        @Field("client_id") clientId: String = BuildConfig.AUTH_CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.AUTH_CLIENT_SECRET,
        @Field("redirect_uri") redirectUri: String = "",
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenInfo

    companion object {
        private const val TAG = "TokenService"

        private var mutableAccessToken: String = ""
        val accessToken: String get() = mutableAccessToken // TODO load from storage

        /** Restore saved token */
        fun Context.loadToken(): TokenInfo = defaultSharedPreferences.run {
            val token = TokenInfo(
                accessToken = getString("auth_access_token", "") ?: "",
                refreshToken = getString("auth_refresh_token", "") ?: "",
                expiresIn = getLong("auth_token_expires", 0),
                authCode = getString("auth_code", "") ?: ""
            )
            mutableAccessToken = token.accessToken
            token
        }

        fun Context.fetchAccessToken(
            scope: CoroutineScope,
            authCode: String,
            onDone: () -> Unit
        ) {
            scope.launch {
                val deferredToken = async {
                    Log.d(TAG, "fetching access token...")
                    saveAuthCode(authCode)
                    val token = create().token(authCode)
                    Log.d(TAG, "token received: $token")
                    saveToken(token)
                    token
                }

                Log.d(TAG, "before await")
                val token = deferredToken.await()
                Log.d(TAG, "got the token: $token")
                mutableAccessToken = token.accessToken
                onDone()
            }
        }

        private fun create(): TokenService = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/oauth2/v4/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()

        private fun Context.saveAuthCode(authCode: String) {
            defaultSharedPreferences.edit {
                putString("auth_code", authCode)
            }
        }

        private fun Context.saveToken(token: TokenInfo) = defaultSharedPreferences.edit {
            putString("auth_access_token", token.accessToken)
            putString("auth_refresh_token", token.refreshToken)
            putLong("auth_token_expires", token.expiresIn)
        }
    }
}
