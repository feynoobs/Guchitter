package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api statuses destroy
 *
 * @property id
 * @property db
 * @constructor Create empty Twitter api statuses destroy
 */
class TwitterApiStatusesDestroy(private val id: Long, private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/statuses/destroy/${id}.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiStatusesDestroy::class.qualifiedName
    }


    override fun start(additionalHeaderParams: Map<String, String>?) : TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${additionalHeaderParams})")
        startMain(additionalHeaderParams)
        Log.v(TAG, "[END]start(${additionalHeaderParams})")

        return this;
    }

    override fun finish(result: String?)
    {
        Log.v(TAG, "[START]finish(${result})")
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result})")
    }
}