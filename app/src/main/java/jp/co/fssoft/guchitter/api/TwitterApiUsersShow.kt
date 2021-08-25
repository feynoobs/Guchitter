package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility

/**
 * TODO
 *
 */
class TwitterApiUsersShow : TwitterApiCommon("https://api.twitter.com/1.1/users/show.json", "GET")
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesUserTimeline::class.qualifiedName

        private var additionalParams: Map<String, String>? = null

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

        val requestParams = mapOf("user_id" to additionalHeaderParams!!["user_id"]!!)
        /***********************************************
         * 通常はDBから取得.認証時のみ引数.
         */
        additionalHeaderParams!!["oauth_token"].let {
            additionalHeaderParams!!["oauth_token_secret"].let {
                additionalParams = mapOf(
                    "oauth_token" to additionalHeaderParams!!["oauth_token"]!!,
                    "oauth_token_secret" to additionalHeaderParams!!["oauth_token_secret"]!!
                )
            }
        }
        startMain(db, requestParams, additionalParams)
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
            db.beginTransaction()
            try {
                val json = Utility.jsonDecode(UserObject.serializer(), result)

                var insert = true
                db.rawQuery("SELECT * FROM t_users WHERE user_id = ${json.id}", null).use {
                    if (it.count == 1) {
                        insert = false
                    }
                }

                if (additionalParams != null) {
                    db.rawQuery("SELECT MAX(my) max FROM t_users", null).use {
                        it.moveToFirst()
                        val my = it.getLong(it.getColumnIndex("max")) + 1

                        var values = ContentValues()
                        values.put("current", 0)
                        db.update("t_users", values, "current = 1", null)

                        values = ContentValues()
                        values.put("user_id", json.id)
                        values.put("oauth_token", additionalParams!!["oauth_token"]!!)
                        values.put("oauth_token_secret", additionalParams!!["oauth_token_secret"])
                        values.put("my", my)
                        values.put("data", result)
                        values.put("current", 1)
                        values.put("updated_at", Utility.now())
                        if (insert == true) {
                            values.put("created_at", Utility.now())
                            db.insert("t_users", null, values)
                        }
                        else {
                            db.update("t_users", values, "user_id = ${json.id}", null)
                        }
                    }
                }
                else {
                    val values = ContentValues()
                    values.put("user_id", json.id)
                    values.put("data", result)
                    values.put("updated_at", Utility.now())
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_users", null, values)
                    }
                    else {
                        db.update("t_time_lines", values, "user_id = ${json.id}", null)
                    }
                }
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
        Log.d(TAG, "[END]finish(${result})")
    }
}
