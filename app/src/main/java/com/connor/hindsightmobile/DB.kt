package com.connor.hindsightmobile

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.connor.hindsightmobile.obj.OCRResult

class DB(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
     companion object {
         private const val DATABASE_NAME = "hindsight.db"
         private const val DATABASE_VERSION = 1

         private const val TABLE_FRAMES = "frames"
         const val COLUMN_ID = "id"
         const val COLUMN_TIMESTAMP = "timestamp"
         const val COLUMN_APPLICATION = "application"
         private const val COLUMN_VIDEO_CHUNK = "video_chunk"
         private const val COLUMN_VIDEO_CHUNK_OFFSET = "video_chunk_offset"

         private const val TABLE_VIDEO_CHUNKS = "video_chunks"
         private const val COLUMN_VIDEO_CHUNK_ID = "id"
         private const val COLUMN_VIDEO_CHUNK_PATH = "path"

         private const val OCR_RESULTS = "ocr_results"
         private const val COLUMN_OCR_RESULT_ID = "id"
         private const val COLUMN_OCR_RESULT_FRAME_ID = "frame_id"
         private const val COLUMN_OCR_RESULT_TEXT = "text"
         private const val COLUMN_OCR_RESULT_X = "x"
         private const val COLUMN_OCR_RESULT_Y = "y"
         private const val COLUMN_OCR_RESULT_WIDTH = "w"
         private const val COLUMN_OCR_RESULT_HEIGHT = "h"
         private const val COLUMN_OCR_RESULT_CONFIDENCE = "confidence"
         private const val COLUMN_OCR_RESULT_BLOCK_NUM = "block_num"

     }

    override fun onCreate(db: SQLiteDatabase) {
        val createFramesTable = """
        CREATE TABLE $TABLE_FRAMES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TIMESTAMP INTEGER NOT NULL,
            $COLUMN_APPLICATION TEXT,
            $COLUMN_VIDEO_CHUNK INTEGER,
            $COLUMN_VIDEO_CHUNK_OFFSET INTEGER,
            FOREIGN KEY($COLUMN_VIDEO_CHUNK) REFERENCES $TABLE_VIDEO_CHUNKS($COLUMN_VIDEO_CHUNK_ID)
            )
        """.trimIndent()

        val createVideoChunksTable = """
            CREATE TABLE $TABLE_VIDEO_CHUNKS (
                $COLUMN_VIDEO_CHUNK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VIDEO_CHUNK_PATH TEXT NOT NULL
            )
        """.trimIndent()

        val createOcrResultsTable = """
            CREATE TABLE $OCR_RESULTS (
                $COLUMN_OCR_RESULT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_OCR_RESULT_FRAME_ID INTEGER NOT NULL,
                $COLUMN_OCR_RESULT_TEXT TEXT,
                $COLUMN_OCR_RESULT_X INTEGER,
                $COLUMN_OCR_RESULT_Y INTEGER,
                $COLUMN_OCR_RESULT_WIDTH INTEGER,
                $COLUMN_OCR_RESULT_HEIGHT INTEGER,
                $COLUMN_OCR_RESULT_CONFIDENCE REAL,
                $COLUMN_OCR_RESULT_BLOCK_NUM INTEGER
            )
        """.trimIndent()

        db.execSQL(createFramesTable)
        db.execSQL(createVideoChunksTable)
        db.execSQL(createOcrResultsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }

    fun insertFrame(timestamp: Long, application: String?, videoChunkId: Int?, videoChunkOffset: Int?): Long {
        if (frameExists(timestamp, application)) {
            // Log.d("DB", "Frame with timestamp: $timestamp and application: $application already exists. Skipping insertion.")
            return -1L // Return -1 to indicate no new insertion
        }
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_APPLICATION, application)
            put(COLUMN_VIDEO_CHUNK, videoChunkId)
            put(COLUMN_VIDEO_CHUNK_OFFSET, videoChunkOffset)
        }

        val result = db.insert(TABLE_FRAMES, null, values)
        db.close()

        if (result == -1L) {
            Log.e("DB", "Failed to insert frame")
        } else {
            Log.d("DB", "Frame inserted successfully with id: $result")
        }

        return result
    }

    fun frameExists(timestamp: Long, application: String?): Boolean {
        val db = this.readableDatabase
        val query = "SELECT 1 FROM $TABLE_FRAMES WHERE $COLUMN_TIMESTAMP = ? AND $COLUMN_APPLICATION = ?"
        val cursor = db.rawQuery(query, arrayOf(timestamp.toString(), application))

        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()

        return exists
    }

    fun insertOCRResults(frameId: Int, results: List<OCRResult>): Long {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val effectiveResults = if (results.isEmpty()) {
                listOf(OCRResult(text = "", x = 0, y = 0, width = 0, height = 0, confidence = 0f, blockNum = 0))
            } else {
                results
            }
            for (result in effectiveResults) {
                val values = ContentValues().apply {
                    put(COLUMN_OCR_RESULT_FRAME_ID, frameId)
                    put(COLUMN_OCR_RESULT_TEXT, result.text)
                    put(COLUMN_OCR_RESULT_X, result.x)
                    put(COLUMN_OCR_RESULT_Y, result.y)
                    put(COLUMN_OCR_RESULT_WIDTH, result.width)
                    put(COLUMN_OCR_RESULT_HEIGHT, result.height)
                    put(COLUMN_OCR_RESULT_CONFIDENCE, result.confidence)
                    put(COLUMN_OCR_RESULT_BLOCK_NUM, result.blockNum)
                }
                db.insert(OCR_RESULTS, null, values)
            }
            db.setTransactionSuccessful()
            Log.d("DB", "Inserted ${results.size} OCR results for frameId $frameId")
        } catch (e: Exception) {
            Log.e("DB", "Failed to insert OCR results for frameId $frameId", e)
            return -1L
        } finally {
            db.endTransaction()
            db.close()
        }
        return results.size.toLong()
    }

    fun getFramesWithoutOCRResults(): List<Map<String, Any?>> {
        val db = this.readableDatabase
        val framesWithoutOCR = mutableListOf<Map<String, Any?>>()

        val query = """
            SELECT f.$COLUMN_ID, f.$COLUMN_TIMESTAMP, f.$COLUMN_APPLICATION, f.$COLUMN_VIDEO_CHUNK, f.$COLUMN_VIDEO_CHUNK_OFFSET
            FROM $TABLE_FRAMES f
            LEFT JOIN $OCR_RESULTS o ON f.$COLUMN_ID = o.$COLUMN_OCR_RESULT_FRAME_ID
            WHERE o.$COLUMN_OCR_RESULT_FRAME_ID IS NULL
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val frame = mapOf(
                    COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    COLUMN_TIMESTAMP to cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    COLUMN_APPLICATION to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLICATION)),
                    COLUMN_VIDEO_CHUNK to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_CHUNK)),
                    COLUMN_VIDEO_CHUNK_OFFSET to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_CHUNK_OFFSET))
                )
                framesWithoutOCR.add(frame)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return framesWithoutOCR
    }

    fun getFramesWithOCRResultsNotIn(ingestedFrameIds: List<Int>): List<Map<String, Any?>> {
        val db = this.readableDatabase
        val framesWithOCRResults = mutableListOf<Map<String, Any?>>()

        val ingestedFrameIdsString = ingestedFrameIds.joinToString(",") { "'$it'" }

        val query = """
        SELECT f.$COLUMN_ID, f.$COLUMN_TIMESTAMP, f.$COLUMN_APPLICATION,
               o.$COLUMN_OCR_RESULT_TEXT, o.$COLUMN_OCR_RESULT_X, o.$COLUMN_OCR_RESULT_Y, 
               o.$COLUMN_OCR_RESULT_WIDTH, o.$COLUMN_OCR_RESULT_HEIGHT, 
               o.$COLUMN_OCR_RESULT_CONFIDENCE, o.$COLUMN_OCR_RESULT_BLOCK_NUM
        FROM $TABLE_FRAMES f
        INNER JOIN $OCR_RESULTS o ON f.$COLUMN_ID = o.$COLUMN_OCR_RESULT_FRAME_ID
        WHERE o.$COLUMN_OCR_RESULT_TEXT IS NOT NULL
        AND f.$COLUMN_ID NOT IN ($ingestedFrameIdsString)
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        val framesMap = mutableMapOf<Int, Pair<Long, MutableList<Map<String, Any?>>>>()

        if (cursor.moveToFirst()) {
            do {
                val frameId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val ocrText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_TEXT)) ?: ""

                val ocrResult = mapOf(
                    "text" to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_TEXT)),
                    "x" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_X)),
                    "y" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_Y)),
                    "width" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_WIDTH)),
                    "height" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_HEIGHT)),
                    "confidence" to cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_CONFIDENCE)),
                    "block_num" to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OCR_RESULT_BLOCK_NUM))
                )
                if (ocrText.isNotEmpty()) {
                    framesMap.getOrPut(frameId) { timestamp to mutableListOf() }.second.add(ocrResult)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        framesMap.forEach { (frameId, pair) ->
            val (timestamp, ocrResults) = pair
            if (ocrResults.isNotEmpty()) { // Only add if there’s at least one non-empty text result
                framesWithOCRResults.add(
                    mapOf(
                        "frame_id" to frameId,
                        "timestamp" to timestamp,
                        "ocr_results" to ocrResults
                    )
                )
            }
        }
        return framesWithOCRResults
    }

    fun insertVideoChunk(videoPath: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_VIDEO_CHUNK_PATH, videoPath)
        }
        val chunkId = db.insert(TABLE_VIDEO_CHUNKS, null, values)
        db.close()

        if (chunkId == -1L) {
            Log.e("DB", "Failed to insert video chunk")
        } else {
            Log.d("DB", "Video chunk inserted successfully with id: $chunkId")
        }
        return chunkId
    }

    fun getFrameIdByTimestampAndApp(timestamp: Long?, application: String?): Int? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_ID FROM $TABLE_FRAMES WHERE $COLUMN_TIMESTAMP = ? AND $COLUMN_APPLICATION = ?"
        val cursor = db.rawQuery(query, arrayOf(timestamp.toString(), application))

        val frameId = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)) else null
        cursor.close()
        db.close()
        return frameId
    }

    fun updateFrameWithVideoChunk(frameId: Int, videoChunkId: Int, videoChunkOffset: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_VIDEO_CHUNK, videoChunkId)
            put(COLUMN_VIDEO_CHUNK_OFFSET, videoChunkOffset)
        }
        val rowsUpdated = db.update(TABLE_FRAMES, values, "$COLUMN_ID = ?", arrayOf(frameId.toString()))
        db.close()

        if (rowsUpdated == 0) {
            Log.e("DB", "Failed to update frame with video chunk")
        } else {
            Log.d("DB", "Frame $frameId updated with video chunk $videoChunkId and offset $videoChunkOffset")
        }
        return rowsUpdated
    }
}