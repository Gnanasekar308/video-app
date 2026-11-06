package com.videoplayer.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.videoplayer.app.vault.VaultSession

class VideoPlayerApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Lock the vault whenever the whole app goes to background.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = VaultSession.lock()
        })
    }

    // Video thumbnails via Coil's frame decoder.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this).components { add(VideoFrameDecoder.Factory()) }.build()
}
