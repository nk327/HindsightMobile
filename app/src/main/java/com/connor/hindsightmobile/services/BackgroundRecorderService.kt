package com.connor.hindsightmobile.services

import android.app.Activity
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.activity.result.ActivityResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.SecurityException
import com.connor.hindsightmobile.R
import com.connor.hindsightmobile.enums.RecorderState
import com.connor.hindsightmobile.obj.ImageResolution
import com.connor.hindsightmobile.obj.UserActivityState
import com.connor.hindsightmobile.utils.Preferences
import com.connor.hindsightmobile.utils.getUnprocessedScreenshotsDirectory

class BackgroundRecorderService : RecorderService() {
    override val notificationTitle: String
        get() = getString(R.string.hindsight_recording)

    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private var activityResult: ActivityResult? = null

    private var imageReader: ImageReader? = null
    private var handler: Handler? = null
    private var recordRunnable: Runnable? = null

    private var recorderLoopStopped: Boolean = false

    private var recordWhenActive: Boolean = Preferences.prefs.getBoolean(
        Preferences.recordwhenactive,
        false
    )
    private var screenshotApplication: String? = null

    override val fgServiceType: Int?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        } else {
            null
        }

    fun prepare(data: ActivityResult) {
        this.activityResult = data
        initMediaProjection()
    }

    override fun onCreate() {
        super.onCreate()
    }

    private fun initMediaProjection() {
        val mProjectionManager = getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager
        try {
            mediaProjection = mProjectionManager.getMediaProjection(
                Activity.RESULT_OK,
                activityResult?.data!!
            )
        } catch (e: Exception) {
            Log.e("Media Projection Error", e.toString())
            onDestroy()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mediaProjection!!.registerCallback(
                object : MediaProjection.Callback() {
                    override fun onStop() {
                        onDestroy()
                    }
                },
                null
            )
        }
    }

    override fun start() {
        super.start()
        isRunning = true
        val resolution = getScreenResolution()
        val density = resolution.density
        val width = resolution.width
        val height = resolution.height
        Log.d("BackgroundRecorderService", "Screen resolution: $width x $height x $density")

        mediaProjection?.let { mp ->
            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            try {
                virtualDisplay = mp.createVirtualDisplay(
                    "BackgroundRecorderService",
                    width,
                    height,
                    density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader!!.surface,
                    object : VirtualDisplay.Callback() {
                        override fun onStopped() {
                            super.onStopped()
                            // Handle the virtual display being stopped
                            onDestroy()
                        }
                    },
                    null
                )
            } catch (e: SecurityException) {
                Log.e("BackgroundRecorderService", "Failed to create VirtualDisplay", e)
                onDestroy()
            }


            handler = Handler(Looper.getMainLooper())
            recordRunnable = object : Runnable {
                override fun run() {
                    if (!screenOn) {
                        Log.d("BackgroundRecorderService", "Screen is off, skipping screenshot")
                        postRecorderLoop(this)
                        return
                    }
                    if (recordWhenActive && !UserActivityState.userActive) {
                        Log.d(
                            "BackgroundRecorderService",
                            "Skipping Screenshot as User has been inactive"
                        )
                        postRecorderLoop(this)
                        return
                    }
                    val image = imageReader!!.acquireLatestImage()
                    screenshotApplication = UserActivityState.currentApplication
                    Log.d("BackgroundRecorderService", "Image Acquired")
                    image?.let {
                        val buffer = it.planes[0].buffer
                        val pixelStride = it.planes[0].pixelStride
                        val rowStride = it.planes[0].rowStride

                        val offset = (rowStride - pixelStride * width) / pixelStride
                        val w = width + offset
                        val bitmap = Bitmap.createBitmap(w, height, Bitmap.Config.ARGB_8888)
                        bitmap.copyPixelsFromBuffer(buffer)

                        saveImageData(bitmap, this@BackgroundRecorderService, screenshotApplication)
                        it.close()
                    }
                    // Schedule the next capture
                    UserActivityState.userActive = false
                    postRecorderLoop(this)
                }
            }
            // Initial delay before starting the recurring task
            handler?.postDelayed(recordRunnable!!, 2000) // Start after a delay of 2 seconds
        }
    }

    private fun postRecorderLoop(runnable: Runnable) {
        if (recorderState == RecorderState.ACTIVE) {
            recorderLoopStopped = false
            handler?.postDelayed(runnable, 2000)
        } else {
            recorderLoopStopped = true
        }
    }

    private fun saveImageData(bitmap: Bitmap, context: Context, imageApplication: String?) {
        // Use the app's private storage directory
        val directory = getUnprocessedScreenshotsDirectory(context)

        val file = File(directory, "${imageApplication}_${System.currentTimeMillis()}.webp")
        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fos)
                Log.d("BackgroundRecorderService", "Image saved to ${file.absolutePath}")
            }
        } catch (e: IOException) {
            Log.e("BackgroundRecorderService", "Failed to save image", e)
        }
    }

    private fun getScreenResolution(): ImageResolution {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        // TODO Use the window API instead on newer devices
        val metrics = DisplayMetrics()
        display.getRealMetrics(metrics)

        return ImageResolution(
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi
        )
    }

    override fun onDestroy() {
        Log.d("BackgroundRecorderService", "Destroying Screen Recording Service")
        // sendBroadcast(Intent(SCREEN_RECORDER_STOPPED))
        isRunning = false
        handler?.removeCallbacks(recordRunnable!!) // Stop the recurring record capture
        imageReader?.close()
        virtualDisplay?.release()
        mediaProjection?.stop() // This will trigger the callback's onStop method
        super.onDestroy()
    }

    override fun resume() {
        super.resume()
        // recorderLoopStopped ensures that the previous image capture is stopped
        if (recorderState == RecorderState.ACTIVE && recorderLoopStopped) {
            handler?.postDelayed(recordRunnable!!, 2000)
        }
    }

    companion object {
        var isRunning = false
        const val SCREEN_RECORDER_STOPPED = "com.connor.hindsight.SCREEN_RECORDER_STOPPED"
    }
}
