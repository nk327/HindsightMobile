package com.connor.hindsightmobile.ui.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.database.Cursor
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.connor.hindsightmobile.models.ModelInfo
import com.connor.hindsightmobile.obj.QAAgent
import com.connor.hindsightmobile.obj.RetrievedContext
import org.koin.android.annotation.KoinViewModel
import java.util.TreeMap

@KoinViewModel
class ChatViewModel(val app: Application, val qaUseCase: QAAgent) : ViewModel() {

    val questionState = mutableStateOf("")
    val responseState = mutableStateOf("")
    val isGeneratingResponseState = mutableStateOf(false)
    val retrievedContextListState = mutableStateOf(emptyList<RetrievedContext>())

    private val _modelLoadingProgress = MutableLiveData(0f)
    private val _loadedModel = MutableLiveData<ModelInfo?>(null)

    private var downloadModels = TreeMap<Long, ModelInfo>()
    private val downloadManager: DownloadManager by lazy {
        app.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    }

    fun downloadModel(model: ModelInfo) {
        val filename = model.remoteUri?.lastPathSegment ?: return
        val request = DownloadManager.Request(model.remoteUri)
        request.setTitle(filename)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        val downloadManager = app.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            filename
        )
        val downloadId = downloadManager.enqueue(request)
        downloadModels[downloadId] = model
        handler.post(runnable)
    }

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            downloadModels.lastKey()?.let {
                checkDownloadStatus(it, downloadModels[it]!!)
                handler.postDelayed(this, 100)
            }
        }
    }

    private fun checkDownloadStatus(downloadId: Long, model: ModelInfo) {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val status = cursor.getInt(statusIndex)
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    _loadedModel.postValue(model.copy(description = "Download completed"))
                    _modelLoadingProgress.postValue(1f)
                }
                DownloadManager.STATUS_FAILED -> {
                    val failureMessage = getFailureMessage(cursor)
                    _loadedModel.postValue(model.copy(description = failureMessage))
                    _modelLoadingProgress.postValue(0f)
                }
                DownloadManager.STATUS_PAUSED -> {
                    _loadedModel.postValue(model.copy(description = "Download paused"))
                }
                DownloadManager.STATUS_PENDING -> {
                    _loadedModel.postValue(model.copy(description = "Download pending..."))
                    _modelLoadingProgress.postValue(0f)
                }
                DownloadManager.STATUS_RUNNING -> {
                    val progress = getDownloadProgress(cursor)
                    _loadedModel.postValue(
                        model.copy(
                            description = "Downloading ${(progress * 100L).toInt()}%"
                        )
                    )
                    _modelLoadingProgress.postValue(progress)
                }
            }
        }
        cursor.close()
    }

    private fun getDownloadProgress(cursor: Cursor): Float {
        val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        if (bytesDownloadedIndex == -1 || bytesTotalIndex == -1) {
            return 0f
        }
        val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
        val bytesTotal = cursor.getLong(bytesTotalIndex)
        return if (bytesTotal > 0) {
            bytesDownloaded.toFloat() / bytesTotal
        } else {
            0f
        }
    }

    private fun getFailureMessage(cursor: Cursor): String {
        val reasonColumn = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(reasonColumn)
        return when (reason) {
            DownloadManager.ERROR_UNKNOWN -> "Unknown error occurred"
            DownloadManager.ERROR_FILE_ERROR -> "Storage issue"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "Unhandled HTTP code"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient storage space"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "External device not found"
            DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume download"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
            else -> "Unknown error occurred"
        }
    }
}