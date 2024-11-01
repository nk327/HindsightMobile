package com.connor.hindsightmobile.obj

import android.content.Context
import android.util.Log
import com.connor.hindsightmobile.embeddings.SentenceEmbeddingProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class QAAgent(context : Context){
    private val framesBox = ObjectBoxStore.store.boxFor(ObjectBoxFrame::class.java)
    private val sentenceEncoder = SentenceEmbeddingProvider(context)

   fun getAnswer(query: String, prompt: String, n: Int = 5, onResponse: (((QueryResults) -> Unit))) {
       CoroutineScope(Dispatchers.IO).launch {
           val queryEmbedding: FloatArray = sentenceEncoder.encodeText(query)
           val queryResults = framesBox
               .query(ObjectBoxFrame_.embedding.nearestNeighbors(queryEmbedding, 25))
               .build()
               .findWithScores()
               .map { Pair(it.score.toFloat(), it.get()) }
               .subList(0, n)

           val retrievedContextList = ArrayList<RetrievedContext>()
           queryResults.forEach { (score, frame) ->
               Log.d("ContextRetriever", "Score: $score, Frame: ${frame.frameText}")
               retrievedContextList.add(RetrievedContext(frame.frameId, frame.frameText.toString()))
           }
           onResponse(QueryResults(query, retrievedContextList))
       }
    }
}