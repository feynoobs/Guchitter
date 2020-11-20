package jp.co.fssoft.guchitter.utility

import android.content.Context
import android.graphics.*
import android.util.Log
import android.webkit.URLUtil
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

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
         * @property prefix
         */
        enum class ImagePrefix(private val prefix: String)
        {
            USER("user"),
            BANNER("banner"),
            PICTURE("picture")
        }

        /**
         * TODO
         *
         * @param context
         * @param prefix
         * @param url
         * @param sync
         * @param saveAs
         * @param callback
         */
        public fun saveImage(context: Context, prefix: ImagePrefix, url: String, sync: Boolean = true, saveAs: String? = null, callback: (()->Unit)? = null)
        {
            Log.d(TAG, "[START]saveImage(${context}, ${prefix}, ${url}, ${callback})")
            val file =
                if (saveAs == null) {
                    "${prefix}_${URLUtil.guessFileName(url, null, null)}"
                }
                else {
                    "${prefix}_${saveAs}"
                }
            val fileObject = File("${context.cacheDir}/${file}")
            if (fileObject.exists() == false) {
                val runnable = Runnable {
                    Log.d(
                        TAG,
                        "[START][THREAD]saveImage(${context}, ${prefix}, ${url}, ${callback})"
                    )
                    val con = URL(url).openConnection() as HttpsURLConnection
                    con.addRequestProperty("Accept-Encoding", "gzip")
                    con.connect()
                    val encoding = con.getHeaderField("Content-Encoding")
                    val stream =
                        if (encoding != null) {
                            GZIPInputStream(con.inputStream)
                        } else {
                            con.inputStream
                        }
                    FileOutputStream(fileObject).use {
                        while (true) {
                            val c = stream.read()
                            if (c == -1) {
                                break
                            }
                            it.write(c)
                        }
                    }
                    callback?.let { it() }
                    Log.d(TAG, "[END][THREAD]saveImage(${context}, ${prefix}, ${url}, ${callback})")
                }
                val thread = Thread(runnable)
                thread.start()
                if (sync == true) {
                    thread.join()
                }
            }
            Log.d(TAG, "[END]saveImage(${context}, ${prefix}, ${url}, ${callback})")
        }

        /**
         * TODO
         *
         * @param context
         * @param path
         * @param prefix
         * @return
         */
        public fun loadImageStream(context: Context, path: String,  prefix: ImagePrefix) : FileInputStream?
        {
            var result: FileInputStream? = null
            val fileObject = File("${context.cacheDir}/${prefix}_${URLUtil.guessFileName(path, null, null)}")
            if (fileObject.exists() == true) {
                result = FileInputStream(fileObject)
            }

            return result
        }

        /**
         * TODO
         *
         * @param source
         * @return
         */
        public fun circleTransform(source: Bitmap) : Bitmap
        {
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

            return bitmap
        }
    }
}
