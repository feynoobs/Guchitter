package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Json
import jp.co.fssoft.guchitter.utility.Utility
import kotlinx.serialization.builtins.ListSerializer

/**
 * Twitter api statuses user timeline
 *
 * @property db
 * @constructor Create empty Twitter api statuses user timeline
 */
class TwitterApiStatusesUserTimeline(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET", db)
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesUserTimeline::class.qualifiedName
    }

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    override fun start(additionalHeaderParams: Map<String, String>?) : TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${additionalHeaderParams})")
        startMain(additionalHeaderParams)
        Log.v(TAG, "[END]start(${additionalHeaderParams})")

        return this
    }

    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String?)
    {
        Log.v(TAG, "[START]finish(${result})")
        result?.let {
            val jsonList = Json.jsonListDecode(ListSerializer(TweetObject.serializer()), it)
            db.beginTransaction()
            try {
                jsonList.forEach {
                    var insert = true
                    var values = ContentValues()
                    val userId =
                        if (it.retweetedTweet == null) {
                            it.user?.id
                        }
                        else {
                            it.retweetedTweet.user?.id
                        }
                    val userData =
                        if (it.retweetedTweet == null) {
                            it.user
                        }
                        else {
                            it.retweetedTweet.user
                        }

                    values.put("user_id", userId)
                    values.put("data", Json.jsonEncode(UserObject.serializer(), userData!!))
                    values.put("updated_at", Utility.now())
                    db.rawQuery("SELECT id FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
                        if (it.count == 1) {
                            insert = false
                        }
                    }
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_users", null, values)
                    }
                    else {
                        db.update("t_users", values, "user_id = ?", arrayOf(userId.toString()))
                    }

                    insert = true
                    values = ContentValues()
                    values.put("tweet_id", it.id)
                    values.put("reply_tweet_id", it.replyTweetId)
                    values.put("user_id", it.user?.id)
                    values.put("data", Json.jsonEncode(TweetObject.serializer(), it))
                    values.put("updated_at", Utility.now())
                    db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ?", arrayOf(it.id.toString())).use {
                        if (it.count == 1) {
                            insert = false
                        }
                    }
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_time_lines", null, values)
                    }
                    else {
                        db.update("t_time_lines", values, "tweet_id = ?", arrayOf(it.id.toString()))
                    }
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
        }
        callback?.let { it(result) }

        Log.v(TAG, "[END]finish(${result})")
    }
}
