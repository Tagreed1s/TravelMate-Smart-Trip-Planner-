package com.travelmate

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.travelmate.data.firebase.FirebasePaths
import com.travelmate.di.AppContainer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class TravelMateApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Must run before any other database call: keeps the catalog and the user's trips
        // readable offline after the first sync, and queues offline writes.
        Firebase.database.setPersistenceEnabled(true)
        container = AppContainer()
        keepCatalogSyncedWhileSignedIn()
    }

    /**
     * The rules only let signed-in users read /catalog, and Firebase does not retry a listen that was
     * refused. So the catalog is kept synced only while someone is signed in, and only after a first
     * read succeeded (right after sign-in the connection may not be authenticated yet).
     */
    private fun keepCatalogSyncedWhileSignedIn() {
        val catalog = Firebase.database.getReference(FirebasePaths.CATALOG)
        container.appScope.launch {
            container.authRepository.currentUser
                .map { it != null }
                .distinctUntilChanged()
                .collectLatest { signedIn ->
                    if (signedIn) {
                        runCatching { container.catalogRepository.destinations().first() }
                            .onSuccess { catalog.keepSynced(true) }
                    } else {
                        catalog.keepSynced(false)
                    }
                }
        }
    }

    /**
     * Image loader used by every RemoteImage. Wikimedia (the source of the catalog photos) answers
     * 403 Forbidden to the default "okhttp" User-Agent, so the app identifies itself, as their policy asks.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder().header("User-Agent", USER_AGENT).build())
                }
                .build()
        }
        .crossfade(true)
        .build()

    private companion object {
        const val USER_AGENT = "TravelMate/1.0 (Android; CSC 402 student project)"
    }
}
