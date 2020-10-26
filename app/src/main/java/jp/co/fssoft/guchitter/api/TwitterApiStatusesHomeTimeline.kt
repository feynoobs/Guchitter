package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility
import kotlinx.serialization.builtins.list

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
                    Log.e(TAG, it.toString())
                    db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ${it.id}", null).use { cursor ->
                        if (cursor.count == 0) {
                            val values = ContentValues()
                            values.put("tweet_id", it.id)
                            values.put("user_id", it.user?.id)
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