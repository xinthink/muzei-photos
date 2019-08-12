package com.xinthink.muzei.photos

import android.content.Context
import com.xinthink.muzei.photos.TokenService.Companion.refreshAccessToken
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET

interface PhotosService {

    @GET("albums")
    suspend fun albums(): AlbumsPagination

    companion object {

        /** Fetch Photos albums */
        suspend fun Context.fetchPhotosAlbums(nextPageToken: String? = null): AlbumsPagination {
            val token = TokenService.tokenInfo
            if (token.isExpired) {
                refreshAccessToken(token)
            }
            return create().albums()
        }

        private fun authorize(chain: Interceptor.Chain) = chain.proceed(
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer ${TokenService.accessToken}")
                .build()
        )

        fun create(): PhotosService = Retrofit.Builder()
            .baseUrl("https://photoslibrary.googleapis.com/v1/")
            .client(OkHttpClient.Builder()
                .addInterceptor(Companion::authorize)
                .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }
}
