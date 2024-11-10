package com.connor.hindsightmobile.utils

private fun isNonMeaningfulText(text: String): Boolean {
    val nonMeaningfulWords = setOf(
        "OK", "Cancel", "Yes", "No", "Back", "Home", "Settings",
        "AM", "PM", "Battery", "Search", "Send", "Recent", "More"
    )

    val nonMeaningfulPatterns = listOf(
        Regex("^\\d{1,2}:\\d{2}\$"),             // Time like 12:34
        Regex("^\\d{1,2}:\\d{2}\\s?(AM|PM)\$"),  // Time like 12:34 PM
        Regex("^\\d{1,3}%\$"),                   // Battery percentage like 100%
        Regex("^\\d{1,2}/\\d{1,2}/\\d{2,4}\$"),  // Date like 12/31/2023
        Regex("^\\s*$")                          // Empty or whitespace-only strings
    )

    return text in nonMeaningfulWords || nonMeaningfulPatterns.any { it.matches(text) }
}


fun processOCRResults(ocrResults: List<Map<String, Any?>>): String {
    // Filter out non-meaningful text
    val meaningfulTexts = ocrResults.filter { ocrResult ->
        val text = (ocrResult["text"] as? String)?.trim() ?: ""
        text.isNotEmpty() && !isNonMeaningfulText(text)
    }

    // Group texts by block_num
    val textsByBlockNum = meaningfulTexts.groupBy { it["block_num"] as Int }

    // Sort the blocks by block_num to maintain order
    val sortedBlocks = textsByBlockNum.entries.sortedBy { entry ->
        entry.value.minOf { it["y"] as Int }
    }

    val paragraphs = sortedBlocks.map { (_, blockTexts) ->
        val sortedBlockTexts = blockTexts.sortedWith(
            compareBy({ it["y"] as Int }, { it["x"] as Int })
        )
        sortedBlockTexts.joinToString(separator = " ") { it["text"] as String }
    }

    // Combine paragraphs into a single string
    return paragraphs.joinToString(separator = "\n\n")
}
