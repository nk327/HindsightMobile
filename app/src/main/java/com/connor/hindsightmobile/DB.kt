package com.connor.hindsightmobile

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.connor.hindsightmobile.obj.OCRResult
import com.connor.hindsightmobile.ui.elements.AppInfo
import com.connor.hindsightmobile.ui.viewmodels.Message

class DB private constructor(context: Context, databaseName: String = DATABASE_NAME) :
    SQLiteOpenHelper(context, databaseName, null, DATABASE_VERSION) {

     companion object {
         private const val DATABASE_NAME = "hindsight.db"
         private const val DATABASE_VERSION = 4

         private const val TABLE_FRAMES = "frames"
         const val COLUMN_ID = "id"
         const val COLUMN_TIMESTAMP = "timestamp"
         const val COLUMN_APPLICATION = "application"
         private const val COLUMN_VIDEO_CHUNK = "video_chunk"
         private const val COLUMN_VIDEO_CHUNK_OFFSET = "video_chunk_offset"

         private const val TABLE_VIDEO_CHUNKS = "video_chunks"
         private const val COLUMN_VIDEO_CHUNK_ID = "id"
         private const val COLUMN_VIDEO_CHUNK_PATH = "path"

         private const val TABLE_OCR_RESULTS = "ocr_results"
         private const val COLUMN_OCR_RESULT_ID = "id"
         private const val COLUMN_OCR_RESULT_FRAME_ID = "frame_id"
         private const val COLUMN_OCR_RESULT_TEXT = "text"
         private const val COLUMN_OCR_RESULT_X = "x"
         private const val COLUMN_OCR_RESULT_Y = "y"
         private const val COLUMN_OCR_RESULT_WIDTH = "w"
         private const val COLUMN_OCR_RESULT_HEIGHT = "h"
         private const val COLUMN_OCR_RESULT_CONFIDENCE = "confidence"
         private const val COLUMN_OCR_RESULT_BLOCK_NUM = "block_num"

         private const val TABLE_APPS = "apps"
         private const val COLUMN_APP_NAME = "name"
         private const val COLUMN_APP_PACKAGE = "package"
         private const val COLUMN_RECORD = "record"

         private const val TABLE_CONVOS = "queries"
         private const val COLUMN_CONVO_ID = "id"
         private const val COLUMN_CONVO_TIMESTAMP = "timestamp"
         private const val COLUMN_CONVO = "conversation"
         private const val COLUMN_CONVO_OFFSETS = "offsets" // format: 0|4,4|10,10|<length>
         private const val COLUMN_CONVO_USER_IDS = "user_ids"
//         private const val COLUMN_ = ""

         @Volatile private var instance: DB? = null

         fun getInstance(context: Context, databaseName: String = DATABASE_NAME): DB =
             instance ?: synchronized(this) {
                 instance ?: DB(context.applicationContext, databaseName).also { instance = it }
             }
     }

    override fun onCreate(db: SQLiteDatabase) {
        val createConvosTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_CONVOS (
            $COLUMN_CONVO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_CONVO_TIMESTAMP INTEGER NOT NULL,
            $COLUMN_CONVO TEXT,
            $COLUMN_CONVO_OFFSETS TEXT,
            $COLUMN_CONVO_USER_IDS TEXT
            )
        """.trimIndent()

        val createFramesTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_FRAMES (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_TIMESTAMP INTEGER NOT NULL,
            $COLUMN_APPLICATION TEXT,
            $COLUMN_VIDEO_CHUNK INTEGER,
            $COLUMN_VIDEO_CHUNK_OFFSET INTEGER,
            FOREIGN KEY($COLUMN_VIDEO_CHUNK) REFERENCES $TABLE_VIDEO_CHUNKS($COLUMN_VIDEO_CHUNK_ID)
            )
        """.trimIndent()

        val createVideoChunksTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_VIDEO_CHUNKS (
                $COLUMN_VIDEO_CHUNK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VIDEO_CHUNK_PATH TEXT NOT NULL
            )
        """.trimIndent()

        val createOcrResultsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_OCR_RESULTS (
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

        val createAppsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_APPS (
                $COLUMN_APP_PACKAGE TEXT NOT NULL PRIMARY KEY,
                $COLUMN_APP_NAME TEXT,
                $COLUMN_RECORD BOOLEAN NOT NULL DEFAULT TRUE
            )
        """.trimIndent()

        db.execSQL(createFramesTable)
        db.execSQL(createVideoChunksTable)
        db.execSQL(createOcrResultsTable)
        db.execSQL(createAppsTable)
        db.execSQL(createConvosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_FRAMES")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_VIDEO_CHUNKS")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_OCR_RESULTS")
//        db.execSQL("DROP TABLE IF EXISTS $TABLE_APPS")
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
                db.insert(TABLE_OCR_RESULTS, null, values)
            }
            db.setTransactionSuccessful()
            Log.d("DB", "Inserted ${results.size} OCR results for frameId $frameId")
        } catch (e: Exception) {
            Log.e("DB", "Failed to insert OCR results for frameId $frameId", e)
            return -1L
        } finally {
            db.endTransaction()
        }
        return results.size.toLong()
    }

    fun getFramesWithoutOCRResults(): List<Map<String, Any?>> {
        val db = this.readableDatabase
        val framesWithoutOCR = mutableListOf<Map<String, Any?>>()

        val query = """
            SELECT f.$COLUMN_ID, f.$COLUMN_TIMESTAMP, f.$COLUMN_APPLICATION, f.$COLUMN_VIDEO_CHUNK, f.$COLUMN_VIDEO_CHUNK_OFFSET
            FROM $TABLE_FRAMES f
            LEFT JOIN $TABLE_OCR_RESULTS o ON f.$COLUMN_ID = o.$COLUMN_OCR_RESULT_FRAME_ID
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
        INNER JOIN $TABLE_OCR_RESULTS o ON f.$COLUMN_ID = o.$COLUMN_OCR_RESULT_FRAME_ID
        WHERE o.$COLUMN_OCR_RESULT_TEXT IS NOT NULL
        AND f.$COLUMN_ID NOT IN ($ingestedFrameIdsString)
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        val framesMap = mutableMapOf<Int, Triple<Long, String, MutableList<Map<String, Any?>>>>()

        if (cursor.moveToFirst()) {
            do {
                val frameId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val application = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLICATION))
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
                    framesMap.getOrPut(frameId) { Triple(timestamp, application, mutableListOf()) }.third.add(ocrResult)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()

        framesMap.forEach { (frameId, pair) ->
            val (timestamp, application, ocrResults) = pair
            if (ocrResults.isNotEmpty()) { // Only add if there’s at least one non-empty text result
                framesWithOCRResults.add(
                    mapOf(
                        "frame_id" to frameId,
                        "timestamp" to timestamp,
                        "application" to application,
                        "ocr_results" to ocrResults
                    )
                )
            }
        }
        return framesWithOCRResults
    }

    fun getFramesWithOCRResults(frameIds: List<Int>): List<Map<String, Any?>> {
        val db = this.readableDatabase
        val framesWithOCRResults = mutableListOf<Map<String, Any?>>()

        val frameIdsString = frameIds.joinToString(",") { "'$it'" }

        val query = """
        SELECT f.$COLUMN_ID, f.$COLUMN_TIMESTAMP, f.$COLUMN_APPLICATION,
               o.$COLUMN_OCR_RESULT_TEXT, o.$COLUMN_OCR_RESULT_X, o.$COLUMN_OCR_RESULT_Y, 
               o.$COLUMN_OCR_RESULT_WIDTH, o.$COLUMN_OCR_RESULT_HEIGHT, 
               o.$COLUMN_OCR_RESULT_CONFIDENCE, o.$COLUMN_OCR_RESULT_BLOCK_NUM
        FROM $TABLE_FRAMES f
        INNER JOIN $TABLE_OCR_RESULTS o ON f.$COLUMN_ID = o.$COLUMN_OCR_RESULT_FRAME_ID
        WHERE o.$COLUMN_OCR_RESULT_TEXT IS NOT NULL
        AND f.$COLUMN_ID IN ($frameIdsString)
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        val framesMap = mutableMapOf<Int, Triple<Long, String, MutableList<Map<String, Any?>>>>()

        if (cursor.moveToFirst()) {
            do {
                val frameId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                val application = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APPLICATION))
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
                    framesMap.getOrPut(frameId) { Triple(timestamp, application, mutableListOf()) }.third.add(ocrResult)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()

        framesMap.forEach { (frameId, pair) ->
            val (timestamp, application, ocrResults) = pair
            if (ocrResults.isNotEmpty()) { // Only add if there’s at least one non-empty text result
                framesWithOCRResults.add(
                    mapOf(
                        "frame_id" to frameId,
                        "timestamp" to timestamp,
                        "application" to application,
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
        return frameId
    }

    fun updateFrameWithVideoChunk(frameId: Int, videoChunkId: Int, videoChunkOffset: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_VIDEO_CHUNK, videoChunkId)
            put(COLUMN_VIDEO_CHUNK_OFFSET, videoChunkOffset)
        }
        val rowsUpdated = db.update(TABLE_FRAMES, values, "$COLUMN_ID = ?", arrayOf(frameId.toString()))

        if (rowsUpdated == 0) {
            Log.e("DB", "Failed to update frame with video chunk")
        } else {
            Log.d("DB", "Frame $frameId updated with video chunk $videoChunkId and offset $videoChunkOffset")
        }
        return rowsUpdated
    }

    fun insertApp(appPackage: String, appName: String?, record: Boolean = true): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_APP_PACKAGE, appPackage)
            put(COLUMN_APP_NAME, appName)
            put(COLUMN_RECORD, record)
        }

        val result = db.insertWithOnConflict(TABLE_APPS, null, values, SQLiteDatabase.CONFLICT_IGNORE)

        if (result == -1L) {
            Log.e("DB", "Failed to insert app $appName")
        } else {
            Log.d("DB", "App $appName inserted successfully with id: $result")
        }
        return result
    }

    fun updateAppRecordStatus(appPackage: String, record: Boolean): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RECORD, record)
        }

        val rowsUpdated = db.update(
            TABLE_APPS, values, "$COLUMN_APP_PACKAGE = ?", arrayOf(appPackage)
        )

        if (rowsUpdated == 0) {
            Log.e("DB", "Failed to update record status for app with package $appPackage")
        } else {
            Log.d("DB", "Record status updated for app with package $appPackage")
        }
        return rowsUpdated
    }

    fun addQuery(timestamp: Long, messages: List<Message>): Long {
        val db = this.writableDatabase

        var lastOffset = 0
        val offsets = messages.map { msg ->
            val start = lastOffset
            val end = lastOffset + msg.content.length
            lastOffset = end
            """$start|$end"""
        }

        val values = ContentValues().apply {
            put(COLUMN_CONVO_TIMESTAMP, timestamp)
            put(COLUMN_CONVO, messages.map{ it.content }.joinToString(""))
            put(COLUMN_CONVO_OFFSETS, offsets.joinToString(","))
            put(COLUMN_CONVO_USER_IDS, messages.map{it.author.replace(",", "")}.joinToString { "," })
        }

        val result = db.insertWithOnConflict(TABLE_CONVOS, null, values, SQLiteDatabase.CONFLICT_IGNORE)

        if (result == -1L) {
            Log.e("DB", "Failed to add query")
        } else {
            Log.d("DB", "Query inserted successfully with id: $result")
        }
        return result
    }

    fun getQueries(): List<Message> {
        val db = this.readableDatabase
        val queries = mutableListOf<List<Message>>()

        val query = """
            SELECT a.$COLUMN_CONVO_TIMESTAMP, a.$COLUMN_CONVO, a.$COLUMN_CONVO_OFFSETS, a.$COLUMN_CONVO_USER_IDS
            FROM $TABLE_CONVOS a
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val messages = mutableListOf<Message>()
            do {
                val offsets = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONVO_OFFSETS)).split(',')
                val users = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONVO_USER_IDS)).split(',')

//                val timestamp = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CONVO_TIMESTAMP))
                val conversation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONVO))

                queries.add(
                    offsets.mapIndexed { idx, value ->
                        val (start, end) = value.split('|').map({ it.toInt() })
                        val author = users[idx]
                        Message(
                            author,
                            content = conversation.substring(start, end)
                        )
                    }
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return queries.flatten()
    }

    fun getAllApps(): List<AppInfo> {
        val db = this.readableDatabase
        val appList = mutableListOf<AppInfo>()

        val query = """
            SELECT a.$COLUMN_APP_NAME, a.$COLUMN_APP_PACKAGE, a.$COLUMN_RECORD, 
                   COUNT(f.$COLUMN_ID) AS num_frames
            FROM $TABLE_APPS a
            LEFT JOIN $TABLE_FRAMES f ON a.$COLUMN_APP_PACKAGE = f.$COLUMN_APPLICATION
            GROUP BY a.$COLUMN_APP_NAME, a.$COLUMN_APP_PACKAGE, a.$COLUMN_RECORD
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val appPackage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_PACKAGE))
                val appName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME))
                val record = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD)) == 1
                val numFrames = cursor.getInt(cursor.getColumnIndexOrThrow("num_frames"))

                appList.add(AppInfo(appPackage, appName ?: appPackage, record, numFrames))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return appList
    }

    fun getAppPackages(record: Boolean? = null): HashSet<String> {
        val db = this.readableDatabase
        val appPackages = HashSet<String>()

        val query = when (record) {
            true -> "SELECT $COLUMN_APP_PACKAGE FROM $TABLE_APPS WHERE $COLUMN_RECORD = 1"
            false -> "SELECT $COLUMN_APP_PACKAGE FROM $TABLE_APPS WHERE $COLUMN_RECORD = 0"
            else -> "SELECT $COLUMN_APP_PACKAGE FROM $TABLE_APPS" // Return all if record is null
        }

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val appPackage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_PACKAGE))
                appPackages.add(appPackage)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return appPackages
    }

    fun getAppPackageToRecordMap(): Map<String, Boolean> {
        val db = this.readableDatabase
        val appPackageToRecordMap = mutableMapOf<String, Boolean>()

        val query = "SELECT $COLUMN_APP_PACKAGE, $COLUMN_RECORD FROM $TABLE_APPS"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val appPackage = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_PACKAGE))
                val record = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECORD)) == 1
                appPackageToRecordMap[appPackage] = record
            } while (cursor.moveToNext())
        }
        cursor.close()

        return appPackageToRecordMap
    }

    fun deleteAllDBDataForApp(appPackage: String) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val frameIds = mutableListOf<Int>()
            val frameQuery = "SELECT $COLUMN_ID FROM $TABLE_FRAMES WHERE $COLUMN_APPLICATION = ?"
            val frameCursor = db.rawQuery(frameQuery, arrayOf(appPackage))
            while (frameCursor.moveToNext()) {
                frameIds.add(frameCursor.getInt(frameCursor.getColumnIndexOrThrow(COLUMN_ID)))
            }
            frameCursor.close()

            for (frameId in frameIds) {
                db.delete(TABLE_OCR_RESULTS, "$COLUMN_OCR_RESULT_FRAME_ID = ?", arrayOf(frameId.toString()))
            }

            db.delete(TABLE_FRAMES, "$COLUMN_APPLICATION = ?", arrayOf(appPackage))

            val applicationDashes = appPackage.replace(".", "-")
            db.delete(TABLE_VIDEO_CHUNKS, "$COLUMN_VIDEO_CHUNK_PATH LIKE ?", arrayOf("%$applicationDashes%"))

            db.setTransactionSuccessful()
            Log.d("DB", "Successfully deleted all DB data for app package: $appPackage")
        } catch (e: Exception) {
            Log.e("DB", "Failed to delete data for app package: $appPackage", e)
        } finally {
            db.endTransaction()
        }
    }
}