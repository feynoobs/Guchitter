package jp.co.fssoft.guchitter.activity

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.api.TwitterApiStatusesUserTimeline
import jp.co.fssoft.guchitter.database.DatabaseHelper

open class RootActivity : AppCompatActivity()
{
    companion object
    {
        private val TAG = RootActivity::class.qualifiedName
    }

    protected val database by lazy {
        DatabaseHelper(applicationContext)
    }


    /**
     * TODO
     *
     * @param db
     * @param userId
     */
    private fun getAllTweetCommon(db: SQLiteDatabase, userId: Long)
    {
        Log.d(TAG, "[START]getAllTweetCommon(${db}, ${userId})")
        var tweetMaxId = 0L
        db.rawQuery("SELECT tweet_id FROM t_timelines ORDER BY tweet_id ASC LIMIT 1", null)
            .use {
                if (it.count == 1) {
                    it.moveToFirst()
                    tweetMaxId = it.getLong(it.getColumnIndex("tweet_id")) - 1
                }
            }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "user_id" to userId.toString(),
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMaxId != 0L) {
            requestMap["max_id"] = tweetMaxId.toString()
        }
        TwitterApiStatusesUserTimeline().start(db, requestMap) {
            if (it != null) {
                getAllTweetCommon(db, userId)
            }
        }
        Log.d(TAG, "[END]getAllTweetCommon(${db}, ${userId})")
    }

    /**
     * TODO
     *
     */
    protected fun getAllTweet()
    {
        Log.d(TAG, "[START]getAllTweet()")
        val db = database.writableDatabase
        // debug
        db.delete("t_timelines", null, null)

        var userId = 0L
        db.rawQuery("SELECT user_id FROM t_users WHERE this = 1", null).use {
            if (it.count == 1) {
                it.moveToFirst()
                userId = it.getLong(it.getColumnIndex("user_id"))
            }
        }
        getAllTweetCommon(db, userId)
        Log.d(TAG, "[END]getAllTweet()")
    }

    protected fun getTweet()
    {

    }

}
