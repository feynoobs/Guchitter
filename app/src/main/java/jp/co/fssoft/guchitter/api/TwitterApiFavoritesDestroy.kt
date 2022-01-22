package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Json
import java.lang.Exception

/**
 * Twitter api favorites destroy
 *
 * @property db
 * @constructor Create empty Twitter api favorites destroy
 */
class TwitterApiFavoritesDestroy(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/favorites/destroy.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiFavoritesDestroy::class.qualifiedName
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
        copyHeaderParams?.put("include_entities", true.toString())
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
                val json = Json.jsonDecode(TweetObject.serializer(), it)
                val values = ContentValues()
                values.put("data", it)
                db.update("t_time_lines", values, "tweet_id = ?", arrayOf(json.id.toString()))
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
