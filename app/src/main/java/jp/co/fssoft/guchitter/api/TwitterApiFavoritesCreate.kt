package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility
import java.lang.Exception

/**
 * TODO
 *
 */
class TwitterApiFavoritesCreate : TwitterApiCommon("https://api.twitter.com/1.1/favorites/create.json", "POST")
{
    companion object
    {
        private val TAG = TwitterApiFavoritesCreate::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param db
     * @param additionalHeaderParams
     * @param callback
     */
    override fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>?, callback: ((String?) -> Unit)?)
    {
        Log.d(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        this.db = db
        this.callback = callback
        val copyHeaderParams = additionalHeaderParams?.toMutableMap()
        copyHeaderParams?.put("include_entities", true.toString())
        copyHeaderParams?.put("tweet_mode", "extended")
        startMain(db, copyHeaderParams)
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
        if (result != null) {
            db.beginTransaction()
            try {
                val json = Utility.jsonDecode(TweetObject.serializer(), result)
                val values = ContentValues()
                values.put("data", result)
                db.update("t_time_lines", values, "tweet_id = ${json.id}", null)
                db.setTransactionSuccessful()
            }
            catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            finally {
                db.endTransaction()
            }
        }
        else {
        }
        callback?.let { it(result) }
        Log.d(TAG, "[END]finish(${result})")
    }
}
