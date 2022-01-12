package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api access token
 *
 * @property db
 * @constructor Create empty Twitter api access token
 */
class TwitterApiAccessToken(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/oauth/access_token", "POST", db)
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiAccessToken::class.qualifiedName
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
        startMain(null, additionalHeaderParams)
        Log.v(TAG, "[END]start(${db}, ${additionalHeaderParams}, ${callback})")

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
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result})")
    }
}
