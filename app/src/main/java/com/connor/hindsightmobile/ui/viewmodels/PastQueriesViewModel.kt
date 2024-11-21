package com.connor.hindsightmobile.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connor.hindsightmobile.DB
import com.connor.hindsightmobile.ui.elements.AppInfo
import com.connor.hindsightmobile.utils.deleteAppData
import kotlinx.coroutines.launch

class PastQueriesViewModel(val app: Application): AndroidViewModel(app) {
    private val dbHelper: DB = DB.getInstance(app)
    var queryList = mutableStateListOf<Message>()
        private set

    init {
        viewModelScope.launch {
            val queries = dbHelper.getQueries()
                .sortedByDescending { 1 }
            queryList.addAll(queries)
        }
        Log.d("PastQueriesViewModel", "Query List: $queryList")
    }

    fun getQueryList(): List<Message> {
        return queryList
    }
}