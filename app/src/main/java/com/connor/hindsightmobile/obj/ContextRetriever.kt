package com.connor.hindsightmobile.obj

import android.content.Context
import com.connor.hindsightmobile.embeddings.SentenceEmbeddingProvider
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
            val queryResults = framesBox
                .query(ObjectBoxFrame_.embedding.nearestNeighbors(queryEmbedding, 25))
                .build()
                .findWithScores()
                .map { Pair(it.score.toFloat(), it.get()) }
                .subList(0, n)

            val retrievedContextList = ArrayList<RetrievedContext>()
            var contextString = ""
            queryResults.forEach { (score, frame) ->
                retrievedContextList.add(RetrievedContext(frame.frameId, frame.frameText.toString()))
                contextString += frame.frameText.toString()
            }

            continuation.resume(QueryResults(contextString, retrievedContextList))
        }
    }
}