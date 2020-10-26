package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility

/**
 * TODO
 *
 */
class TwitterApiAccessToken : TwitterApiCommon("https://api.twitter.com/oauth/access_token", "POST")
{

    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiAccessToken::class.qualifiedName
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
        Log.d(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        this.db = db
        this.callback = callback
        startMain(db, null, additionalHeaderParams)
        Log.d(TAG, "[END]start(${db}, ${additionalHeaderParams}, ${callback})")
    }

    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String)
    {
        Log.d(TAG, "[START]finish(${result})")

        val token = Utility.splitQuery(result)
        db.beginTransaction()
        try {
            db.delete("t_users", null, null)
            val values = ContentValues()
            values.put("oauth_token", token["oauth_token"])
            values.put("oauth_token_secret", token["oauth_token_secret"])
            values.put("screen_name", token["screen_name"])
            values.put("user_id", token["user_id"])
            values.put("this", 1)
            values.put("created_at", Utility.now())
            values.put("updated_at", Utility.now())
            db.insert("t_users", null, values)
            db.setTransactionSuccessful()
        }
        finally {
            db.endTransaction()
        }

        callback?.let { it(null) }
        Log.d(TAG, "[END]finish(${result})")
    }
}
