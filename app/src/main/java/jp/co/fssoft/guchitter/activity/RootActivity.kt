package jp.co.fssoft.guchitter.activity

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.TweetObject
import jp.co.fssoft.guchitter.api.TwitterApiStatusesHomeTimeline
import jp.co.fssoft.guchitter.api.TwitterApiStatusesUserTimeline
import jp.co.fssoft.guchitter.api.UserObject
import jp.co.fssoft.guchitter.database.DatabaseHelper
import jp.co.fssoft.guchitter.utility.Utility
import kotlinx.serialization.builtins.list

open class RootActivity : AppCompatActivity()
{
    companion object
    {
        /**
         *
         */
        private val TAG = RootActivity::class.qualifiedName

    }

    /**
     *
     */
    protected val database by lazy {
        DatabaseHelper(applicationContext)
    }


    /**
     * TODO
     *
     * @param db
     * @param userId
     */
    protected fun getCurrentUserTweet(db: SQLiteDatabase, userId: Long) : List<TweetObject>
    {
        Log.d(TAG, "[START]getCurrentUserTweet(${db}, ${userId})")

        val tweetObjects = mutableListOf<TweetObject>()
        val query =
            """
                SELECT 
                    user_id, data 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ${userId}
                ORDER BY
                    tweet_id
                DESC
            """
        db.rawQuery(query, null).use {
            var movable = it.moveToFirst()
            while (movable) {
                val tweetObject = Utility.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndex("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ${userId}", null).use {
                    it.moveToFirst()
                    val userObject = Utility.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndex("data")))
                    tweetObject.user = userObject
                }
                tweetObjects.add(tweetObject)
                movable = it.moveToNext()
            }
        }
        Log.d(TAG, "[END]getCurrentUserTweet(${db}, ${userId})")

        return tweetObjects
    }

    /**
     * TODO
     *
     * @param db
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getNextUserTweet(db: SQLiteDatabase, userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getNextUserTweet(${db}, ${userId}, ${recursive})")

        var tweetMaxId = 0L
        val query =
            """
                SELECT 
                    tweet_id 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ${userId} 
                ORDER BY
                    tweet_id
                DESC
                LIMIT
                    1
            """
        db.rawQuery(query, null).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMaxId = it.getLong(it.getColumnIndex("tweet_id"))
            }
        }
        val requestMap = mutableMapOf(
            "count" to 10.toString(),
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended",
            "user_id" to userId.toString()
        )
        if (tweetMaxId != 0L) {
            requestMap["since_id"] = tweetMaxId.toString()
        }
        TwitterApiStatusesUserTimeline(userId).start(db, requestMap) {
            if (it != null) {
                val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, it)
                jsonList.forEach {
                    var tweetObject = it
                    if (it.retweetedTweet != null) {
                        tweetObject = it.retweetedTweet
                    }
                    Utility.saveImage(applicationContext, Utility.Companion.ImagePrefix.USER, tweetObject.user!!.profileImageUrl)
                }
                if (recursive == true) {
                    if (jsonList.isEmpty() != false) {
                        getNextHomeTweet(db, userId, recursive)
                    }
                    else {
                        callback?.let { it() }
                    }
                }
                else {
                    callback?.let { it() }
                }
            }
            else {
                callback?.let { it() }
            }
        }

        Log.d(TAG, "[END]getNextUserTweet(${db}, ${userId}, ${recursive})")
    }

    /**
     * TODO
     *
     * @param db
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getPrevUserTweet(db: SQLiteDatabase, userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevUserTweet(${db}, ${userId}, ${recursive})")

        var tweetMinId = 0L
        val query =
            """
                SELECT 
                    tweet_id 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ${userId} 
                ORDER BY
                    tweet_id
                ASC
                LIMIT
                    1
            """
        db.rawQuery(query, null).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMinId = it.getLong(it.getColumnIndex("tweet_id")) - 1
            }
        }
        val requestMap = mutableMapOf(
            "count" to 10.toString(),
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended",
            "user_id" to userId.toString()
        )
        if (tweetMinId != 0L) {
            requestMap["max_id"] = tweetMinId.toString()
        }

        TwitterApiStatusesUserTimeline(userId).start(db, requestMap) {
            if (it != null) {
                val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, it)
                jsonList.forEach {
                    var tweetObject = it
                    if (it.retweetedTweet != null) {
                        tweetObject = it.retweetedTweet
                    }
                    Utility.saveImage(applicationContext, Utility.Companion.ImagePrefix.USER, tweetObject.user!!.profileImageUrl)
                }
                if (recursive == true) {
                    if (jsonList.isEmpty() != false) {
                        getNextHomeTweet(db, userId, recursive)
                    }
                    else {
                        callback?.let { it() }
                    }
                }
                else {
                    callback?.let { it() }
                }
            }
            else {
                callback?.let { it() }
            }
        }
        Log.d(TAG, "[END]getPrevUserTweet(${db}, ${userId}, ${recursive})")
    }



    /**
     * TODO
     *
     * @param db
     * @param userId
     */
    protected fun getCurrentHomeTweet(db: SQLiteDatabase, userId: Long) : List<TweetObject>
    {
        Log.d(TAG, "[START]getCurrentHomeTweet(${db}, ${userId})")
        val tweetObjects = mutableListOf<TweetObject>()
        val query =
            """
                SELECT 
                    t_time_lines.user_id, t_time_lines.data 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ${userId} AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
            """
        db.rawQuery(query, null).use {
            var movable = it.moveToFirst()
            while (movable) {
                val userId = it.getLong(it.getColumnIndex("user_id"))
                val tweetObject = Utility.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndex("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ${userId}", null).use {
                    it.moveToFirst()
                    val userObject = Utility.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndex("data")))
                    tweetObject.user = userObject
                }
                tweetObjects.add(tweetObject)
                movable = it.moveToNext()
            }
        }
        Log.d(TAG, "[END]getCurrentHomeTweet(${db}, ${userId})")

        return tweetObjects
    }

    /**
     * TODO
     *
     * @param db
     * @param userId
     * @param recursive
     */
    protected fun getNextHomeTweet(db: SQLiteDatabase, userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getNextHomeTweet(${db}, ${userId}, ${recursive})")
        var tweetMaxId = 0L
        val query =
            """
                SELECT 
                    t_time_lines.tweet_id 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ${userId} AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
                LIMIT
                    1
            """
        db.rawQuery(query, null).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMaxId = it.getLong(it.getColumnIndex("tweet_id"))
            }
        }
        val requestMap = mutableMapOf(
            "count" to 10.toString(),
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMaxId != 0L) {
            requestMap["since_id"] = tweetMaxId.toString()
        }
        TwitterApiStatusesHomeTimeline(userId).start(db, requestMap) {
            if (it != null) {
                val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, it)
                jsonList.forEach {
                    var tweetObject = it
                    if (it.retweetedTweet != null) {
                        tweetObject = it.retweetedTweet
                    }
                    Utility.saveImage(applicationContext, Utility.Companion.ImagePrefix.USER, tweetObject.user!!.profileImageUrl)
                }
                if (recursive == true) {
                    if (jsonList.isEmpty() != false) {
                        getNextHomeTweet(db, userId, recursive)
                    }
                    else {
                        callback?.let { it() }
                    }
                }
                else {
                    callback?.let { it() }
                }
            }
            else {
                callback?.let { it() }
            }
        }
        Log.d(TAG, "[END]getNextHomeTweet(${db}, ${userId}, ${recursive})")
    }

    /**
     * TODO
     *
     * @param db
     * @param userId
     * @param recursive
     */
    protected fun getPrevHomeTweet(db: SQLiteDatabase, userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevHomeTweet(${db}, ${userId}, ${recursive})")
        var tweetMinId = 0L
        val query =
            """
                SELECT 
                    t_time_lines.tweet_id 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ${userId} AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                ASC
                LIMIT
                    1
            """
        db.rawQuery(query, null).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMinId = it.getLong(it.getColumnIndex("tweet_id")) - 1
            }
        }
        val requestMap = mutableMapOf(
            "count" to 10.toString(),
            "exclude_replies" to true.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMinId != 0L) {
            requestMap["max_id"] = tweetMinId.toString()
        }
        TwitterApiStatusesHomeTimeline(userId).start(db, requestMap) {
            if (it != null) {
                val jsonList = Utility.jsonListDecode(TweetObject.serializer().list, it)
                jsonList.forEach {
                    Utility.saveImage(applicationContext, Utility.Companion.ImagePrefix.USER, it.user!!.profileImageUrl)
                }
                if (recursive == true) {
                    if (jsonList.isEmpty() != false) {
                        getPrevHomeTweet(db, userId, recursive)
                    }
                    else {
                        callback?.let { it() }
                    }
                }
                else {
                    callback?.let { it() }
                }
            }
            else {
                callback?.let { it() }
            }
        }
        Log.d(TAG, "[END]getPrevHomeTweet(${db}, ${userId}, ${recursive})")
    }

    /**
     * TODO
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "[START]onCreate(${savedInstanceState})")
        setContentView(R.layout.root_activity)
        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
