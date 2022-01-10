package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

class TwitterApiUpdate : TwitterApiCommon("https://api.twitter.com/1.1/statuses/update.json", "POST")
{
    companion object
    {
        private val TAG = TwitterApiUpdate::class.qualifiedName
    }

    override fun start(db: SQLiteDatabase, additionalHeaderParams: Map<String, String>?, callback: ((String?) -> Unit)?)
    {
        Log.d(TAG, "[START]start(${db}, ${additionalHeaderParams}, ${callback})")
        startMain(db, additionalHeaderParams)
        Log.d(TAG, "[END]start(${db}, ${additionalHeaderParams}, ${callback})")
    }

    override fun finish(result: String?)
    {
        Log.d(TAG, "[START]finish(${result}")
        Log.d(TAG, "[END]finish(${result}")
    }
}
