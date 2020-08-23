package com.xinthink.muzei.photos

import android.content.Context
import android.util.Log
import com.xinthink.muzei.photos.TokenService.Companion.refreshAccessToken
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.IOException

interface PhotosService {

    @GET("albums")
    suspend fun albums(
        @Query("pageSize") pageSize: Int = 10,
        @Query("pageToken") pageToken: String? = null
    ): AlbumsPagination

    @POST("./mediaItems:search")
    @FormUrlEncoded
    fun albumPhotos(
        @Field("albumId") albumId: String,
        @Field("pageSize") pageSize: Int = 10,
        @Field("pageToken") pageToken: String? = null
    ): Call<MediaItemsPagination>

    companion object {
        private const val TAG = "MZPhotosSvc"

        /** Fetch Photos albums */
        suspend fun Context.fetchPhotosAlbums(
            pageSize: Int = 36,
            pageToken: String? = null
        ): AlbumsPagination {
            val token = TokenService.tokenInfo
            if (token.isExpired) {
                refreshAccessToken(token)
            }
            Log.d(TAG, "fetching albums pageSize=$pageSize pageToken=${pageToken?.isNotEmpty()}")
            return create().albums(pageSize, pageToken)
        }

        @Throws(IOException::class)
        internal fun Context.albumPhotos(
            albumId: String,
            pageSize: Int = 21,
            pageToken: String? = null
        ): MediaItemsPagination {
            runBlocking {
                val token = TokenService.tokenInfo
                if (token.isExpired) refreshAccessToken(token)
            }

            Log.d(TAG, "fetching photos album=$albumId pageSize=$pageSize pageToken=${pageToken?.isNotEmpty()}")
            return create()
                .albumPhotos(
                    albumId = albumId,
                    pageSize = pageSize,
                    pageToken = pageToken
                ).execute()
                .body() ?: throw IOException("Response was null")
        }

        private fun authorize(chain: Interceptor.Chain) = chain.proceed(
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer ${TokenService.accessToken}")
                .build()
        )

        private fun create(): PhotosService = Retrofit.Builder()
            .baseUrl("https://photoslibrary.googleapis.com/v1/")
            .client(OkHttpClient.Builder()
                .addInterceptor(Companion::authorize)
                .addLoggingInterceptor()
                .build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()
    }
}
