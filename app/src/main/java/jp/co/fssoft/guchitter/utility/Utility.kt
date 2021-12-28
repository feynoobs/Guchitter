package jp.co.fssoft.guchitter.utility

import android.graphics.*
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * TODO
 *
 */
class Utility
{
    companion object
    {
        /**
         *
         */
        private val TAG = Utility::class.qualifiedName

        /**
         * TODO
         *
         * @param query
         * @return
         */
        public fun splitQuery(query: String): Map<String, String>
        {
            Log.d(TAG, "[START]splitQuery(${query})")
            val results = query.split("&")
            val resultMap = mutableMapOf<String, String>()
            results.forEach {
                val splitResult = it.split("=")
                resultMap[splitResult[0]] = splitResult[1]
            }

            Log.d(TAG, "[END]splitQuery(${query})")
            return resultMap
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonEncode(serializer: SerializationStrategy<T>, values: T): String
        {
            return Json.stringify(serializer, values)
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonListEncode(serializer: KSerializer<List<T>>, values: List<T>): String
        {
            return Json.stringify(serializer, values)
        }


        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param json
         * @return
         */
        public fun <T> jsonDecode(serializer: KSerializer<T>, json: String): T
        {
            Log.d(TAG, "[START]jsonDecode(${serializer}, ${json})")

            return Json(
                JsonConfiguration.Stable.copy(
                    ignoreUnknownKeys                   = true,
                    isLenient                           = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism                = true
                )
            ).parse(serializer, json)
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param json
         * @return
         */
        public fun <T> jsonListDecode(serializer: KSerializer<List<T>>, json: String): List<T>
        {
            Log.d(TAG, "[START]jsonDecode(${serializer}, ${json})")

            return Json(
                JsonConfiguration.Stable.copy(
                    ignoreUnknownKeys = true,
                    isLenient = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism = true
                )
            ).parse(serializer, json)
        }

        public fun createFuzzyDateTime(dateTime: String) : String
        {
            val dt = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy"))
            val unixPost = dt.atZone(ZoneOffset.ofHours(+0)).toEpochSecond()
            val unixNow = System.currentTimeMillis() / 1000
            val unixDiff = unixNow - unixPost

            val fuzzyDateTime =
                when {
                    unixDiff < 60 -> {
                        "${unixDiff}秒"
                    }
                    unixDiff < 3600 -> {
                        val minute = unixDiff / 60
                        "${minute}分"
                    }
                    unixDiff < 86400 -> {
                        val hour = unixDiff / (60 * 60)
                        "${hour}時間"
                    }
                    unixDiff < 604800 -> {
                        val day = unixDiff / (60 * 60 * 24)
                        "${day}日"
                    }
                    unixDiff < 31536000 -> {
                        dt.format(DateTimeFormatter.ofPattern("MM月dd日"))
                    }
                    else -> {
                        dt.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
                    }
                }

            return fuzzyDateTime
        }


        /**
         * TODO
         *
         * @return
         */
        public fun now(): String
        {
            Log.d(TAG, "[START]now()")
            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            Log.d(TAG, "[END]now()")

            return format.format(date)
        }

        /**
         * TODO
         *
         * @param source
         * @return
         */
        public fun circleTransform(source: Bitmap) : Bitmap
        {
            Log.d(TAG, "[START]circleTransform(${source})")

            val size = Math.min(source.width, source.height)
            val x = (source.width - size) / 2
            val y = (source.height - size) / 2
            val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)
            val paint = Paint()
            val shader = BitmapShader(squaredBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            paint.shader = shader
            paint.isAntiAlias = true
            val bitmap = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(bitmap);
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

            if (source !== squaredBitmap) {
                source.recycle()
            }
            squaredBitmap.recycle()
            Log.d(TAG, "[END]circleTransform(${source})")

            return bitmap
        }
    }
}
