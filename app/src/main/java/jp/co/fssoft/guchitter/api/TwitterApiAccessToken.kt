package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

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
//        this.db = db
        this.callback = callback
        startMain(db, null, additionalHeaderParams)
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
/*
        result?.let {
            val token = Utility.splitQuery(result)

            db.beginTransaction()
            try {
                var insert = true
                db.rawQuery("SELECT * FROM t_users WHERE user_id = ${token["user_id"]}", null).use {
                    if (it.count == 1) {
                        insert = false
                    }
                }

                var my = 0L
                db.rawQuery("SELECT MAX(my) max FROM t_users", null).use {
                    it.moveToFirst()
                    my = it.getLong(it.getColumnIndex("max")) + 1
                }

                var values = ContentValues()
                values.put("current", 0)
                db.update("t_users", values, "current = 1", null)

                values = ContentValues()
                values.put("oauth_token", token["oauth_token"])
                values.put("oauth_token_secret", token["oauth_token_secret"])
                values.put("user_id", token["user_id"]?.toLong())
                values.put("my", my)
                values.put("current", 1)
                values.put("updated_at", Utility.now())
                if (insert == true) {
                    values.put("created_at", Utility.now())
                    db.insert("t_users", null, values)
                }
                else {
                    db.update("t_users", values, "user_id = ${token["user_id"]}", null)
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
        }
*/
        callback?.let { it(result) }

        Log.d(TAG, "[END]finish(${result})")
    }
}
