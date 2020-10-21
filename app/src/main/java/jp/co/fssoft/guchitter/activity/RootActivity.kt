package jp.co.fssoft.guchitter.activity

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.api.TwitterApiStatusesHomeTimeline
import jp.co.fssoft.guchitter.api.TwitterApiStatusesUserTimeline
import jp.co.fssoft.guchitter.database.DatabaseHelper

open class RootActivity : AppCompatActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = RootActivity::class.qualifiedName
    }

    protected val database by lazy {
        /**
         *
         */
        DatabaseHelper(applicationContext)
    }


    /**
     * TODO
     *
     * @param db
     * @param userId
     * @param recursive
     */
    protected fun getPrevUserTweet(db: SQLiteDatabase, userId: Long, recursive: Boolean = false)
    {
        Log.d(TAG, "[START]getPrevUserTweet(${db}, ${userId}, ${recursive})")
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
            if (recursive == true) {
                if (it != null) {
                    getPrevUserTweet(db, userId, recursive)
                }
            }
        }
        Log.d(TAG, "[END]getPrevUserTweet(${db}, ${userId}, ${recursive})")
    }


    protected fun getPrevHomeTweet(db: SQLiteDatabase, recursive: Boolean = false)
    {
        Log.d(TAG, "[START]getPrevUserTweet(${db}, ${recursive})")
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
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMaxId != 0L) {
            requestMap["max_id"] = tweetMaxId.toString()
        }
        TwitterApiStatusesHomeTimeline().start(db, requestMap) {
            if (recursive == true) {
                if (it != null) {
                    getPrevHomeTweet(db, recursive)
                }
            }
        }
        Log.d(TAG, "[END]getPrevUserTweet(${db}, ${recursive})")
    }

}
