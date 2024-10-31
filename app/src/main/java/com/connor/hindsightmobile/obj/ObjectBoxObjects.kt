package com.connor.hindsightmobile.obj

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id

@Entity
data class ObjectBoxFrame(
    @Id var id: Long = 0,
    var frameId: Int = 0,
    var timestamp: Long = 0,
    var frameText: String? = null,
    @HnswIndex(dimensions = 384) var frameEmbedding: FloatArray = floatArrayOf()
)