package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.lang.Exception

/**
 * Twitter api statuses retweet
 *
 * @property id
 * @property db
 * @constructor Create empty Twitter api statuses retweet
 */
class TwitterApiStatusesRetweet(private val id: Long, private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/statuses/retweet/${id}.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiStatusesRetweet::class.qualifiedName
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
        val copyHeaderParams = additionalHeaderParams?.toMutableMap()
        copyHeaderParams?.put("trim_user", false.toString())
        copyHeaderParams?.put("tweet_mode", "extended")
        startMain(copyHeaderParams)
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
            db.beginTransaction()
            try {
                val values = ContentValues()
                values.put("data", it)
                db.update("t_time_lines", values, "tweet_id = ?", arrayOf(id.toString()))
                db.setTransactionSuccessful()
            }
            catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            finally {
                db.endTransaction()
            }
        }
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result})")
    }
}
