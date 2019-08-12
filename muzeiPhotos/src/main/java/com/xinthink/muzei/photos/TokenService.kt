package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.xinthink.muzei.photos.worker.BuildConfig
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
    suspend fun exchangeAccessToken(
        @Field("code") authCode: String,
        @Field("client_id") clientId: String = BuildConfig.AUTH_CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.AUTH_CLIENT_SECRET,
        @Field("redirect_uri") redirectUri: String = "",
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenInfo

    /**
     * Refresh an access token.
     *
     * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#offline">Refreshing an access token</a>
     */
    @POST("token")
    @FormUrlEncoded
    suspend fun refreshAccessToken(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String = BuildConfig.AUTH_CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.AUTH_CLIENT_SECRET,
        @Field("grant_type") grantType: String = "refresh_token"
    ): TokenInfo

    companion object {
        private const val TAG = "TokenService"

        @Volatile
        private var mTokenInfo: TokenInfo = TokenInfo()
        val tokenInfo: TokenInfo get() = mTokenInfo
        val accessToken: String get() = mTokenInfo.accessToken

        private fun create(): TokenService = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/oauth2/v4/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()

        /** Restore saved token */
        fun Context.loadToken(): TokenInfo = defaultSharedPreferences.run {
            mTokenInfo = TokenInfo(
                accessToken = getString("auth_access_token", "") ?: "",
                refreshToken = getString("auth_refresh_token", "") ?: "",
                expiresIn = getLong("auth_token_expires_in", 0),
                mExpiresAt = getLong("auth_token_expires_at", 0)
            )
            mTokenInfo
        }

        private fun Context.saveToken(token: TokenInfo) {
            mTokenInfo = token
            defaultSharedPreferences.edit {
                token.computeExpiresAt() // compute expires before persisting
                putString("auth_access_token", token.accessToken)
                putString("auth_refresh_token", token.refreshToken)
                putLong("auth_token_expires_in", token.expiresIn)
                putLong("auth_token_expires_at", token.expiresAt)
            }
        }

        /**
         * Exchange an authorization code for an access token.
         *
         * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code">Step 5: Exchange authorization code for refresh and access tokens</a>
         */
        suspend fun Context.exchangeAccessToken(serverAuthCode: String) {
            val token = create().exchangeAccessToken(serverAuthCode)
            saveToken(token)
            Log.d(TAG, "save exchanged token=$token now=${System.currentTimeMillis()}")
        }

        /**
         * Refresh an access token.
         *
         * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#offline">Refreshing an access token</a>
         */
        suspend fun Context.refreshAccessToken(originToken: TokenInfo) {
            var token = create().refreshAccessToken(originToken.refreshToken)
            token = originToken.copy(
                accessToken = token.accessToken,
                expiresIn = token.expiresIn
            )
            saveToken(token)
            Log.d(TAG, "save refreshed token=$token now=${System.currentTimeMillis()}")
        }
    }
}
