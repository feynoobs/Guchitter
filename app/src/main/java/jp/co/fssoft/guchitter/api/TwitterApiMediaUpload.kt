package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

class TwitterApiMediaUpload(db: SQLiteDatabase) : TwitterApiCommon("https://upload.twitter.com/1.1/media/upload.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiMediaUpload::class.qualifiedName
    }

    override fun start(additionalHeaderParams: Map<String, String>?): TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${additionalHeaderParams})")
        startMain(null, null, additionalHeaderParams)
        Log.v(TAG, "[END]start(${additionalHeaderParams})")
        return this
    }

    override fun finish(result: String?)
    {
        Log.v(TAG, "[START]finish(${result})")
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result})")
    }
}