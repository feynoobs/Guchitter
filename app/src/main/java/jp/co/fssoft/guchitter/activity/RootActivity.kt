package jp.co.fssoft.guchitter.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.URLUtil
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.guchitter.R
import jp.co.fssoft.guchitter.api.*
import jp.co.fssoft.guchitter.database.DatabaseHelper
import jp.co.fssoft.guchitter.utility.Imager
import jp.co.fssoft.guchitter.utility.Json
import kotlinx.serialization.builtins.ListSerializer

/**
 * TODO
 *
 */
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

    private val launcher by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) {

            }
        }
    }

    /**
     * TODO
     *
     * @param api
     * @param request
     * @param callback
     * @param userId
     * @param recursive
     */
    private fun getTweetsCommon(api: TwitterApiCommon, request: Map<String, String>, callback: (()->Unit)?, userId: Long, recursive: ((Long, Boolean, (()->Unit)?) -> Unit)? = null)
    {
        Log.d(TAG, "[START]getTweetsCommon(${api}, ${request}, ${callback}, ${userId}, ${recursive})")
        val db = database.writableDatabase
        api.start(request).callback = {
            if (it != null) {
                val jsonList = Json.jsonListDecode(ListSerializer(TweetObject.serializer()), it)
                jsonList.forEach {
                    var tweetObject = it
                    it.retweetedTweet?.let {
                        tweetObject = it
                    }
                    Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.USER, tweetObject.user!!.profileImageUrl)
                    tweetObject.user!!.profileBannerUrl?.let {
                        val file = URLUtil.guessFileName(it, null, null).removeSuffix(".bin")
                        Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.BANNER, "${it}/300x100", file)
                    }
                    tweetObject.extendedEntities?.let {
                        it.medias.forEach {
                            Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.PICTURE, it.mediaUrl)
                        }
                    }
                }
                if (recursive != null) {
                    if (jsonList.isEmpty() != false) {
                        recursive(userId, true, callback)
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

        Log.d(TAG, "[END]getTweetsCommon(${db}, ${api}, ${request}, ${callback}, ${userId}, ${recursive})")
    }

    /**
     * TODO
     *
     * @param tweetId
     * @param replies
     */
    private fun getReplyHomeTweets(tweetId: Long, replies: MutableList<TweetObject>)
    {
        val db = database.readableDatabase
        val query =
            """
                SELECT 
                    tweet_id, user_id, data 
                FROM 
                    t_time_lines
                WHERE
                    reply_tweet_id = ?
                ORDER BY
                    tweet_id
                DESC
            """
        db.rawQuery(query, arrayOf(tweetId.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
                    it.moveToFirst()
                    val userObject = Json.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                    tweetObject.user = userObject
                }
                replies.add(tweetObject)
                getReplyHomeTweets(it.getLong(it.getColumnIndexOrThrow("tweet_id")), replies)
                movable = it.moveToNext()
            }
        }
    }


    /**
     * TODO
     *
     * @param replyTweetId
     * @param replies
     */
    private fun getReplyUserTweets(replyTweetId: Long, replies: MutableList<TweetObject>)
    {
        Log.d(TAG, "[START]getReplyUserTweets(${replyTweetId}, ${replies})")

        val db = database.readableDatabase
        val query =
            """
                SELECT 
                    reply_tweet_id, user_id, data 
                FROM 
                    t_time_lines
                WHERE
                    tweet_id = ?
                ORDER BY
                    tweet_id
                DESC
            """
        db.rawQuery(query, arrayOf(replyTweetId.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
                    it.moveToFirst()
                    val userObject = Json.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                    tweetObject.user = userObject
                }
                replies.add(0, tweetObject)
                if (it.isNull(it.getColumnIndexOrThrow("reply_tweet_id")) == false) {
                    getReplyUserTweets(it.getLong(it.getColumnIndexOrThrow("reply_tweet_id")), replies)
                }

                movable = it.moveToNext()
            }
        }

        Log.d(TAG, "[END]getReplyUserTweets(${replyTweetId}, ${replies})")
    }

    /**
     * TODO
     *
     * @param userId
     */
    protected fun getCurrentUserTweet(userId: Long) : List<List<TweetObject>>
    {
        Log.d(TAG, "[START]getCurrentUserTweet(${userId})")

        val db = database.readableDatabase
        val tweetObjects = mutableListOf<MutableList<TweetObject>>()
        val query =
            """
                SELECT 
                    user_id, data 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ?
                ORDER BY
                    tweet_id
                DESC
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
                    it.moveToFirst()
                    val userObject = Json.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                    tweetObject.user = userObject
                }
                tweetObjects.add(mutableListOf(tweetObject))
                tweetObject.replyTweetId?.let {
                    getReplyUserTweets(it, tweetObjects.last())
                }
                movable = it.moveToNext()
            }
        }

        /***********************************************
         * 先祖が共通なツイートは消しておく
         */
        var nextI = 0
        while (nextI < tweetObjects.size) {
            for (i in nextI until tweetObjects.size) {
                val removeIndex = mutableListOf<Int>()
                nextI = i + 1
                for (j in i + 1 until tweetObjects.size) {
                    if (tweetObjects[i].size > tweetObjects[j].size) {
                        var removeable = true
                        for (k in 0 until tweetObjects[j].size) {
                            if (tweetObjects[i][k].id != tweetObjects[j][k].id) {
                                removeable = false
                                break
                            }
                        }
                        if (removeable == true) {
                            removeIndex.add(0, j)
                        }
                    }
                }
                removeIndex.forEach {
                    tweetObjects.removeAt(it)
                }
            }
        }

        Log.d(TAG, "[END]getCurrentUserTweet(${userId})")
        return tweetObjects
    }

    /**
     * TODO
     *
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getNextUserTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getNextUserTweet(${userId}, ${recursive}, ${callback})")

        val db = database.writableDatabase
        var tweetMaxId = 0L
        val query =
            """
                SELECT 
                    tweet_id 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ?
                ORDER BY
                    tweet_id
                DESC
                LIMIT
                    1
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMaxId = it.getLong(it.getColumnIndexOrThrow("tweet_id"))
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended",
            "user_id" to userId.toString()
        )
        if (tweetMaxId != 0L) {
            requestMap["since_id"] = tweetMaxId.toString()
        }

        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesUserTimeline(db), requestMap, callback, userId, ::getNextUserTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesUserTimeline(db), requestMap, callback, userId)
        }

        Log.d(TAG, "[END]getNextUserTweet(${userId}, ${recursive}, ${callback})")
    }

    /**
     * TODO
     *
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getPrevUserTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevUserTweet(${userId}, ${recursive}, ${callback})")

        val db = database.readableDatabase
        var tweetMinId = 0L
        val query =
            """
                SELECT 
                    tweet_id 
                FROM 
                    t_time_lines
                WHERE
                    user_id = ?
                ORDER BY
                    tweet_id
                ASC
                LIMIT
                    1
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMinId = it.getLong(it.getColumnIndexOrThrow("tweet_id")) - 1
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended",
            "user_id" to userId.toString()
        )
        if (tweetMinId != 0L) {
            requestMap["max_id"] = tweetMinId.toString()
        }

        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesUserTimeline(db), requestMap, callback, userId, ::getPrevUserTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesUserTimeline(db), requestMap, callback, userId)
        }

        Log.d(TAG, "[END]getPrevUserTweet(${userId}, ${recursive}, ${callback})")
    }



    /**
     * TODO
     *
     * @param userId
     */
    protected fun getCurrentHomeTweet(userId: Long) : List<List<TweetObject>>
    {
        Log.d(TAG, "[START]getCurrentHomeTweet(${userId})")
        val db = database.readableDatabase
        val tweetObjects = mutableListOf<MutableList<TweetObject>>()
        val query =
            """
                SELECT 
                    t_time_lines.user_id, t_time_lines.data 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                WHERE
                    t_time_lines.reply_tweet_id IS NULL
                ORDER BY
                    t_time_lines.tweet_id
                DESC
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                db.rawQuery("SELECT data FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
                    it.moveToFirst()
                    val userObject = Json.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                    tweetObject.user = userObject
                }
                tweetObjects.add(mutableListOf(tweetObject))
                getReplyHomeTweets(tweetObject.id, tweetObjects.last())

                movable = it.moveToNext()
            }
        }
        Log.d(TAG, "[END]getCurrentHomeTweet(${userId})")

        return tweetObjects
    }

    /**
     * TODO
     *
     * @param userId
     * @param recursive
     */
    protected fun getNextHomeTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getNextHomeTweet(${userId}, ${recursive}, ${callback})")
        val db = database.writableDatabase
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
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
                LIMIT
                    1
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMaxId = it.getLong(it.getColumnIndexOrThrow("tweet_id"))
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMaxId != 0L) {
            requestMap["since_id"] = tweetMaxId.toString()
        }
        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId, ::getNextHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId)
        }
        Log.d(TAG, "[END]getNextHomeTweet(${userId}, ${recursive}, ${callback})")
    }

    /**
     * TODO
     *
     * @param userId
     * @param recursive
     */
    protected fun getPrevHomeTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevHomeTweet(${userId}, ${recursive})")
        val db = database.writableDatabase
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
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                ASC
                LIMIT
                    1
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMinId = it.getLong(it.getColumnIndexOrThrow("tweet_id")) - 1
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMinId != 0L) {
            requestMap["max_id"] = tweetMinId.toString()
        }
        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId, ::getPrevHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId)
        }
        Log.d(TAG, "[END]getPrevHomeTweet(${userId}, ${recursive})")
    }

    /**
     * TODO
     *
     * @param userId
     * @return
     */
    protected fun getUser(userId: Long) : UserObject
    {
        Log.d(TAG, "[START]getUser(${userId})")
        var data: UserObject? = null
        val db = database.readableDatabase
        db.rawQuery("SELECT data FROM t_users WHERE user_id = ?", arrayOf(userId.toString())).use {
            it.moveToFirst()
            data = Json.jsonDecode(UserObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
        }
        Log.d(TAG, "[END]getUser(${userId})")

        return data!!
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

//        val message1Layout = findViewById<LinearLayout>(R.id.message1_layout)
//        message1Layout.post {
//            val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
//                setMargins(0, 0, 0, -message1Layout.height)
//                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
//            }
//            message1Layout.layoutParams = params
//        }

        if (Settings.canDrawOverlays(applicationContext) == false) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        Log.d(TAG, "[END]onCreate(${savedInstanceState})")
    }
}
