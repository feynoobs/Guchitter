package jp.co.fssoft.guchitter.api

import android.database.sqlite.SQLiteDatabase

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
        TODO("Not yet implemented")
    }

    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String)
    {
        TODO("Not yet implemented")
    }
}
