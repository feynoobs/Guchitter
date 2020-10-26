package jp.co.fssoft.guchitter.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.guchitter.utility.Utility
import kotlinx.serialization.builtins.list

/**
 * TODO
 *
 */
class TwitterApiStatusesHomeTimeline : TwitterApiCommon("https://api.twitter.com/1.1/statuses/home_timeline.json", "GET")
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesHomeTimeline::class.qualifiedName
    }

    /**
     * TODO
     *
     * @param db
     * @param additionalHeaderParams
     * @param callback
     */
    override fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>?,  callback: ((String?) -> Unit)?)
    {
        Log.d(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        this.db = db
        this.callback = callback
        startMain(db, additionalHeaderParams)
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

        val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, result)
        db.beginTransaction()
        try {
            jsonList.forEach {
                Log.e(TAG, it.toString())
                db.rawQuery("SELECT id, home FROM t_timelines WHERE tweet_id = ${it.id}", null).use { cursor ->
                    if (cursor.count != 1) {
                        val values = ContentValues()
                        values.put("tweet_id", it.id)
                        values.put("user_id", it.user?.id)
                        values.put("home", 1)
                        values.put("data", Utility.jsonEncode(TweetObject.serializer(), it))
                        values.put("created_at", Utility.now())
                        values.put("updated_at", Utility.now())
                        db.insert("t_timelines", null, values)
                    }
                    else {
                        if (cursor.getInt(cursor.getColumnIndex("home")) == 0) {
                            val values = ContentValues()
                            db.update("t_timelines", values, "id = ${cursor.getLong(cursor.getColumnIndex("id"))}", null)
                        }
                        else {

                        }
                    }
                }
            }
            db.setTransactionSuccessful()
        }
        finally {
            db.endTransaction()
        }

        if (jsonList.count() == 0) {
            callback?.let { it(null) }
        }
        else {
            callback?.let { it(result) }
        }
        Log.d(TAG, "[END]finish(${result})")
    }
}