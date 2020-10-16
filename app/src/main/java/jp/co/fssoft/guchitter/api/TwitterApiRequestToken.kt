package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * TODO
 *
 */
class TwitterApiRequestToken : TwitterApiCommon("https://api.twitter.com/oauth/request_token", "POST")
{
    /**
     *
     */
    companion object
    {
        private val TAG = TwitterApiRequestToken::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param db
     * @param additionalHeaderParams
     * @param callback
     */
    override fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>?, callback: ((String?)->Unit)?)
    {
        this.callback = callback
        startMain(db, null, mapOf("oauth_callback" to CALLBACK_URL))
    }


    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String)
    {
        Log.d(TAG, "[START]finish(${result})")
        callback?.let { it(result) }
        Log.d(TAG, "[END]finish(${result})")
    }
}