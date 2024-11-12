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

    suspend fun getContext(query: String, n: Int = 5, n_seconds: Int = 120): QueryResults = suspendCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            val queryEmbedding: FloatArray = sentenceEncoder.encodeText(query)
            val allQueryResults = framesBox
                .query(ObjectBoxFrame_.embedding.nearestNeighbors(queryEmbedding, 200)
                    .and(ObjectBoxFrame_.application.notEqual("com.connor.hindsightmobile"))
                    .and(ObjectBoxFrame_.application.notEqual("com.google.android.inputmethod.latin"))
                )
                .build()
                .findWithScores()

            if (allQueryResults.isEmpty()) {
                continuation.resume(QueryResults("", emptyList()))
                return@launch
            }

            // Map scores to frames and sort by timestamp in descending order (most recent first)
            val sortedResults = allQueryResults
                .map { Pair(it.score.toFloat(), it.get()) }
                .sortedByDescending { it.second.timestamp }

            // Filter to keep only the first (most recent) frame in each n_seconds time group
            val filteredResults = mutableListOf<Pair<Float, ObjectBoxFrame>>()
            var lastTimestamp: Long? = null

            for ((score, frame) in sortedResults) {
                if (lastTimestamp == null || lastTimestamp!! - frame.timestamp > n_seconds * 1000) {
                    filteredResults.add(Pair(score, frame))
                    lastTimestamp = frame.timestamp
                }
            }

            // Sort by score in descending order and take the top n (Note score is actually distance so we want smal)
            val topResultsByScore = filteredResults
                .sortedBy { it.first }
                .take(n)

            // Sort the top n results by timestamp in ascending order
            val finalResults = topResultsByScore.sortedBy { it.second.timestamp }

            val retrievedContextList = ArrayList<RetrievedContext>()
            var contextString = ""
            finalResults.forEach { (score, frame) ->
                retrievedContextList.add(RetrievedContext(frame.frameId, frame.frameText.toString()))
                val localTime = convertToLocalTime(frame.timestamp)
                contextString += "Text from Screenshot of ${frame.application} at ${localTime} Distance ${score}\n"
                contextString += frame.frameText.toString() + "\n\n"
            }

            continuation.resume(QueryResults(contextString, retrievedContextList))
        }
    }
}