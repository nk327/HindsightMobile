package com.connor.hindsightmobile.obj

data class OCRResult(
    val text: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val confidence: Float,
    val blockNum: Int
)
