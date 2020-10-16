package jp.co.fssoft.guchitter.utility

import android.content.Context
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.BufferedInputStream
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
         * @param context
         * @param urls
         * @param callback
         */
        public fun saveImage(context: Context, urls: Array<String>, callback: (()->Unit)? = null)
        {
            Log.d(TAG, "[START]saveImage()")
            urls.forEach {
                val runnable = Runnable {
                    Log.d(TAG, "[START]saveImage()[THREAD]")
                    val url = URL(it)
                    val con = url.openConnection() as HttpsURLConnection
                    con.addRequestProperty("Accept-Encoding", "gzip")
                    con.connect()

                    val encoding = con.getHeaderField("Content-Encoding")
                    val stream =
                        if (encoding != null) {
                            BufferedInputStream(GZIPInputStream(con.inputStream))
                        }
                        else {
                            BufferedInputStream(con.inputStream)
                        }
                    val file = it.substring(it.lastIndexOf('/') + 1)
                    context.openFileOutput(file, Context.MODE_PRIVATE).use {
                        while (true) {
                            val line = stream.read()
                            if (line == -1) {
                                break
                            }
                            it.write(line)
                        }
                    }
                    con.disconnect()
                    Log.d(TAG, "[END]saveImage()[THREAD]")
                }
                val thread = Thread(runnable)
                thread.start()
                if (callback != null) {
                    thread.join()
                }
            }
            if (callback != null) {
                callback()
            }
            Log.d(TAG, "[END]saveImage()")
        }
    }
}
