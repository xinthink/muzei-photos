/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xinthink.muzei.photos.worker

import androidx.core.net.toUri
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.io.IOException

internal interface UnsplashService {

    companion object {

        private fun createService() : UnsplashService {
            val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        var request = chain.request()
                        val url = request.url().newBuilder()
                                .addQueryParameter("client_id", "CONSUMER_KEY").build()
                        request = request.newBuilder().url(url).build()
                        chain.proceed(request)
                    }
                    .build()

            val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.com.xinthink.muzei.photos.com/")
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()

            return retrofit.create()
        }

        @Throws(IOException::class)
        internal fun popularPhotos(): List<Photo> {
            return createService().popularPhotos.execute().body()
                    ?: throw IOException("Response was null")
        }

        @Throws(IOException::class)
        internal fun randomPhotos(): List<Photo> {
            return createService().randomPhotos().execute().body()
                    ?: throw IOException("Response was null")
        }

        @Throws(IOException::class)
        internal fun photos(
            api: String = "photos/random",
            query: Map<String, @JvmSuppressWildcards Any> = mapOf(
                "featured" to true,
                "query" to "wallpaper nature landscape scenery",
                "count" to 10
            )
        ): List<Photo> = createService()
            .photos(
                api = api,
                query = query
            ).execute()
            .body() ?: throw IOException("Response was null")

        @Throws(IOException::class)
        internal fun trackDownload(photoId: String) {
            createService().trackDownload(photoId).execute()
        }
    }

//    @get:GET("photos/random?featured=true&query=wallpaper underwater nature&count=10")
    @get:GET("photos/random?featured=true&query=wallpaper nature landscape scenery&count=10")
    val popularPhotos: Call<List<Photo>>

//    "search/photos" /photos/curated
    @GET("photos/random")
    fun randomPhotos(
        @Query("query") keywords: String = "wallpaper nature landscape scenery",
        @Query("featured") featuredOnly: Boolean = true,
        @Query("count") count: Int = 10
    ): Call<List<Photo>>

    @GET("{api}")
    fun photos(
        @Path("api") api: String = "photos/random",
        @QueryMap query: Map<String, @JvmSuppressWildcards Any> = emptyMap()
    ): Call<List<Photo>>

    @GET("photos/{id}/download")
    fun trackDownload(@Path("id") photoId: String) : Call<Any>

    data class Photo(
        val id: String,
        val urls: Urls,
        val description: String?,
        val user: User,
        val links: Links
    )

    data class Urls(val full: String)

    data class Links(val html: String) {
        val webUri get() = "$html<ATTRIBUTION_QUERY_PARAMETERS>".toUri()
    }

    data class User(
            val name: String,
            val links: Links
    )
}
