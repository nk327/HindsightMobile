package com.connor.hindsightmobile.obj

data class RetrievedContext(val frameId: Int, val context: String)

data class QueryResults(val response: String, val contextList: List<RetrievedContext>)