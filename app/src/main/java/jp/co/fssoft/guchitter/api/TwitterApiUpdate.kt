package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api update
 *
 * @property db
 * @constructor Create empty Twitter api update
 */
class TwitterApiUpdate(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/statuses/update.json", "POST", db)
{
    /**
     * Companion
     *
     * @constructor Create empty Companion
     */
    companion object
    {
        private val TAG = TwitterApiUpdate::class.qualifiedName
    }

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    override fun start(additionalHeaderParams: Map<String, String>?) : TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        startMain(additionalHeaderParams)
        Log.v(TAG, "[END]start(${db}, ${additionalHeaderParams}, ${callback})")

        return this
    }

    /**
     * Finish
     *
     * @param result
     */
    override fun finish(result: String?)
    {
        Log.v(TAG, "[START]finish(${result}")
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result}")
    }
}
