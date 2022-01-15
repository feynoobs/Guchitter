package jp.co.fssoft.guchitter.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TwitterApiUpdate
import jp.co.fssoft.guchitter.database.DatabaseHelper

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

        val params = mapOf(
            "status" to intent?.getStringExtra("status")!!,
            "display_coordinates" to false.toString()
        )
        TwitterApiUpdate(DatabaseHelper(applicationContext).readableDatabase).start(params).callback =  {
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

        startForeground(1, notification)

        Log.d(TAG, "[END]onStartCommand(${intent}, ${flags}, ${startId})")
        return START_STICKY
    }
}
