package com.xinthink.muzei.photos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import org.jetbrains.anko.frameLayout

@ExperimentalCoroutinesApi
class LauncherActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        frameLayout {
            id = R.id.nav_host_fragment
        }

        val navHost = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, navHost)
            .setPrimaryNavigationFragment(navHost) // this is the equivalent to app:defaultNavHost="true"
            .commit()
    }
}
