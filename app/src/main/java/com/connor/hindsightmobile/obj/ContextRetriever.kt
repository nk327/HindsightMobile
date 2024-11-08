package com.connor.hindsightmobile.obj

import android.content.Context
import com.connor.hindsightmobile.embeddings.SentenceEmbeddingProvider
import com.connor.hindsightmobile.utils.convertToLocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ContextRetriever(context : Context){
    private val framesBox = ObjectBoxStore.store.boxFor(ObjectBoxFrame::class.java)
    private val sentenceEncoder = SentenceEmbeddingProvider(context)

    suspend fun getContext(query: String, n: Int = 5): QueryResults = suspendCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            val queryEmbedding: FloatArray = sentenceEncoder.encodeText(query)
            val allQueryResults = framesBox
                .query(ObjectBoxFrame_.embedding.nearestNeighbors(queryEmbedding, 25)
                    .and(ObjectBoxFrame_.application.notEqual("com.connor.hindsightmobile"))
                )
                .build()
                .findWithScores()

            if (allQueryResults.isEmpty()) {
                continuation.resume(QueryResults("", emptyList()))
                return@launch
            }

            val queryResults = allQueryResults
                .map { Pair(it.score.toFloat(), it.get()) }
                .sortedBy { it.second.timestamp }
                .take(n)

            val retrievedContextList = ArrayList<RetrievedContext>()
            var contextString = ""
            queryResults.forEach { (score, frame) ->
                retrievedContextList.add(RetrievedContext(frame.frameId, frame.frameText.toString()))
                val localTime = convertToLocalTime(frame.timestamp)
                contextString += "Text from Screenshot of ${frame.application} at ${localTime}\n"
                contextString += frame.frameText.toString() + "\n\n"
            }

            continuation.resume(QueryResults(contextString, retrievedContextList))
        }
    }
}