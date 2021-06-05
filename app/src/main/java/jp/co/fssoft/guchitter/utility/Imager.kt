package jp.co.fssoft.guchitter.utility

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**
 * TODO
 *
 */
class Imager
{
    companion object
    {
        /**
         *
         */
        private val TAG = Imager::class.qualifiedName

        /**
         *
         */
        private const val MAX = 4

        /**
         *
         */
        private val httpsRequest = mutableListOf<String>()

        /**
         *
         */
        private val keepRequest = mutableListOf<Thread>()

        /**
         *
         */
        private val loadRequest = mutableMapOf<String, MutableList<(File)->Unit>>()

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
    }


    /**
     * TODO
     *
     * @param context
     * @param prefix
     * @param url
     * @param saveAs
     */
    public fun saveImage(context: Context, prefix: ImagePrefix, url: String, saveAs: String? = null)
    {
        Log.d(TAG, "[START]saveImage(${context}, ${prefix}, ${url})")
        val file =
            if (saveAs == null) {
                "${prefix}_${URLUtil.guessFileName(url, null, null)}"
            }
            else {
                "${prefix}_${saveAs}"
            }
        val fileObject = File("${context.cacheDir}/${file}")
        var runnable: Runnable? = null
        if (fileObject.exists() == false) {
            synchronized(httpsRequest) {
                if (httpsRequest.find { it == file} == null) {
                    httpsRequest.add(file)
                    runnable = Runnable {
                        Log.d(TAG, "[START][THREAD]saveImage(${context}, ${prefix}, ${url})")
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
                        /***********************************************
                         * 中途半端なImageが読み出されないようにtmpに保存して
                         * 完了したらリネームする
                         */
                        val tmpFileObject = File("${context.cacheDir}/tmp_${file}")
                        FileOutputStream(tmpFileObject).use {
                            while (true) {
                                val c = stream.read()
                                if (c == -1) {
                                    break
                                }
                                it.write(c)
                            }
                        }
                        tmpFileObject.renameTo(fileObject)

                        synchronized(keepRequest) {
                            if (keepRequest.size > 0) {
                                keepRequest.removeAt(0).start()
                            }
                        }

                        synchronized(loadRequest) {
                            loadRequest.forEach { (k, v) ->
                                if (k == file) {
                                    v.forEach {
                                        it(fileObject)
                                    }
                                }
                            }
                            loadRequest.remove(file)
                        }
                        synchronized(httpsRequest) {
                            httpsRequest.remove(file)
                        }
                    }
                    Log.d(TAG, "[END][THREAD]saveImage(${context}, ${prefix}, ${url})")
                }
            }
        }
        val thread = Thread(runnable)
        if (keepRequest.size <= MAX) {
            thread.start()
        }
        else {
            synchronized(keepRequest) {
                keepRequest.add(thread)
            }
        }
        Log.d(TAG, "[END]saveImage(${context}, ${prefix}, ${url})")
    }

    /**
     * TODO
     *
     * @param context
     * @param path
     * @param prefix
     * @param callback
     */
    public fun loadImage(context: Context, path: String, prefix: ImagePrefix, callback: (File)->Unit)
    {
        Log.d(TAG, "[START]loadImage(${context}, ${path}, ${prefix}, ${callback})")

        val file = "${context.cacheDir}/${prefix}_${URLUtil.guessFileName(path, null, null)}"
        val fileObject = File("${context.cacheDir}/${prefix}_${URLUtil.guessFileName(path, null, null)}")
        if (fileObject.exists() == true) {
            callback(fileObject)
        }
        else {
            val runnable = Runnable {
                synchronized (loadRequest) {
                    loadRequest[file]?.add(callback)
                }
            }
            Thread(runnable).start()
        }

        Log.d(TAG, "[END]loadImage(${context}, ${path}, ${prefix}, ${callback})")
    }
}
