package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import java.net.URL
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

abstract class TwitterApiCommon(private val entryPoint: String, private val method: String)
{
    companion object
    {
        private val TAG = TwitterApiCommon::class.qualifiedName
        const val API_KEY = "Zh5yLVETVZbJECibsok2cxbRD"
        const val API_SECRET = "fTA1UkmfLgACkp7xK2FxQMSs1OYjM3RsxDTZKaRC6EUyVAvtW0"
        const val CALLBACK_URL = "twinida://"
    }
    /**
     *
     */
    protected var callback: ((String?)->Unit)? = null

    /**
     *
     */
    protected lateinit var db: SQLiteDatabase

    /**
     * TODO
     *
     * @param db
     * @param additionalHeaderParams
     * @param callback
     */
    abstract fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>? = null, callback: ((String?)->Unit)? = null)

    /**
     * TODO
     *
     * @param result
     */
    abstract fun finish(result: String)

    /**
     * TODO
     *
     * @param db
     * @param requestParams
     * @param additionalHeaderParams
     */
    protected fun startMain(db: SQLiteDatabase, requestParams: Map<String, String>? = null, additionalHeaderParams: Map<String, String>? = null)
    {
        Log.d(TAG, "[START]startMain(${db}, ${requestParams}, ${additionalHeaderParams})")
        val runnable = Runnable {
            Log.d(TAG, "[START]startMain(${db}, ${requestParams}, ${additionalHeaderParams})[THREAD]")

            val headerParams = mutableMapOf(
                "oauth_consumer_key"     to API_KEY,
                "oauth_nonce"            to System.currentTimeMillis().toString(),
                "oauth_signature_method" to "HMAC-SHA1",
                "oauth_timestamp"        to (System.currentTimeMillis() / 1000).toString(),
                "oauth_version"          to "1.0"
            )
            var signatureKey = URLEncoder.encode(API_SECRET, "UTF-8") + "&"
            additionalHeaderParams?.forEach { (k, v) ->
                if (k != "oauth_token_secret") {
                    headerParams[k] = v
                }
                else {
                    signatureKey += URLEncoder.encode(v, "UTF-8")
                }
            }
            requestParams?.let { headerParams.putAll(it) }
            db.rawQuery("SELECT * FROM t_users", null).use {
                if (it.count == 1) {
                    it.moveToFirst()
                    headerParams["oauth_token"] = it.getString(it.getColumnIndex("oauth_token"))
                    signatureKey += URLEncoder.encode(it.getString(it.getColumnIndex("oauth_token_secret")), "UTF-8")
                }
            }

            var queryParams = "";
            val sortHeaderParams = headerParams.toList().sortedBy { it.first }.toMap().toMutableMap()
            sortHeaderParams.forEach { (k, v) ->
                val value = URLEncoder.encode(v, "UTF-8")
                queryParams += "${k}=${value}&"
            }
            queryParams = queryParams.removeSuffix("&")
            queryParams = URLEncoder.encode(queryParams, "UTF-8")
            val encodeUrl = URLEncoder.encode(entryPoint, "UTF-8")
            val signatureData = "${method}&${encodeUrl}&${queryParams}"

            val key = SecretKeySpec(signatureKey.toByteArray(), "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(key)
            sortHeaderParams["oauth_signature"] = android.util.Base64.encodeToString(mac.doFinal(signatureData.toByteArray()), android.util.Base64.NO_WRAP)

            var header = ""
            sortHeaderParams.forEach { (k, v) ->
                val value = URLEncoder.encode(v, "UTF-8")
                header += "${k}=${value},"
            }
            header = header.removeSuffix(",")

            val url =
                if (requestParams == null) {
                    URL(entryPoint)
                }
                else {
                    val builder = Uri.Builder()
                    requestParams.forEach{ (k, v) ->
                        builder.appendQueryParameter(k, v)
                    }
                    URL(entryPoint + builder.toString())
                }

            val con = url.openConnection() as HttpsURLConnection
            con.requestMethod = method
            if (method == "POST") {
                con.doOutput = true
            }
            con.addRequestProperty("Authorization", "OAuth ${header}")
            con.addRequestProperty("Accept-Encoding", "gzip")
            con.connect()
            Log.d(TAG, con.responseCode.toString())

            if (con.responseCode == 200) {
                val encoding = con.getHeaderField("Content-Encoding")
                val reader =
                    if (encoding != null) {
                        GZIPInputStream(con.inputStream).bufferedReader()
                    } else {
                        con.inputStream.bufferedReader()
                    }
                val builder = StringBuilder()
                while (true) {
                    val line = reader.readLine() ?: break
                    builder.append(line)
                }
                finish(builder.toString())
            }
            con.disconnect()

            Log.d(TAG, "[END]startMain(${db}, ${requestParams}, ${additionalHeaderParams})[THREAD]")
        }
        Thread(runnable).start()

        Log.d(TAG, "[END]startMain(${db}, ${requestParams}, ${additionalHeaderParams})")
    }
}
