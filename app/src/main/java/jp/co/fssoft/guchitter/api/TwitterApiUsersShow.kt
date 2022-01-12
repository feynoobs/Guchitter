package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Json
import jp.co.fssoft.guchitter.utility.Utility

/**
 * Twitter api users show
 *
 * @property db
 * @constructor Create empty Twitter api users show
 */
class TwitterApiUsersShow(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/1.1/users/show.json", "GET", db)
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesUserTimeline::class.qualifiedName

        /**
         * Additional params
         */
        private var additionalParams: Map<String, String>? = null
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
        startMain(requestParams, additionalParams)
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
        result?.let {
            db.beginTransaction()
            try {
                val json = Json.jsonDecode(UserObject.serializer(), result)

                var insert = true
                db.rawQuery("SELECT * FROM t_users WHERE user_id = ?", arrayOf(json.id.toString())).use {
                    if (it.count == 1) {
                        insert = false
                    }
                }

                if (additionalParams != null) {
                    db.rawQuery("SELECT MAX(my) max FROM t_users", null).use {
                        it.moveToFirst()
                        val my = it.getLong(it.getColumnIndexOrThrow("max")) + 1

                        var values = ContentValues()
                        values.put("current", 0)
                        db.update("t_users", values, "current = ?", arrayOf("1"))

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
                            db.update("t_users", values, "user_id = ?", arrayOf(json.id.toString()))
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
                        db.update("t_time_lines", values, "user_id = ?", arrayOf(json.id.toString()))
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
        Log.v(TAG, "[END]finish(${result})")
    }
}
