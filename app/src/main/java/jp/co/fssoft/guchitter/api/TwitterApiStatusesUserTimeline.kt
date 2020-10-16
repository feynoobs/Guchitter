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
class TwitterApiStatusesUserTimeline : TwitterApiCommon("https://api.twitter.com/1.1/statuses/user_timeline.json", "GET")
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesUserTimeline::class.qualifiedName
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
        this.db = db
        this.callback = callback
        startMain(db, additionalHeaderParams)
    }

    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String)
    {
        Log.d(TAG, "[START]finish(${result})")
        Log.e(TAG, result)
        val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, result)
        db.beginTransaction()
        try {
            jsonList.forEach {
                val values = ContentValues()
                values.put("tweet_id", it.id)
                values.put("user_id", it.user?.id)
                values.put("data", Utility.jsonEncode(TweetObject.serializer(), it))
                values.put("created_at", Utility.now())
                values.put("updated_at", Utility.now())
                db.insert("t_timelines", null, values)
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
