package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import com.xinthink.muzei.photos.worker.BuildConfig
import okhttp3.OkHttpClient
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
        private const val TAG = "MZPTokenSvc"

        @Volatile
        private var mTokenInfo: TokenInfo = TokenInfo()
        val tokenInfo: TokenInfo get() = mTokenInfo
        val accessToken: String get() = mTokenInfo.accessToken

        private fun create(): TokenService = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/oauth2/v4/")
            .client(OkHttpClient.Builder()
                .addLoggingInterceptor()
                .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()

        /** Restore saved token */
        fun Context.loadToken(): TokenInfo {
            mTokenInfo = loadSavedToken()
            return mTokenInfo
        }

        /**
         * Exchange an authorization code for an access token.
         *
         * @see <a href="https://developers.google.com/identity/protocols/OAuth2InstalledApp#exchange-authorization-code">Step 5: Exchange authorization code for refresh and access tokens</a>
         */
        suspend fun Context.exchangeAccessToken(serverAuthCode: String): TokenInfo {
            val token = create().exchangeAccessToken(serverAuthCode)
            mTokenInfo = saveToken(token)
            if (BuildConfig.DEBUG) Log.d(TAG, "save exchanged token=$token now=${System.currentTimeMillis()}")
            return token
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
            mTokenInfo = saveToken(token)
            if (BuildConfig.DEBUG) Log.d(TAG, "save refreshed token=$token now=${System.currentTimeMillis()}")
        }
    }
}
