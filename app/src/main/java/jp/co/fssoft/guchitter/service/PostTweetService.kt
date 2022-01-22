package jp.co.fssoft.guchitter.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.ImageObject
import jp.co.fssoft.guchitter.api.TwitterApiMediaUpload
import jp.co.fssoft.guchitter.api.TwitterApiStatusesUpdate
import jp.co.fssoft.guchitter.database.DatabaseHelper
import jp.co.fssoft.guchitter.utility.Json
import java.io.ByteArrayOutputStream

/**
 * Post tweet service
 *
 * @constructor Create empty Post tweet service
 */
class PostTweetService : Service()
{
    companion object
    {
        /**
         *
         */
        private val TAG = PostTweetService::class.qualifiedName
    }

    /**
     * On bind
     *
     * @param p0
     * @return
     */
    override fun onBind(p0: Intent?): IBinder?
    {
        Log.d(TAG, "[START]onBind(${p0})")
        return null
    }

    /**
     * On start command
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        Log.d(TAG, "[START]onStartCommand(${intent}, ${flags}, ${startId})")
        val notification = NotificationCompat.Builder(applicationContext, "channel_1").apply {
            setContentTitle("現在")
            setContentText("送信中")
            setSmallIcon(R.drawable.tweet_pen)
        }.build()

        val bodyParams = mutableMapOf(
            "status" to intent?.getStringExtra("status")!!,
            "display_coordinates" to false.toString()
        )
        val replyId = intent.getLongExtra("in_reply_to_status_id", -1L)
        if (replyId != -1L) {
            bodyParams["in_reply_to_status_id"] = replyId.toString()
        }

        val images = intent.getStringArrayListExtra("images")
        if (images?.size != 0) {
            val medias = mutableListOf<Long>()
            images?.forEach {
                val stream = contentResolver.openInputStream(Uri.parse(it))!!
                val data = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (true) {
                    val read = stream.read(buffer)
                    if (read < 0) {
                        break
                    }
                    data.write(buffer, 0, read)
                }
                val params = mapOf(
                    "media_data" to android.util.Base64.encodeToString(data.toByteArray(), android.util.Base64.NO_WRAP)
                )
                TwitterApiMediaUpload(DatabaseHelper(applicationContext).readableDatabase).start(params).callback = {
                    Log.e(TAG, it!!)
                    val postData = Json.jsonDecode(ImageObject.serializer(), it)
                    medias.add(postData.id)
                    if (medias.size == images.size) {
                        var mediaIds = ""
                        medias.forEach {
                            mediaIds += "${it.toString()},"
                        }
                        mediaIds.removeSuffix(",")
                        bodyParams["media_ids"] = mediaIds
                        postTweet(bodyParams)
                    }
                }
            }
        }
        else {
            postTweet(bodyParams)
        }

        startForeground(1, notification)
        Log.d(TAG, "[END]onStartCommand(${intent}, ${flags}, ${startId})")

        return START_STICKY
    }

    /**
     * Post tweet
     *
     * @param param
     */
    private fun postTweet(param: Map<String, String>)
    {
        TwitterApiStatusesUpdate(DatabaseHelper(applicationContext).readableDatabase).start(param).callback =  {
            Handler(Looper.getMainLooper()).post {
                if (it == null) {
                    Toast.makeText(applicationContext, getString(R.string.post_tweet_fail), Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(applicationContext, getString(R.string.post_tweet_success), Toast.LENGTH_LONG).show()
                }
            }
            stopForeground(true)
        }
    }
}
