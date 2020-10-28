package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility
import kotlinx.serialization.builtins.list
import kotlin.system.exitProcess

/**
 * TODO
 *
 */
class TwitterApiStatusesHomeTimeline(private val userId: Long) : TwitterApiCommon("https://api.twitter.com/1.1/statuses/home_timeline.json", "GET")
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesHomeTimeline::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param db
     * @param additionalHeaderParams
     * @param callback
     */
    override fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>?,  callback: ((String?) -> Unit)?)
    {
        Log.d(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        this.db = db
        this.callback = callback
        startMain(db, additionalHeaderParams)
        Log.d(TAG, "[END]start(${db}, ${additionalHeaderParams}, ${callback})")
    }

    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String?)
    {
        Log.d(TAG, "[START]finish(${result})")

        result?.let {
            val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, result)
            db.beginTransaction()
            try {
                jsonList.forEach {
                    db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ${it.id}", null).use { cursor ->
                        if (cursor.count == 0) {
                            var insert = true
                            var values = ContentValues()
                            values.put("user_id", it.user?.id)
                            values.put("data", Utility.jsonEncode(UserObject.serializer(), it.user!!))
                            values.put("updated_at", Utility.now())
                            db.rawQuery("SELECT id FROM t_users WHERE user_id = ${it.user?.id}", null).use {
                                if (it.count == 1) {
                                    insert = false
                                }
                            }
                            if (insert == true) {
                                values.put("created_at", Utility.now())
                                db.insert("t_users", null, values)
                            }
                            else {
                                db.update("t_users", values, "user_id = ${it.user?.id}", null)
                            }

                            values = ContentValues()
                            values.put("tweet_id", it.id)
                            values.put("user_id", it.user?.id)
                            it.retweetedTweet?.let {
                                values.put("retweet_original_tweet_id", it.id)
                                values.put("retweet_original_user_id", it.user?.id)
                            }
                            values.put("data", Utility.jsonEncode(TweetObject.serializer(), it))
                            values.put("created_at", Utility.now())
                            values.put("updated_at", Utility.now())
                            db.insert("t_time_lines", null, values)
                        }
                    }
                    val values = ContentValues()
                    values.put("tweet_id", it.id)
                    values.put("user_id", userId)
                    values.put("created_at", Utility.now())
                    values.put("updated_at", Utility.now())
                    db.insert("r_home_tweets", null, values)
                }
/*
                    if (it.retweetedTweet == null) {
                        db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ${it.id}", null).use { cursor ->
                            if (cursor.count == 0) {
                                var insert = true
                                var values = ContentValues()
                                values.put("user_id", it.user?.id)
                                values.put("data", Utility.jsonEncode(UserObject.serializer(), it.user!!))
                                values.put("updated_at", Utility.now())
                                db.rawQuery("SELECT id FROM t_users WHERE user_id = ${it.user?.id}", null).use {
                                    if (it.count == 1) {
                                        insert = false
                                    }
                                }
                                if (insert == true) {
                                    values.put("created_at", Utility.now())
                                    db.insert("t_users", null, values)
                                }
                                else {
                                    db.update("t_users", values, "user_id = ${it.user?.id}", null)
                                }

                                values = ContentValues()
                                values.put("tweet_id", it.id)
                                values.put("user_id", it.user?.id)
                                val tweetObject = it
                                it.user = null
                                values.put("data", Utility.jsonEncode(TweetObject.serializer(), tweetObject))
                                values.put("created_at", Utility.now())
                                values.put("updated_at", Utility.now())
                                db.insert("t_time_lines", null, values)
                            }
                        }
                        val values = ContentValues()
                        values.put("tweet_id", it.id)
                        values.put("user_id", userId)
                        values.put("created_at", Utility.now())
                        values.put("updated_at", Utility.now())
                        db.insert("r_home_tweets", null, values)
                    }
                    else {
                        db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ${it.id}", null).use { cursor ->
                            if (cursor.count == 0) {
                                var insert = true
                                var values = ContentValues()
                                values.put("user_id", it.retweetedTweet.user?.id)
                                values.put("data", Utility.jsonEncode(UserObject.serializer(), it.retweetedTweet.user!!))
                                values.put("updated_at", Utility.now())
                                db.rawQuery("SELECT id FROM t_users WHERE user_id = ${it.retweetedTweet?.id}", null).use {
                                    if (it.count == 1) {
                                        insert = false
                                    }
                                }
                                if (insert == true) {
                                    values.put("created_at", Utility.now())
                                    db.insert("t_users", null, values)
                                }
                                else {
                                    db.update("t_users", values, "user_id = ${it.user?.id}", null)
                                }

                                values = ContentValues()
                                values.put("tweet_id", it.retweetedTweet.id)
                                values.put("user_id", it.retweetedTweet.user?.id)
                                values.put("retweeted_user_id", it.user?.id)
                                val tweetObject = it.retweetedTweet
                                tweetObject.user = null
                                values.put("data", Utility.jsonEncode(TweetObject.serializer(), tweetObject))
                                values.put("created_at", Utility.now())
                                values.put("updated_at", Utility.now())
                                db.insert("t_time_lines", null, values)
                            }
                        }
                        val values = ContentValues()
                        values.put("tweet_id", it.retweetedTweet.id)
                        values.put("user_id", userId)
                        values.put("created_at", Utility.now())
                        values.put("updated_at", Utility.now())
                        db.insert("r_home_tweets", null, values)
                    }
                }
*/
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
        }
        callback?.let { it(result) }

        Log.d(TAG, "[END]finish(${result})")
    }
}